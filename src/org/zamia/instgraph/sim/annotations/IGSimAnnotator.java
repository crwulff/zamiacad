/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Oct 4, 2009
 */
package org.zamia.instgraph.sim.annotations;

import java.math.BigInteger;

import org.zamia.DMManager;
import org.zamia.ExceptionLogger;
import org.zamia.IDesignModule;
import org.zamia.SFDMInfo;
import org.zamia.SourceFile;
import org.zamia.SourceLocation;
import org.zamia.Toplevel;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.analysis.SourceLocation2IG;
import org.zamia.instgraph.IGConcurrentStatement;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGContainerItem;
import org.zamia.instgraph.IGInstantiation;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGMapping;
import org.zamia.instgraph.IGModule;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGObject.IGObjectCat;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGStructure;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.instgraph.sim.IGISimCursor;
import org.zamia.instgraph.sim.IGISimulator;
import org.zamia.util.HashMapArray;
import org.zamia.util.HashSetArray;
import org.zamia.util.Pair;
import org.zamia.util.PathName;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.vhdl.ast.Architecture;
import org.zamia.vhdl.ast.Block;
import org.zamia.vhdl.ast.DMUID;
import org.zamia.vhdl.ast.DeclarativeItem;
import org.zamia.vhdl.ast.GenerateStatement;
import org.zamia.vhdl.ast.Name;
import org.zamia.vhdl.ast.NameExtension;
import org.zamia.vhdl.ast.NameExtensionIndex;
import org.zamia.vhdl.ast.NameExtensionRange;
import org.zamia.vhdl.ast.NameExtensionSuffix;
import org.zamia.vhdl.ast.Operation;
import org.zamia.vhdl.ast.OperationLiteral;
import org.zamia.vhdl.ast.Range;
import org.zamia.zdb.ZDB;


/**
 * Compute annotations for a given source file + path from a given simulator
 * plugin
 * 
 * @author Guenter Bartsch
 * 
 */

public class IGSimAnnotator {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public final static boolean dump = false;

	private ZamiaProject fZPrj;

	private DMManager fDMM;

	private IGManager fIGM;

	private IGInterpreterRuntimeEnv fAnnotationsEnv;

	private Toplevel fToplevel;

	private IGModule fModule;

	private HashMapArray<PathName, SignalInfo> fSignalInfos;

	private ZDB fZDB;

	private HashSetArray<PathName> fLocalPrefixes;

	private Architecture fArch;

	private ToplevelPath fTLP;

	static class SignalInfo {

		private final PathName fLocalPrefix;

		private final long fDBID;

		public SignalInfo(PathName aLocalPrefix, long aDBID) {
			fLocalPrefix = aLocalPrefix;
			fDBID = aDBID;
		}

		public PathName getLocalPrefix() {
			return fLocalPrefix;
		}

		public long getDBID() {
			return fDBID;
		}
	}

	public IGSimAnnotator(ZamiaProject aZPrj) {

		fZPrj = aZPrj;
		fZDB = fZPrj.getZDB();
		fDMM = fZPrj.getDUM();
		fIGM = fZPrj.getIGM();
	}

	private void initValues(IGContainer aContainer, IGInterpreterRuntimeEnv aEnv, IGISimulator aSim, BigInteger aTimeOffset, PathName aPathPrefix) throws ZamiaException {

		IGISimCursor cursor = aSim.createCursor();

		int n = aContainer.getNumLocalItems();
		for (int i = 0; i < n; i++) {
			IGContainerItem item = aContainer.getLocalItem(i);

			if (!(item instanceof IGObject)) {
				continue;
			}

			try {

				IGObject obj = (IGObject) item;

				IGObjectCat cat = obj.getCat();

				aEnv.newObject(obj, ASTErrorMode.EXCEPTION, null, obj.computeSourceLocation());

				if (cat == IGObjectCat.SIGNAL && obj.getDirection() == OIDir.NONE) {
					PathName signalName = aPathPrefix.append(obj.getId());
					if (cursor.gotoTransition(signalName, aTimeOffset)) {
						IGStaticValue value = cursor.getCurrentValue();
						if (value != null) {
							aEnv.setObjectValue(obj, value, null);
						} else {
							logger.debug("SimAnnotations: Couldn't get sim value for signal '%s'", signalName);
						}
					} else {
						logger.debug("SimAnnotations: Couldn't get sim value for signal '%s'", signalName);
					}
				}
			} catch (Throwable t) {
				el.logException(t);
			}
		}

		cursor.dispose();
	}

