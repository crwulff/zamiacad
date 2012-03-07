/* 
 * Copyright 2010,2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 17, 2010
 */
package org.zamia.rtl;

import java.util.HashSet;

import org.zamia.DMManager;
import org.zamia.ERManager;
import org.zamia.ExceptionLogger;
import org.zamia.IZamiaMonitor;
import org.zamia.SourceLocation;
import org.zamia.Toplevel;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaException;
import org.zamia.ZamiaException.ExCat;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGInstantiation;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGModule;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.rtl.RTLType.TypeCat;
import org.zamia.util.HashSetArray;
import org.zamia.util.PathName;
import org.zamia.util.ZStack;
import org.zamia.vhdl.ast.DMUID;
import org.zamia.zdb.ZDB;

/**
 * Persists RTLModules in ZDB
 * 
 * @author Guenter Bartsch
 *
 */

public class RTLManager {

	protected final static ZamiaLogger logger = ZamiaLogger.getInstance();

	protected final static ExceptionLogger el = ExceptionLogger.getInstance();

	private static final String MODULE_IDX = "RTLM_ModuleIdx"; // signature -> RTLModule

	private static final String TYPES_IDX = "RTLM_TypesIdx";

	private static final String TYPES_BIT_PREFIX = "STDBIT_";

	private static final String SIGNATURES_IDX = "RTLM_SignaturesIdx"; // uid -> HSA{signature, signature, ...}

	private RTLType fBitType;

	private final ZamiaProject fZPrj;

	private final ZDB fZDB;

	private final DMManager fDUM;

	private final IGManager fIGM;

	private final ERManager fERM;

	private IZamiaMonitor fMonitor;

	private int fNumDone; // for progress reporting

	private HashSet<String> fTodo;

	private ZStack<BuildNodeJob> fTodoStack;

	private synchronized void updateStats(ToplevelPath aPath, String aSignature) {
		fNumDone++;
		logger.info("RTLManager: %d modules done (%d todo ATM): building %s", fNumDone, getNumTodo(), aPath);
	}
	
	public void clean() {
		fZDB.delAllIdx(MODULE_IDX);
		fZDB.delAllIdx(SIGNATURES_IDX);
	}

	private class BuildNodeJob implements Runnable {

		public final ToplevelPath fPath;

		public final DMUID fDUUID;

		public final String fSignature;

		public final SourceLocation fLocation;

		public final DMUID fParentDUUID;

		private IGSynth fSynth;

		public BuildNodeJob(IGSynth aSynth, ToplevelPath aPath, DMUID aParentDUUID, DMUID aDUUID, String aSignature, SourceLocation aLocation) {
			fPath = aPath;
			fParentDUUID = aParentDUUID;
			fDUUID = aDUUID;
			fSignature = aSignature;
			fLocation = aLocation;
			fSynth = aSynth;
		}

		@Override
		public void run() {
			RTLModule module = null;

			try {
				try {

					module = getOrCreateRTLModule(fPath, fParentDUUID, fDUUID, fSignature, false, fLocation);

					updateStats(fPath, fSignature);

					if (module.isSynthesized()) {
						logger.error("RTLManager: Internal error: module %s on todo list was already done!", fSignature);
					} else {

						fSynth.synthesizeBody();
					}
				} catch (ZamiaException e) {
					el.logException(e);
					fERM.addError(new ZamiaException(ExCat.RTL, true, e.getMessage(), e.getLocation()));
				}
			} catch (Throwable t) {
				el.logException(t);
			}

			if (module != null) {
				module.setSynthesized(true);
				module.storeOrUpdate();
			}

			fTodo.remove(fSignature);
		}
	}

	public RTLManager(ZamiaProject aZPrj) {
		fZPrj = aZPrj;
		fZDB = fZPrj.getZDB();
		fDUM = fZPrj.getDUM();
		fIGM = fZPrj.getIGM();
		fERM = fZPrj.getERM();

		initStdTypes();
	}

	private void initStdTypes() {

		String sig = TYPES_BIT_PREFIX + "SINGLE";

		long id = fZDB.getIdx(TYPES_IDX, sig);
		if (id == 0) {

			fBitType = new RTLType(TypeCat.BIT, null, fZDB);

			id = fZDB.store(fBitType);

			fZDB.putIdx(TYPES_IDX, sig, id);

		} else {
			fBitType = (RTLType) fZDB.load(id);
		}
	}

	public RTLType getBitType() {
		return fBitType;
	}

