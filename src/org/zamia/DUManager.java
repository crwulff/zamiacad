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
import org.zamia.vhdl.ast.DUUID;
import org.zamia.vhdl.ast.DesignUnit;
import org.zamia.vhdl.ast.Entity;
import org.zamia.vhdl.ast.Library;
import org.zamia.vhdl.ast.VHDLPackage;
import org.zamia.vhdl.ast.DUUID.LUType;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class DUManager {

	private final static boolean dump = false;

	private final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private final static ExceptionLogger el = ExceptionLogger.getInstance();

	private final static FSCache fsCache = FSCache.getInstance();

	// uid -> DesignUnitStub, result of indexing
	private static final String STUBS_IDX = "DUM_StubsIdx";

	// uid -> DesignUnit, index of elaborated design units
	private static final String DU_IDX = "DUM_DUIdx";

	// id -> Library, library management
	private static final String LIB_IDX = "DUM_LibIdx";

	private final ZamiaProject fZPrj;

	private final ZDB fZDB;

	private final ERManager fERM;

	private final VHDLIndexer fVHDLIndexer;

	private final IHDLParser fVHDLParser;

	// packages store their constants and types here
	private static final String PKGCONTEXT_OBJ_NAME = "DUM_GlobalPkgContext";

	private IGInterpreterContext fGlobalPackageContext;

	// path -> HashSetArray<DUUID> - used for cleaning/rebuilding and caching
	private static final String SFMAP_OBJ_NAME = "DUM_SFMap";

	private HashMap<String, SFDUInfo> fSFMap;

	// (entity-)UID -> HashSetArray<DUUID> - used to find architectures that implement a given entity-uid
	private static final String EA_OBJ_NAME = "DUM_EA";

	private HashMap<String, HashSetArray<DUUID>> fEA;

	private DesignUnit fWantedDU;

	private String fWantedUID;

	@SuppressWarnings("unchecked")
	public DUManager(ZamiaProject aZPrj) throws IOException, ZamiaException {
		fZPrj = aZPrj;
		fZDB = fZPrj.getZDB();
		fERM = fZPrj.getERM();
		fVHDLIndexer = fZPrj.getVHDLIndexer();
		fVHDLParser = fZPrj.getVHDLParser();

		fGlobalPackageContext = (IGInterpreterContext) fZDB.getNamedObject(PKGCONTEXT_OBJ_NAME);
		if (fGlobalPackageContext == null) {
			fGlobalPackageContext = new IGInterpreterContext();
			fZDB.createNamedObject(PKGCONTEXT_OBJ_NAME, fGlobalPackageContext);
		}

		fSFMap = (HashMap<String, SFDUInfo>) fZDB.getNamedObject(SFMAP_OBJ_NAME);
		if (fSFMap == null) {
			fSFMap = new HashMap<String, SFDUInfo>();
			fZDB.createNamedObject(SFMAP_OBJ_NAME, fSFMap);
		}

		fEA = (HashMap<String, HashSetArray<DUUID>>) fZDB.getNamedObject(EA_OBJ_NAME);
		if (fEA == null) {
			fEA = new HashMap<String, HashSetArray<DUUID>>();
			fZDB.createNamedObject(EA_OBJ_NAME, fEA);
		}

		indexStdLibs();
	}

	public synchronized void clean() throws IOException, ZamiaException {
		fGlobalPackageContext = new IGInterpreterContext();
		fZDB.createNamedObject(PKGCONTEXT_OBJ_NAME, fGlobalPackageContext);
		fSFMap = new HashMap<String, SFDUInfo>();
		fZDB.createNamedObject(SFMAP_OBJ_NAME, fSFMap);
		fEA = new HashMap<String, HashSetArray<DUUID>>();
		fZDB.createNamedObject(EA_OBJ_NAME, fEA);

		indexStdLibs();
	}

	public SFDUInfo compileFile(SourceFile aSF, Reader aReader) throws IOException, ZamiaException {

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

	public synchronized SFDUInfo compileFile(SourceFile aSF, Reader aReader, String aLibId, int aPriority, boolean aBottomUp, boolean aUseFSCache, boolean aUseDUCache)
			throws IOException, ZamiaException {

		String filename = aSF.getAbsolutePath();

		SFDUInfo info = null;
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
					logger.info("DUManager: Not running parser for " + aSF + " because we have cached the result.");
					return info;
				}
			}
		}

		aSF.flush();

		// keep old info in case compilation fails
		SFDUInfo oldInfo = info;

		removeStubs(aSF);
		fERM.removeErrors(aSF, ExCat.FRONTEND);
		fERM.removeErrors(aSF, ExCat.INTERMEDIATE);

		IHDLParser compiler = getCompiler(aSF);

		if (compiler == null)
			return null;

		int oldNErrors = fERM.getNumErrors();
		Reader reader = null;
		HashSetArray<DUUID> duuids = null;
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
				logger.debug("DUManager: Not parsing '%s' because we have a 'none' bp entry.", aSF);
				return new SFDUInfo();
			}
			logger.info("DUManager: Parsing '%s' => Library '%s'", aSF, libId);

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
			logger.error("DUManager: %d errors found while parsing '%s'", newNErrors - oldNErrors, aSF.getAbsolutePath());
		}

		if (oldInfo != null) {
			if (duuids == null || duuids.size() == 0) {
				// io error occured, 
				// restore old stub information
				int n = oldInfo.getNumDUUIDs();
				for (int i = 0; i < n; i++) {
					DUUID duuid = oldInfo.getDUUID(i);

					addDesignUnitSource(duuid, aSF, aPriority, aUseFSCache);
				}
			} else {
				info = new SFDUInfo(duuids);
			}
		} else {
			info = new SFDUInfo(duuids);
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
				logger.error("DUManager: INTERNAL ERROR: parsing of std lib failed.");
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

		logger.debug("DUManager: compiling std libs took %dms", endTime);
	}

	public IHDLParser getCompiler(SourceFile aSF) {
		IHDLParser compiler;
		switch (aSF.getFormat()) {
		case SourceFile.FORMAT_VHDL:
			compiler = fVHDLParser;
			break;
		// case SourceFile.FORMAT_VERILOG:
		// compiler = verilog;
		// break;
		default:
			logger.error("DUManager: File format not recognized: " + aSF);
			return null;
		}

		return compiler;
	}

	public synchronized SFDUInfo removeStubs(SourceFile aSF) {

		String filename = aSF.getAbsolutePath();
		//logger.debug("DUManager: Removing stubs from '%s'", filename);
		SFDUInfo info = fSFMap.get(filename);

		if (info != null) {
			int n = info.getNumDUUIDs();

			for (int i = 0; i < n; i++) {
				DUUID duuid = info.getDUUID(i);

				String uid = duuid.getUID();

				//logger.debug("DUManager: Removing stub for '%s'", uid);

				fZDB.delIdxObj(STUBS_IDX, uid);
				fZDB.delIdxObj(DU_IDX, uid);
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

	public synchronized void addDesignUnit(DesignUnit aDU, SourceFile aSF, String aLibId, int aPriority, boolean aUseFSCache) throws ZamiaException {

		DUUID duuid = aDU.getDUUID(aLibId);

		String uid = duuid.getUID();
		if (uid.equals(fWantedUID)) {
			logger.debug("DU Cache: pinning wanted DU '%s'", uid);
			fWantedDU = aDU;
		}

		if (aSF != null) {
			addDesignUnitSource(duuid, aSF, aPriority, aUseFSCache);
		}

		fZDB.putIdxObj(DU_IDX, uid, aDU);
	}

	public synchronized void addDesignUnitSource(DUUID aDUUID, SourceFile aSF, int aPriority, boolean aReadonly) {

		Library lib = getLibrary(aDUUID.getLibId());
		lib.add(aDUUID);

		String uid = aDUUID.getUID();

		logger.debug("DUManager: Adding DU source for %s: '%s', uuid is '%s'", aDUUID.toString(), aSF, uid);

		DesignUnitStub oldstub = (DesignUnitStub) fZDB.getIdxObj(STUBS_IDX, uid);
		if (oldstub != null && oldstub.getPriority() > aPriority) {
			logger.info("DUManager: Already got a higher-priority stub for " + uid + ": " + oldstub.getSourceFile());
			return;
		}

		DesignUnitStub stub = new DesignUnitStub(aDUUID, aSF, aPriority, aReadonly);
		fZDB.putIdxObj(STUBS_IDX, uid, stub);

		String filename = aSF.getAbsolutePath();
		SFDUInfo info = fSFMap.get(filename);
		if (info == null) {
			info = new SFDUInfo();
			fSFMap.put(filename, info);
		} else {
			info.touch();
		}
		info.add(aDUUID);

		if (aDUUID.getType() == LUType.Architecture) {

			String entityId = aDUUID.getId();

			String entityUID = aDUUID.getLibId() + "_#_entity_#_" + entityId;

			HashSetArray<DUUID> archs = fEA.get(entityUID);
			if (archs == null) {
				archs = new HashSetArray<DUUID>();
				fEA.put(entityUID, archs);
			}
			archs.add(aDUUID);
		}
	}

	private DesignUnit compileFromStub(String aUID) throws ZamiaException {
		DesignUnit du = null;

		DesignUnitStub stub = (DesignUnitStub) fZDB.getIdxObj(STUBS_IDX, aUID);
		if (stub != null) {

			SourceFile sf = stub.getSourceFile();

			logger.debug("DUManager: Found a stub for %s. Need to compile %s...", aUID, sf);

			fWantedUID = aUID; // in case the one we're looking for gets kicked out of the cache right away

			long start = System.currentTimeMillis();

			try {

				compileFile(sf, null, DUUID.getLibId(aUID), stub.getPriority(), true, stub.isUseFSCache(), false);

			} catch (IOException e) {
				e.printStackTrace();
				throw new ZamiaException("IOException caught: " + e);
			}
			long end = System.currentTimeMillis();
			double t = (end - start) / 1000.0;
			logger.debug("DUManager: Compilation of %s took %f s", sf, t);

			du = fWantedDU;
			if (du == null) {
				logger.error("DUManager: compile from stub failed to produce '%s'", aUID);
			} else {
				fWantedDU = null;
				fWantedUID = null;
			}
		}

		return du;
	}

	public DesignUnit getDU(DUUID aDUUID) throws ZamiaException {
		return getDU(aDUUID.getUID());
	}

	public synchronized DesignUnit getDU(String aUID) throws ZamiaException {

		if (dump) {
			logger.debug("DUManager: looking for DU '%s'", aUID);
		}

		DesignUnit du = (DesignUnit) fZDB.getIdxObj(DU_IDX, aUID);
		if (du == null) {

			if (dump) {
				logger.debug("DUManager: DU '%s' not in memory and no file -> compiling from stub.", aUID);
			}

			du = compileFromStub(aUID);

			if (du != null) {
				fZDB.putIdxObj(DU_IDX, aUID, du);
			}
		}

		return du;
	}

	public boolean hasDU(DUUID aDUUID) {
		return fZDB.isIdxKey(STUBS_IDX, aDUUID.getUID());
	}

	public Architecture getArchitecture(String aLibId, String aEntityId) throws ZamiaException {
		return getArchitecture(aLibId, aEntityId, null);
	}

	public synchronized DUUID getArchDUUID(String aLibId, String aId, String aArchId) {

		String libId = aLibId;
		String id = aId;
		String archId = aArchId;

		if (libId == null) {
			libId = "WORK";
		}

		if (archId == null) {
			DUUID entityDUUID = new DUUID(LUType.Entity, libId, id, null);

			String entityUID = entityDUUID.getUID();

			HashSetArray<DUUID> archDUUIDs = fEA.get(entityUID);
			if (archDUUIDs == null)
				return null;

			if (archDUUIDs.size() > 1) {
				logger.warn("DUManager: Warning: was asked for an architecture for entity %s.%s and there are multiple choices available.", aLibId, aId);
			}

			return archDUUIDs.get(0);
		}

		return new DUUID(LUType.Architecture, libId, id, archId);
	}

	public DUUID getArchDUUID(Toplevel aTL) {
		return getArchDUUID(aTL.getDUUID());
	}

	public DUUID getArchDUUID(DUUID aDUUID) {

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

			DUUID entityDUUID = new DUUID(LUType.Entity, aLibId, aEntityId, null);

			String entityUID = entityDUUID.getUID();

			HashSetArray<DUUID> archDUUIDs = fEA.get(entityUID);
			if (archDUUIDs == null)
				return null;

			if (archDUUIDs.size() > 1) {
				logger.warn("DUManager: Warning: was asked for an architecture for entity %s.%s and there are multiple choices available.", aLibId, aEntityId);
			}

			return (Architecture) getDU(archDUUIDs.get(0));
		}

		DUUID duuid = new DUUID(LUType.Architecture, aLibId, aEntityId, aArchId);

		return (Architecture) getDU(duuid);
	}

	public Entity findEntity(String aLibId, String aEntityId) throws ZamiaException {

		DUUID duuid = new DUUID(LUType.Entity, aLibId, aEntityId, null);

		return (Entity) getDU(duuid);
	}

	public VHDLPackage findPackage(String aLibId, String aPkgId) throws ZamiaException {

		DUUID duuid = new DUUID(LUType.Package, aLibId, aPkgId, null);

		VHDLPackage pkg = (VHDLPackage) getDU(duuid);

		return pkg;
	}

	public int getNumStubs() {
		return fZDB.getIdxNumEntries(STUBS_IDX);
	}

	public DesignUnitStub getStub(int aIdx) {
		return (DesignUnitStub) fZDB.getIdxObj(STUBS_IDX, aIdx);
	}

	public IGInterpreterContext getGlobalPackageContext() {
		return fGlobalPackageContext;
	}

	@SuppressWarnings("unchecked")
	public synchronized void zdbChanged() {
		fGlobalPackageContext = (IGInterpreterContext) fZDB.getNamedObject(PKGCONTEXT_OBJ_NAME);
		fSFMap = (HashMap<String, SFDUInfo>) fZDB.getNamedObject(SFMAP_OBJ_NAME);
		fEA = (HashMap<String, HashSetArray<DUUID>>) fZDB.getNamedObject(EA_OBJ_NAME);
	}
}