	private void computeEnv(IGItem aItem, IGISimulator aSim, BigInteger aTimeOffset, PathName aPath) throws ZamiaException {
		if (aItem instanceof IGModule) {

			IGModule module = (IGModule) aItem;

			fAnnotationsEnv.enterContext();

			initValues(module.getStructure().getContainer(), fAnnotationsEnv, aSim, aTimeOffset, aPath);

			fModule = module;

		} else if (aItem instanceof IGStructure) {
			IGStructure struct = (IGStructure) aItem;

			fAnnotationsEnv.enterContext();

			initValues(struct.getContainer(), fAnnotationsEnv, aSim, aTimeOffset, aPath);

			IGInterpreterCode ic = new IGInterpreterCode("Mapping computation for " + struct, null);

			int m = struct.getNumMappings();
			for (int j = 0; j < m; j++) {

				IGMapping mapping = struct.getMapping(j);

				mapping.generateCode(ic, mapping.computeSourceLocation());
			}

			try {
				fAnnotationsEnv.call(ic, ASTErrorMode.EXCEPTION, null);
				fAnnotationsEnv.resume(ASTErrorMode.EXCEPTION, null);
				fAnnotationsEnv.rts();
			} catch (Throwable t) {
				el.logException(t);
			}

		} else if (aItem instanceof IGInstantiation) {

			// real work starts here.

			IGInstantiation inst = (IGInstantiation) aItem;

			fAnnotationsEnv.enterContext();

			PathName childPath = aPath.append((String) null);
			aItem = fIGM.findItem(fToplevel, childPath);
			IGModule child = (IGModule) aItem;

			if (child != null) {

				fModule = child;

				IGContainer childContainer = child.getStructure().getContainer();

				initValues(childContainer, fAnnotationsEnv, aSim, aTimeOffset, aPath);

				// compute mappings

				IGInterpreterCode ic = new IGInterpreterCode("Mapping computation for " + inst, null);

				int m = inst.getNumMappings();
				for (int j = 0; j < m; j++) {

					try {
						IGMapping mapping = inst.getMapping(j);
						mapping.generateCode(ic, null);
					} catch (Throwable t) {
						el.logException(t);
					}
				}

				try {
					fAnnotationsEnv.call(ic, ASTErrorMode.EXCEPTION, null);
					fAnnotationsEnv.resume(ASTErrorMode.EXCEPTION, null);
					fAnnotationsEnv.rts();
				} catch (Throwable t) {
					el.logException(t);
				}

				if (dump) {
					m = childContainer.getNumInterfaces();
					for (int j = 0; j < m; j++) {

						IGObject iface = childContainer.getInterface(j);

						IGStaticValue actualValue = fAnnotationsEnv.getObjectValue(iface);
						logger.info("      INTERFACE %s => %s", iface.getId(), actualValue);
					}
				}

			} else {
				logger.error("SimAnnotations: Couldn't resolve module '%s'", childPath);
			}
		}
	}

	private void computeEnvRec(IGStructure aStruct, IGISimulator aSim, BigInteger aTimeOffset, PathName aPath, PathName aLocalPrefix) throws ZamiaException {

		fLocalPrefixes.add(aLocalPrefix);

		computeEnv(aStruct, aSim, aTimeOffset, aPath);
		computeSignalInfos(aStruct.getContainer(), aLocalPrefix);

		int n = aStruct.getNumStatements();
		for (int i = 0; i < n; i++) {
			IGConcurrentStatement stmt = aStruct.getStatement(i);

			if (!(stmt instanceof IGStructure)) {
				continue;
			}

			IGStructure struct = (IGStructure) stmt;

			computeEnvRec(struct, aSim, aTimeOffset, aPath.append(struct.getLabel()), aLocalPrefix.append(struct.getLabel()));
		}
	}

	private void computeSignalInfos(IGContainer aContainer, PathName aLocalPrefix) {
		int n = aContainer.getNumLocalItems();
		for (int i = 0; i < n; i++) {
			IGContainerItem item = aContainer.getLocalItem(i);

			if (!(item instanceof IGObject)) {
				continue;
			}

			IGObject obj = (IGObject) item;

			PathName localPath = aLocalPrefix.append(obj.getId());

			PathName absPath = fTLP.getPath().append(localPath);

			fSignalInfos.put(absPath, new SignalInfo(localPath, obj.getDBID()));
		}
	}