	public RTLType getBitVectorType(int aWidth) {

		String sig = TYPES_BIT_PREFIX + aWidth;

		RTLType bvt = null;

		long id = fZDB.getIdx(TYPES_IDX, sig);
		if (id == 0) {

			bvt = new RTLType(TypeCat.ARRAY, null, fZDB);

			bvt.setArrayParams(fBitType, aWidth - 1, false, 0);

			id = fZDB.store(bvt);

			fZDB.putIdx(TYPES_IDX, sig, id);

		} else {
			bvt = (RTLType) fZDB.load(id);
		}

		return bvt;
	}

	public RTLModule findModule(String aSignature) {

		long id = fZDB.getIdx(MODULE_IDX, aSignature);
		if (id == 0) {
			return null;
		}

		RTLModule module = (RTLModule) fZDB.load(id);

		return module;
	}

	public RTLModule findModule(Toplevel aTL) {

		DMUID duuid = fDUM.getArchDUUID(aTL);

		if (duuid == null)
			return null;

		String signature = IGInstantiation.computeSignature(duuid, null);

		return findModule(signature);
	}

	public RTLModule buildRTL(Toplevel aTL, IZamiaMonitor aMonitor, int aTotalUnits) throws ZamiaException {

		RTLModule module = null;

		fMonitor = aMonitor;

		DMUID duuid = fDUM.getArchDUUID(aTL);

		if (duuid == null) {
			logger.error("RTLManager: Failed to find toplevel %s.", aTL);
			fERM.addError(new ZamiaException(ExCat.RTL, true, "RTLManager: failed to find toplevel " + aTL, aTL.getLocation()));
			return null;
		}

		fNumDone = 0;

		String signature = IGInstantiation.computeSignature(duuid, null);

		fTodo = new HashSet<String>();

		fTodoStack = new ZStack<BuildNodeJob>();

		module = getOrCreateRTLModule(new ToplevelPath(aTL, new PathName("")), null, duuid, signature, true, aTL.getLocation());

		while (!fTodoStack.isEmpty()) {

			if (isCanceled()) {
				logger.info("Canceled.");
				break;
			}

			BuildNodeJob job = fTodoStack.pop();
			job.run();
		}

		return module;
	}

	@SuppressWarnings("unchecked")
	public RTLModule getOrCreateRTLModule(ToplevelPath aPath, DMUID aParentDUUID, DMUID aDUUID, String aSignature, boolean aSynthesize, SourceLocation aLocation) {

		RTLModule module = null;

		long mid = fZDB.getIdx(MODULE_IDX, aSignature);

		if (mid != 0) {

			module = (RTLModule) fZDB.load(mid);

		} else {

			try {

				IGModule m = fIGM.findModule(aSignature);
				if (m != null) {

					IGSynth synth = new IGSynth(fZPrj);

					module = synth.synthesizePorts(m);

					mid = module.storeOrUpdate();
					fZDB.putIdx(RTLManager.MODULE_IDX, aSignature, mid);

					if (!module.isSynthesized() && aSynthesize) {

						String uid = aDUUID.getUID();

						long dbid = fZDB.getIdx(SIGNATURES_IDX, uid);
						HashSetArray<String> signatures = new HashSetArray<String>();
						if (dbid == 0) {
							signatures = new HashSetArray<String>();
							signatures.add(aSignature);
							dbid = fZDB.store(signatures);
							fZDB.putIdx(SIGNATURES_IDX, uid, dbid);
						} else {
							signatures = (HashSetArray<String>) fZDB.load(dbid);
							if (signatures.add(aSignature)) {
								fZDB.update(dbid, signatures);
							}
						}

						if (!fTodo.contains(aSignature)) {
							fTodo.add(aSignature);

							BuildNodeJob job = new BuildNodeJob(synth, aPath, aParentDUUID, aDUUID, aSignature, aLocation);

							fTodoStack.push(job);
						}
					}

				} else {
					fERM.addError(new ZamiaException(ExCat.RTL, true, "RTLManager: failed to find " + aDUUID, aLocation));
				}
			} catch (ZamiaException e) {
				el.logException(e);
				fERM.addError(new ZamiaException(ExCat.RTL, true, e.getMessage(), e.getLocation()));
			}
		}

		return module;
	}

	private int getNumTodo() {
		int n = fTodo.size();
		return n;
	}

	private boolean isCanceled() {
		if (fMonitor == null) {
			return false;
		}
		return fMonitor.isCanceled();
	}
}
