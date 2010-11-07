/* 
 * Copyright 2008,2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by guenter on Jul 10, 2008
 */

package org.zamia;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

import org.zamia.ZamiaException.ExCat;
import org.zamia.instgraph.interpreter.IGInterpreterContext;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.VHDLIndexer;
import org.zamia.vhdl.ast.Architecture;
import org.zamia.vhdl.ast.DMUID;
import org.zamia.vhdl.ast.DMUID.LUType;
import org.zamia.vhdl.ast.Entity;
import org.zamia.vhdl.ast.Library;
import org.zamia.vhdl.ast.VHDLPackage;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class DMManager {

	private final static boolean dump = false;

	private final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private final static ExceptionLogger el = ExceptionLogger.getInstance();

	private final static FSCache fsCache = FSCache.getInstance();

	// uid -> DesignUnitStub, result of indexing
	private static final String STUBS_IDX = "DUM_StubsIdx";

	// uid -> DesignUnit, index of elaborated design units
	private static final String DM_IDX = "DUM_DMIdx";

	// id -> Library, library management
	private static final String LIB_IDX = "DUM_LibIdx";

	private final ZamiaProject fZPrj;

	private final ZDB fZDB;

	private final ERManager fERM;

	private final VHDLIndexer fVHDLIndexer;

	private final IHDLParser fVHDLParser, fVerilogParser;

	// packages store their constants and types here
	private static final String PKGCONTEXT_OBJ_NAME = "DUM_GlobalPkgContext";

	private IGInterpreterContext fGlobalPackageContext;

	// path -> HashSetArray<DUUID> - used for cleaning/rebuilding and caching
	private static final String SFMAP_OBJ_NAME = "DUM_SFMap";

	private HashMap<String, SFDMInfo> fSFMap;

	// (entity-)UID -> HashSetArray<DUUID> - used to find architectures that implement a given entity-uid
	private static final String EA_OBJ_NAME = "DUM_EA";

	private HashMap<String, HashSetArray<DMUID>> fEA;

	private IDesignModule fWantedDM;

	private String fWantedUID;

	@SuppressWarnings("unchecked")
	public DMManager(ZamiaProject aZPrj) throws IOException, ZamiaException {
		fZPrj = aZPrj;
		fZDB = fZPrj.getZDB();
		fERM = fZPrj.getERM();
		fVHDLIndexer = fZPrj.getVHDLIndexer();
		fVHDLParser = fZPrj.getVHDLParser();
		fVerilogParser = fZPrj.getVerilogParser();

		fGlobalPackageContext = (IGInterpreterContext) fZDB.getNamedObject(PKGCONTEXT_OBJ_NAME);
		if (fGlobalPackageContext == null) {
			fGlobalPackageContext = new IGInterpreterContext();
			fZDB.createNamedObject(PKGCONTEXT_OBJ_NAME, fGlobalPackageContext);
		}

		fSFMap = (HashMap<String, SFDMInfo>) fZDB.getNamedObject(SFMAP_OBJ_NAME);
		if (fSFMap == null) {
			fSFMap = new HashMap<String, SFDMInfo>();
			fZDB.createNamedObject(SFMAP_OBJ_NAME, fSFMap);
		}

		fEA = (HashMap<String, HashSetArray<DMUID>>) fZDB.getNamedObject(EA_OBJ_NAME);
		if (fEA == null) {
			fEA = new HashMap<String, HashSetArray<DMUID>>();
			fZDB.createNamedObject(EA_OBJ_NAME, fEA);
		}

		indexStdLibs();
	}

	public synchronized void clean() throws IOException, ZamiaException {
		fGlobalPackageContext = new IGInterpreterContext();
		fZDB.createNamedObject(PKGCONTEXT_OBJ_NAME, fGlobalPackageContext);
		fSFMap = new HashMap<String, SFDMInfo>();
		fZDB.createNamedObject(SFMAP_OBJ_NAME, fSFMap);
		fEA = new HashMap<String, HashSetArray<DMUID>>();
		fZDB.createNamedObject(EA_OBJ_NAME, fEA);

		indexStdLibs();
	}

	public SFDMInfo compileFile(SourceFile aSF, Reader aReader) throws IOException, ZamiaException {

		String libId = "WORK";
		int priority = Integer.MAX_VALUE;
		boolean bottomUp = true;
		boolean useFSCache = false;

		BuildPath bp = fZPrj.getBuildPath();
		if (bp != null) {

			BuildPathEntry entry = bp.findEntry(aSF);

			if (entry != null) {
				libId = entry.fLibId;
				priority = entry.fPriority;
				bottomUp = entry.fBottomUp;
				useFSCache = entry.fReadonly;
			}
		}

		return compileFile(aSF, aReader, libId, priority, bottomUp, useFSCache, true);
	}

	public synchronized SFDMInfo compileFile(SourceFile aSF, Reader aReader, String aLibId, int aPriority, boolean aBottomUp, boolean aUseFSCache, boolean aUseDUCache)
			throws IOException, ZamiaException {

		String filename = aSF.getAbsolutePath();

		SFDMInfo info = null;
		if (aUseDUCache) {
			info = fSFMap.get(filename);
			if (info != null) {

				boolean upToDate = true;

				String path = aSF.getAbsolutePath();
				if (path != null) {
					long ts = FSCache.getInstance().getLastModified(path, aUseFSCache);

					upToDate = ts <= info.getTimestamp();
				}

				if (upToDate) {
					logger.info("DMManager: Not running parser for " + aSF + " because we have cached the result.");
					return info;
				}
			}
		}

		aSF.flush();

		// keep old info in case compilation fails
		SFDMInfo oldInfo = info;

		removeStubs(aSF);
		fERM.removeErrors(aSF, ExCat.FRONTEND);
		fERM.removeErrors(aSF, ExCat.INTERMEDIATE);

		IHDLParser compiler = getCompiler(aSF);

		if (compiler == null)
			return null;

		int oldNErrors = fERM.getNumErrors();
		Reader reader = null;
		HashSetArray<DMUID> duuids = null;
		try {
			reader = aReader;
			if (reader == null) {
				reader = fsCache.openFile(aSF, aUseFSCache);
			}

			String libId = aLibId;
			int priority = aPriority;
			if (libId == null) {

				BuildPathEntry entry = fZPrj.getBuildPath().findEntry(aSF);
				if (entry != null) {
					libId = entry.fLibId;
					priority = entry.fPriority;
				} else {
					libId = "WORK";
				}

			}

			if (libId == null) {
				logger.debug("DMManager: Not parsing '%s' because we have a 'none' bp entry.", aSF);
				return new SFDMInfo();
			}
			logger.info("DMManager: Parsing '%s' => Library '%s'", aSF, libId);

			duuids = compiler.parse(reader, libId, aSF, priority, aUseFSCache, aBottomUp, fZPrj);
		} catch (IOException e) {
			el.logException(e);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		int newNErrors = fERM.getNumErrors();
		if (newNErrors > oldNErrors) {
			logger.error("DMManager: %d errors found while parsing '%s'", newNErrors - oldNErrors, aSF.getAbsolutePath());
		}

		if (oldInfo != null) {
			if (duuids == null || duuids.size() == 0) {
				// io error occured, 
				// restore old stub information
				int n = oldInfo.getNumDMUIDs();
				for (int i = 0; i < n; i++) {
					DMUID duuid = oldInfo.getDMUID(i);

					addDesignUnitSource(duuid, aSF, aPriority, aUseFSCache);
				}
			} else {
				info = new SFDMInfo(duuids);
			}
		} else {
			info = new SFDMInfo(duuids);
		}

		return info;
	}

	public synchronized void indexFile(SourceFile aSF, String aLibId, int aPriority, boolean aUseFSCache) throws IOException, ZamiaException {
		aSF.flush();
		removeStubs(aSF);
		fERM.removeErrors(aSF);

		String libId = aLibId;
		int priority = aPriority;
		if (libId == null) {

			BuildPathEntry entry = fZPrj.getBuildPath().findEntry(aSF);
			if (entry != null) {
				libId = entry.fLibId;
				priority = entry.fPriority;
			} else {
				libId = "WORK";
			}

		}

		if (libId != null) {
			Reader reader = fsCache.openFile(aSF, aUseFSCache);
			try {
				fVHDLIndexer.parse(reader, libId, aSF, priority, aUseFSCache, true, this);
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
		}
	}

	private void indexStdLib(String aPath, String aLibId) throws IOException, ZamiaException {

		SourceFile sf = new SourceFile("/" + aPath);

		Reader r = fsCache.openFile(sf, false);
		if (r != null) {

			indexFile(sf, aLibId, 0, false);

			if (fERM.getNumErrors(sf) > 0) {
				logger.error("DMManager: INTERNAL ERROR: parsing of std lib failed.");
				for (int i = 0; i < fERM.getNumErrors(); i++) {
					ZamiaException errMsg = fERM.getError(i);
					logger.error(errMsg.toString());
				}
				System.exit(1);
			}

			r.close();
		}
	}

	private void indexStdLibs() throws IOException, ZamiaException {

		long startTime = System.currentTimeMillis();

		indexStdLib("vhdl/standard.vhdl", "STD");
		indexStdLib("vhdl/textio.vhdl", "STD");
		indexStdLib("vhdl/std_logic_1164.vhdl", "IEEE");
		indexStdLib("vhdl/std_logic_arith.vhdl", "IEEE");
		indexStdLib("vhdl/std_logic_misc.vhdl", "IEEE");
		indexStdLib("vhdl/std_logic_signed.vhdl", "IEEE");
		indexStdLib("vhdl/std_logic_unsigned.vhdl", "IEEE");
		indexStdLib("vhdl/std_logic_textio.vhdl", "IEEE");
		indexStdLib("vhdl/numeric_std.vhdl", "IEEE");
		indexStdLib("vhdl/numeric_bit.vhdl", "IEEE");
		indexStdLib("vhdl/vital_primitives.vhdl", "IEEE");
		indexStdLib("vhdl/vital_primitives_body.vhdl", "IEEE");
		indexStdLib("vhdl/vital_timing.vhdl", "IEEE");
		indexStdLib("vhdl/vital_timing_body.vhdl", "IEEE");
		indexStdLib("vhdl/mathpack.vhdl", "IEEE");

		long endTime = System.currentTimeMillis() - startTime;

		logger.debug("DMManager: compiling std libs took %dms", endTime);
	}

	public IHDLParser getCompiler(SourceFile aSF) {
		IHDLParser compiler;
		switch (aSF.getFormat()) {
		case SourceFile.FORMAT_VHDL:
			compiler = fVHDLParser;
			break;
		case SourceFile.FORMAT_VERILOG:
			compiler = fVerilogParser;
			break;
		default:
			logger.error("DMManager: File format not recognized: " + aSF);
			return null;
		}

		return compiler;
	}

	public synchronized SFDMInfo removeStubs(SourceFile aSF) {

		String filename = aSF.getAbsolutePath();
		//logger.debug("DMManager: Removing stubs from '%s'", filename);
		SFDMInfo info = fSFMap.get(filename);

		if (info != null) {
			int n = info.getNumDMUIDs();

			for (int i = 0; i < n; i++) {
				DMUID duuid = info.getDMUID(i);

				String uid = duuid.getUID();

				//logger.debug("DMManager: Removing stub for '%s'", uid);

				fZDB.delIdxObj(STUBS_IDX, uid);
				fZDB.delIdxObj(DM_IDX, uid);
				// FIXME: remove from EA_IDX ?
			}
			fSFMap.remove(filename);
		}

		return info;
	}

	public synchronized Library getLibrary(String aId) {

		Library lib = (Library) fZDB.getIdxObj(LIB_IDX, aId);

		if (lib == null) {
			lib = new Library(aId);
			fZDB.putIdxObj(LIB_IDX, aId, lib);
		}

		return lib;

	}

	public synchronized void addDesignUnit(IDesignModule aDM, SourceFile aSF, String aLibId, int aPriority, boolean aUseFSCache) throws ZamiaException {

		DMUID duuid = aDM.getDMUID(aLibId);

		String uid = duuid.getUID();
		if (uid.equals(fWantedUID)) {
			logger.debug("DU Cache: pinning wanted DU '%s'", uid);
			fWantedDM = aDM;
		}

		if (aSF != null) {
			addDesignUnitSource(duuid, aSF, aPriority, aUseFSCache);
		}

		fZDB.putIdxObj(DM_IDX, uid, aDM);
	}

	public synchronized void addDesignUnitSource(DMUID aDUUID, SourceFile aSF, int aPriority, boolean aReadonly) {

		Library lib = getLibrary(aDUUID.getLibId());
		lib.add(aDUUID);

		String uid = aDUUID.getUID();

		logger.debug("DMManager: Adding DU source for %s: '%s', uuid is '%s'", aDUUID.toString(), aSF, uid);

		DesignModuleStub oldstub = (DesignModuleStub) fZDB.getIdxObj(STUBS_IDX, uid);
		if (oldstub != null && oldstub.getPriority() > aPriority) {
			logger.info("DMManager: Already got a higher-priority stub for " + uid + ": " + oldstub.getSourceFile());
			return;
		}

		DesignModuleStub stub = new DesignModuleStub(aDUUID, aSF, aPriority, aReadonly);
		fZDB.putIdxObj(STUBS_IDX, uid, stub);

		String filename = aSF.getAbsolutePath();
		SFDMInfo info = fSFMap.get(filename);
		if (info == null) {
			info = new SFDMInfo();
			fSFMap.put(filename, info);
		} else {
			info.touch();
		}
		info.add(aDUUID);

		if (aDUUID.getType() == LUType.Architecture) {

			String entityId = aDUUID.getId();

			String entityUID = aDUUID.getLibId() + "_#_entity_#_" + entityId;

			HashSetArray<DMUID> archs = fEA.get(entityUID);
			if (archs == null) {
				archs = new HashSetArray<DMUID>();
				fEA.put(entityUID, archs);
			}
			archs.add(aDUUID);
		}
	}

	private IDesignModule compileFromStub(String aUID) throws ZamiaException {
		IDesignModule dm = null;

		DesignModuleStub stub = (DesignModuleStub) fZDB.getIdxObj(STUBS_IDX, aUID);
		if (stub != null) {

			SourceFile sf = stub.getSourceFile();

			logger.debug("DMManager: Found a stub for %s. Need to compile %s...", aUID, sf);

			fWantedUID = aUID; // in case the one we're looking for gets kicked out of the cache right away

			long start = System.currentTimeMillis();

			try {

				compileFile(sf, null, DMUID.getLibId(aUID), stub.getPriority(), true, stub.isUseFSCache(), false);

			} catch (IOException e) {
				e.printStackTrace();
				throw new ZamiaException("IOException caught: " + e);
			}
			long end = System.currentTimeMillis();
			double t = (end - start) / 1000.0;
			logger.debug("DMManager: Compilation of %s took %f s", sf, t);

			dm = fWantedDM;
			if (dm == null) {
				logger.error("DMManager: compile from stub failed to produce '%s'", aUID);
			} else {
				fWantedDM = null;
				fWantedUID = null;
			}
		}

		return dm;
	}

	public IDesignModule getDM(DMUID aDUUID) throws ZamiaException {
		return getDM(aDUUID.getUID());
	}

	public synchronized IDesignModule getDM(String aUID) throws ZamiaException {

		if (dump) {
			logger.debug("DMManager: looking for DU '%s'", aUID);
		}

		IDesignModule dm = (IDesignModule) fZDB.getIdxObj(DM_IDX, aUID);
		if (dm == null) {

			if (dump) {
				logger.debug("DMManager: DU '%s' not in memory and no file -> compiling from stub.", aUID);
			}

			dm = compileFromStub(aUID);

			if (dm != null) {
				fZDB.putIdxObj(DM_IDX, aUID, dm);
			}
		}

		return dm;
	}

	public boolean hasDM(DMUID aDUUID) {
		return fZDB.isIdxKey(STUBS_IDX, aDUUID.getUID());
	}

	public Architecture getArchitecture(String aLibId, String aEntityId) throws ZamiaException {
		return getArchitecture(aLibId, aEntityId, null);
	}

	public synchronized DMUID getArchDUUID(String aLibId, String aId, String aArchId) {

		String libId = aLibId;
		String id = aId;
		String archId = aArchId;

		if (libId == null) {
			libId = "WORK";
		}

		if (archId == null) {
			DMUID entityDUUID = new DMUID(LUType.Entity, libId, id, null);

			String entityUID = entityDUUID.getUID();

			HashSetArray<DMUID> archDUUIDs = fEA.get(entityUID);
			if (archDUUIDs == null)
				return null;

			if (archDUUIDs.size() > 1) {
				logger.warn("DMManager: Warning: was asked for an architecture for entity %s.%s and there are multiple choices available.", aLibId, aId);
			}

			return archDUUIDs.get(0);
		}

		return new DMUID(LUType.Architecture, libId, id, archId);
	}

	public DMUID getArchDUUID(Toplevel aTL) {
		return getArchDUUID(aTL.getDUUID());
	}

	public DMUID getArchDUUID(DMUID aDUUID) {

		switch (aDUUID.getType()) {
		case Architecture:
			return aDUUID;
		case Entity:
			return getArchDUUID(aDUUID.getLibId(), aDUUID.getId(), aDUUID.getArchId());
		}
		return null;
	}

	public synchronized Architecture getArchitecture(String aLibId, String aEntityId, String aArchId) throws ZamiaException {

		if (aArchId == null) {

			DMUID entityDUUID = new DMUID(LUType.Entity, aLibId, aEntityId, null);

			String entityUID = entityDUUID.getUID();

			HashSetArray<DMUID> archDUUIDs = fEA.get(entityUID);
			if (archDUUIDs == null)
				return null;

			if (archDUUIDs.size() > 1) {
				logger.warn("DMManager: Warning: was asked for an architecture for entity %s.%s and there are multiple choices available.", aLibId, aEntityId);
			}

			return (Architecture) getDM(archDUUIDs.get(0));
		}

		DMUID duuid = new DMUID(LUType.Architecture, aLibId, aEntityId, aArchId);

		return (Architecture) getDM(duuid);
	}

	public Entity findEntity(String aLibId, String aEntityId) throws ZamiaException {

		DMUID duuid = new DMUID(LUType.Entity, aLibId, aEntityId, null);

		return (Entity) getDM(duuid);
	}

	public VHDLPackage findPackage(String aLibId, String aPkgId) throws ZamiaException {

		DMUID duuid = new DMUID(LUType.Package, aLibId, aPkgId, null);

		VHDLPackage pkg = (VHDLPackage) getDM(duuid);

		return pkg;
	}

	public int getNumStubs() {
		return fZDB.getIdxNumEntries(STUBS_IDX);
	}

	public DesignModuleStub getStub(int aIdx) {
		return (DesignModuleStub) fZDB.getIdxObj(STUBS_IDX, aIdx);
	}

	public IGInterpreterContext getGlobalPackageContext() {
		return fGlobalPackageContext;
	}

	@SuppressWarnings("unchecked")
	public synchronized void zdbChanged() {
		fGlobalPackageContext = (IGInterpreterContext) fZDB.getNamedObject(PKGCONTEXT_OBJ_NAME);
		fSFMap = (HashMap<String, SFDMInfo>) fZDB.getNamedObject(SFMAP_OBJ_NAME);
		fEA = (HashMap<String, HashSetArray<DMUID>>) fZDB.getNamedObject(EA_OBJ_NAME);
	}
}