	public boolean genAnnotationsEnv(SourceFile aSF, ToplevelPath aPath, IGISimulator aSim, BigInteger aTimeOffset) {

		try {
			SFDMInfo info = fDMM.compileFile(aSF, null);


			Pair<IGItem, ToplevelPath> pair = null;

			for (DMUID duuid : info) {

				IDesignModule dm = fDMM.getDM(duuid);
				if (!(dm instanceof Architecture)) {
					continue;
				}

				fArch = (Architecture) dm;

				SourceLocation location = fArch.getLocation();

				pair = SourceLocation2IG.findNearestItem(location, aPath, fZPrj);

				if (pair != null) {
					break;
				}
			}

			if (pair == null) {
				logger.error("SimAnnotations: Failed to match IG/DUM.");
				return false;
			}

			/*
			 * set up annotation runtime environment
			 */

			fTLP = pair.getSecond();
			PathName path = fTLP.getPath();

			fToplevel = fTLP.getToplevel();

			int n = path.getNumSegments();

			fAnnotationsEnv = new IGInterpreterRuntimeEnv(null, fZPrj);

			PathName cp = new PathName("");

			int i = 0;
			while (i <= n) {
				// for (int i = 0; i < n; i++) {

				logger.info("SimAnnotations: Computing env for %s", cp);

				IGItem item = fIGM.findItem(fToplevel, cp);

				computeEnv(item, aSim, aTimeOffset, cp);

				if (i < n) {
					cp = cp.append(path.getSegment(i));
				}
				i++;
			}

			// we need to compute values for all structures inside the
			// last module we have found
			// also, this is the perfect opportunity to generate
			// the list of all signals in the current module for which
			// we provide annotation values for along with the list of local
			// prefixes

			IGStructure struct = fModule.getStructure();
			fSignalInfos = new HashMapArray<PathName, SignalInfo>();
			fLocalPrefixes = new HashSetArray<PathName>();

			PathName localPrefix = new PathName("");

			computeSignalInfos(struct.getContainer(), localPrefix);

			n = struct.getNumStatements();
			for (i = 0; i < n; i++) {
				IGConcurrentStatement stmt = struct.getStatement(i);

				if (!(stmt instanceof IGStructure)) {
					continue;
				}

				IGStructure s2 = (IGStructure) stmt;
				computeEnvRec(s2, aSim, aTimeOffset, cp.append(s2.getLabel()), localPrefix.append(s2.getLabel()));
			}

			if (dump) {
				n = getNumValues();
				for (i = 0; i < n; i++) {
					localPrefix = getLocalPrefix(i);
					IGStaticValue value = getValue(i);

					logger.info("Value %20s : %s", localPrefix, value);
				}
			}

			return true;

		} catch (Throwable e) {
			el.logException(e);
		}
		return false;
	}

	public HashSetArray<IGSimAnnotation> genAnnotations() {

		// now, traverse the AST, keep track of our path prefix
		// and generate signal value annotations

		PathName pathPrefix = new PathName("");
		HashSetArray<IGSimAnnotation> res = new HashSetArray<IGSimAnnotation>();
		genAnnotationsRek(fArch, pathPrefix, 0, res);

		return res;
	}

	private PathName fAbsPath = null;

	private IGStaticValue getValue(String aId, PathName aPathPrefix) {
		int n = aPathPrefix.getNumSegments();

		for (int i = n; i >= 0; i--) {

			fAbsPath = fTLP.getPath().append(aPathPrefix.getPrefix(i)).append(aId);

			logger.debug("IGSimAnnotator: genAnnotationsRek(): computing value for %s", fAbsPath);

			SignalInfo si = fSignalInfos.get(fAbsPath);
			if (si != null) {
				IGStaticValue value = null;
				try {

					long dbid = si.getDBID();
					IGObject obj = (IGObject) fZDB.load(dbid);
					value = fAnnotationsEnv.getObjectValue(obj);
					logger.debug("IGSimAnnotator: genAnnotationsRek():     value is %s", value);
				} catch (ZamiaException e) {
					el.logException(e);
				}
				return value;
			}
		}

		return null;
	}

	private Integer getNumber(Operation aOperation) {
		
		if (! (aOperation instanceof OperationLiteral)) {
			return null;
		}
		
		OperationLiteral literal = (OperationLiteral) aOperation;
		
		String img = literal.getImage();
		
		try {
			
			int i = Integer.parseInt(img);
			return new Integer(i);
			
		} catch (Throwable t) {
		}

		return null;
	}
	

	/**
	 * very simple minded right now, will only work on record fields and static array indices
	 * (which is better than nothing)
	 * 
	 * @param aValue
	 * @param aExtension
	 * @return
	 */

	private IGStaticValue applyNameExtension(IGStaticValue aValue, NameExtension aExtension) {

		try {
			if (aExtension instanceof NameExtensionSuffix) {

				IGTypeStatic t = aValue.getStaticType();

				if (!t.isRecord()) {
					return null;
				}

				NameExtensionSuffix nes = (NameExtensionSuffix) aExtension;
				String fid = nes.getSuffix().getId();

				return aValue.getRecordFieldValue(fid, null);
			} else if (aExtension instanceof NameExtensionIndex) {
				
				NameExtensionIndex nes = (NameExtensionIndex) aExtension;
				
				IGStaticValue value = aValue;
				
				int nIdxs = nes.getNumIndices();
				for (int i = 0; i<nIdxs; i++) {

					IGTypeStatic t = value.getStaticType();

					if (!t.isArray()) {
						return null;
					}

					Operation idx = nes.getIndex(i);

					Integer num = getNumber(idx);
					
					if (num == null) {
						return null;
					}

					value = value.getValue(num.intValue(), null);

					if (value == null) {
						return null;
					}
				}
				
				return value;
			} else if (aExtension instanceof NameExtensionRange) {
				
				NameExtensionRange ner = (NameExtensionRange) aExtension;
				
				IGStaticValue value = aValue;
				
				int nRanges = ner.getNumRanges();
				for (int i = 0; i<nRanges; i++) {

					IGTypeStatic t = value.getStaticType();

					if (!t.isArray()) {
						return null;
					}

					Range range = ner.getRange(i);
					
					if (range.isRange()) {
						return null;
					}
					
					Integer num = getNumber(range.getLeft());
					
					if (num == null) {
						return null;
					}

					value = value.getValue(num.intValue(), null);

					if (value == null) {
						return null;
					}
				}
				
				return value;
			}
		} catch (Throwable t) {
		}

		return null;
	}

	private void genAnnotationsRek(VHDLNode aASTO, PathName aPathPrefix, int aDepth, HashSetArray<IGSimAnnotation> aRes) {

		if (aASTO == null) {
			return;
		}

		if (aASTO instanceof Name) {

			try {
				Name name = (Name) aASTO;

				IGStaticValue value = getValue(name.getId(), aPathPrefix);

				if (value != null) {
					int n = name.getNumExtensions();
					for (int i = 0; i < n; i++) {

						IGStaticValue nv = applyNameExtension(value, name.getExtension(i));
						if (nv == null) {
							break;
						}

						value = nv;
					}

					aRes.add(new IGSimAnnotation(name.getLocation(), value, fAbsPath));
				}

			} catch (Throwable t) {
				el.logException(t);
			}

		} else if (aASTO instanceof DeclarativeItem) {
			DeclarativeItem di = (DeclarativeItem) aASTO;

			IGStaticValue value = getValue(di.getId(), aPathPrefix);

			if (value != null) {
				aRes.add(new IGSimAnnotation(di.getLocation(), value, fAbsPath));
			}
		}

		int n = aASTO.getNumChildren();
		for (int i = 0; i < n; i++) {

			try {
				VHDLNode child = aASTO.getChild(i);

				if (child == null) {
					continue;
				}

				if (aASTO instanceof Block) {

					Block block = (Block) aASTO;

					String label = block.getLabel();
					PathName prefix = aPathPrefix;
					int depth = aDepth;
					if (label != null) {
						prefix = aPathPrefix.append(label);
						depth++;
					}

					genAnnotationsRek(child, prefix, depth, aRes);

				} else if (aASTO instanceof GenerateStatement) {

					GenerateStatement gen = (GenerateStatement) aASTO;

					String label = gen.getLabel();

					if (label != null) {

						String sPrefix = aPathPrefix.append(label).toString();

						int depth = aDepth + 1;

						int m = fLocalPrefixes.size();
						for (int j = 0; j < m; j++) {

							PathName lp = fLocalPrefixes.get(j);

							if (lp.getNumSegments() != depth) {
								continue;
							}

							if (lp.toString().startsWith(sPrefix)) {
								genAnnotationsRek(child, lp, depth, aRes);
							}
						}

					} else {
						genAnnotationsRek(child, aPathPrefix, aDepth, aRes);
					}

				} else {
					genAnnotationsRek(child, aPathPrefix, aDepth, aRes);
				}
			} catch (Throwable t) {
				el.logException(t);
			}
		}
	}

	private IGStaticValue getValue(int aI) throws ZamiaException {
		long dbid = fSignalInfos.get(aI).getDBID();

		IGObject obj = (IGObject) fZDB.load(dbid);

		return fAnnotationsEnv.getObjectValue(obj);

	}

	private PathName getLocalPrefix(int aI) {
		return fSignalInfos.get(aI).getLocalPrefix();
	}

	private int getNumValues() {
		return fSignalInfos != null ? fSignalInfos.size() : 0;
	}

}
