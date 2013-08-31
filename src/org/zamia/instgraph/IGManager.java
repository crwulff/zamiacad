/* 
 * Copyright 2009,2010,2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Mar 22, 2009
 */
package org.zamia.instgraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.zamia.BuildPath;
import org.zamia.DMManager;
import org.zamia.ERManager;
import org.zamia.ExceptionLogger;
import org.zamia.IDesignModule;
import org.zamia.IZamiaMonitor;
import org.zamia.SourceLocation;
import org.zamia.Toplevel;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaException;
import org.zamia.ZamiaException.ExCat;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProfiler;
import org.zamia.ZamiaProject;
import org.zamia.cli.jython.ZCJInterpreter;
import org.zamia.instgraph.IGObject.IGObjectCat;
import org.zamia.util.HashSetArray;
import org.zamia.util.Pair;
import org.zamia.util.PathName;
import org.zamia.util.ZStack;
import org.zamia.vhdl.ast.DMUID;
import org.zamia.vhdl.ast.DMUID.LUType;
import org.zamia.zdb.ZDB;
import org.zamia.zdb.ZDBListIndex;
import org.zamia.zdb.ZDBMapIndex;

/**
 * ZDB-based IG persistence manager
 * 
 * @author Guenter Bartsch
 * 
 */

public final class IGManager {

	protected final static ZamiaLogger logger = ZamiaLogger.getInstance();

	protected final static ExceptionLogger el = ExceptionLogger.getInstance();

    private static final String PYTHON_BUILD_ELABORATE_CMD = "zamia_build_elaborate";

    private static final int NUM_THREADS = 1; // set to 1 to disable multithreading code

	private static final boolean ENABLE_MULTITHREADING = NUM_THREADS > 1;

	private static final String MODULE_IDX = "IGM_ModuleIdx"; // signature -> IGModule

	private static final String INSTANTIATORS_IDX = "IGM_InstantiatorsIdx";

	private static final String PACKAGE_IDX = "IGM_PackageIdx";

	private static final String SIGNATURES_IDX = "IGM_SignaturesIdx"; // uid -> HSA{signature, signature, ...}

	private final ZamiaProject fZPrj;

	private final ZDB fZDB;

	private final DMManager fDUM;

	private final ERManager fERM;

	// single thread only:
	private ZStack<BuildNodeJob> fTodoStack;

	// multi-threading:

	private Lock fLock;

	private Condition fModuleCreatedCond;

	private HashSet<String> fTodo;

	private HashSet<String> fModulesBeingCreated;

	private ExecutorService fExecutorService;

	private IZamiaMonitor fMonitor;

	private int fNumDone; // for progress reporting

	private static final boolean ENABLE_NEW_INDICES = false;

	private static final String STRUCT_INST_IDX = "IGM_StructInstIdx"; // struct dbid -> id -> InstMapInfo

	private ZDBMapIndex<String, IGInstMapInfo> fStructInstIdx;

	private static final String STRUCT_SIGNAL_IDX = "IGM_StructSignalIdx"; // struct dbid -> id -> SignalDBID

	private ZDBMapIndex<String, Long> fStructSignalIdx;

	private static final String SIGNAL_CONN_IDX = "IGM_SignalConnIdx"; // signal dbid -> dbid, dbid, ...

	private ZDBListIndex<Long> fSignalConnIdx;

	public IGManager(ZamiaProject aZPrj) {
		fZPrj = aZPrj;
		fZDB = fZPrj.getZDB();
		fDUM = fZPrj.getDUM();
		fERM = fZPrj.getERM();

		fStructInstIdx = new ZDBMapIndex<String, IGInstMapInfo>(STRUCT_INST_IDX, fZDB);
		fStructSignalIdx = new ZDBMapIndex<String, Long>(STRUCT_SIGNAL_IDX, fZDB);
		fSignalConnIdx = new ZDBListIndex<Long>(SIGNAL_CONN_IDX, fZDB);
	}

	private synchronized void updateStats(ToplevelPath aPath, String aSignature) {
		fNumDone++;
		logger.info("IGManager: %d modules done (%d todo ATM): building %s", fNumDone, getNumTodo(), aPath);
	}

	private class BuildNodeJob implements Runnable {

		public final ToplevelPath fPath;

		public final DMUID fDUUID;

		public final String fSignature;

		public final SourceLocation fLocation;

		public final ArrayList<Pair<String, IGStaticValue>> fActualGenerics;

		public final DMUID fParentDUUID;

		private IGManager fIGM;

		public BuildNodeJob(IGManager aIGM, ToplevelPath aPath, DMUID aParentDUUID, DMUID aDUUID, String aSignature, ArrayList<Pair<String, IGStaticValue>> aActualGenerics,
				SourceLocation aLocation) {
			fIGM = aIGM;
			fPath = aPath;
			fParentDUUID = aParentDUUID;
			fDUUID = aDUUID;
			fSignature = aSignature;
			fActualGenerics = aActualGenerics;
			fLocation = aLocation;
		}

		private void index(IGStructure aStruct, long aDBID) {

			/*
			 * index signals
			 */

			IGContainer container = aStruct.getContainer();

			if (container != null) {

				int n = container.getNumLocalItems();
				for (int i = 0; i < n; i++) {

					IGContainerItem item = container.getLocalItem(i);

					if (!(item instanceof IGObject)) {
						continue;
					}

					IGObject obj = (IGObject) item;

					if (obj.getCat() != IGObjectCat.SIGNAL) {
						continue;
					}

					//logger.info("IGManager: Indexing: DBID=%5d id=%s", aDBID, obj.getId());

					fStructSignalIdx.put(aDBID, obj.getId(), obj.getDBID());
				}
			}

			/*
			 * index statements
			 */

			for (IGConcurrentStatement stmt : aStruct.getStatements()) {

				String label = stmt.getLabel();

				if (stmt instanceof IGInstantiation) {

					IGInstantiation inst = (IGInstantiation) stmt;

					long instDBID = inst.getDBID();

					String signature = inst.getSignature();

					IGModule childModule = findModule(signature);

					if (childModule != null) {

						IGInstMapInfo info = new IGInstMapInfo(childModule.getDBID(), label);

						int m = inst.getNumMappings();
						for (int j = 0; j < m; j++) {

							IGMapping mapping = inst.getMapping(j);

							IGMapInfo mapInfo = new IGMapInfo(instDBID, mapping);

							int l = mapInfo.getNumActualItems();
							for (int k = 0; k < l; k++) {
								IGItemAccess ai = mapInfo.getActualItem(k);

								IGItem item = ai.getItem();

								fSignalConnIdx.add(item.getDBID(), instDBID);
							}

							l = mapInfo.getNumFormalItems();
							for (int k = 0; k < l; k++) {
								IGItemAccess ai = mapInfo.getFormalItem(k);

								IGItem item = ai.getItem();

								info.addMapInfo(item.getDBID(), mapInfo);
							}
						}

						fStructInstIdx.put(aDBID, label, info);
					}

				} else if (stmt instanceof IGStructure) {

					IGStructure struct = (IGStructure) stmt;

					long childDBID = struct.getDBID();

					if (label != null && label.length() > 0) {
						IGInstMapInfo info = new IGInstMapInfo(childDBID, label);

						fStructInstIdx.put(aDBID, label, info);
					}

					index(struct, childDBID);

				} else if (stmt instanceof IGProcess) {
					IGProcess proc = (IGProcess) stmt;

					IGSequenceOfStatements sos = proc.getSequenceOfStatements();

					HashSetArray<IGItemAccess> accessedItems = new HashSetArray<IGItemAccess>();
					sos.computeAccessedItems(null, null, 0, accessedItems);

					for (IGItemAccess ai : accessedItems) {

						IGItem item = ai.getItem();

						fSignalConnIdx.add(item.getDBID(), fZDB.store(ai));
					}
				}
			}

		}

		@Override
		public void run() {
			IGModule module = null;

			try {
				try {

					module = getOrCreateIGModule(fPath, fParentDUUID, fDUUID, fSignature, fActualGenerics, false, fLocation);

					updateStats(fPath, fSignature);

					if (module.isStatementsElaborated()) {
						logger.error("IGManager: Internal error: module %s on todo list was already done!", fSignature);
					} else {

						IDesignModule dm = fDUM.getDM(fDUUID);
						if (dm != null) {

							dm.computeStatementsIG(fIGM, module);

						} else {
							fERM.addError(new ZamiaException(ExCat.INTERMEDIATE, true, "IGManager: failed to find " + fDUUID, fLocation));
						}
					}
				} catch (ZamiaException e) {
					el.logException(e);
					fERM.addError(new ZamiaException(ExCat.INTERMEDIATE, true, e.getMessage(), e.getLocation()));
				}
			} catch (Throwable t) {
				el.logException(t);
			}

			if (ENABLE_MULTITHREADING) {
				fLock.lock();
			}
			if (module != null) {
				module.setStatementsElaborated(true);
				module.storeOrUpdate();

				if (ENABLE_NEW_INDICES) {
					logger.info("IGManager: Indexing %s", fSignature);
					index(module.getStructure(), module.getDBID());
				}
			}

			fTodo.remove(fSignature);

			if (ENABLE_MULTITHREADING) {
				fLock.unlock();
			}
		}
	}

	private int getNumTodo() {
		if (ENABLE_MULTITHREADING) {
			fLock.lock();
		}
		int n = fTodo.size();
		if (ENABLE_MULTITHREADING) {
			fLock.unlock();
		}
		return n;
	}

	private boolean isCanceled() {
		if (fMonitor == null) {
			return false;
		}
		return fMonitor.isCanceled();
	}

	private void initIGBuild() {
		fTodo = new HashSet<String>();

		if (ENABLE_MULTITHREADING) {

			fExecutorService = Executors.newFixedThreadPool(NUM_THREADS);

			fModulesBeingCreated = new HashSet<String>();
			fLock = new ReentrantLock();
			fModuleCreatedCond = fLock.newCondition();
		} else {
			fTodoStack = new ZStack<BuildNodeJob>();
		}
	}

	private void runIGBuild() {
		if (ENABLE_MULTITHREADING) {
			while (!isCanceled() && getNumTodo() > 0) {
				logger.info("IGManager: waiting, %d jobs todo...", getNumTodo());
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					el.logException(e);
				}
			}

			try {
				if (isCanceled()) {
					fExecutorService.shutdownNow();
				} else {
					fExecutorService.shutdown();
				}
				fExecutorService.awaitTermination(7, TimeUnit.DAYS);
			} catch (InterruptedException e) {
				el.logException(e);
			}

		} else {
			while (!fTodoStack.isEmpty()) {

				if (isCanceled()) {
					logger.info("Canceled.");
					break;
				}

				BuildNodeJob job = fTodoStack.pop();
				job.run();
			}
		}
	}

	public IGModule buildIG(Toplevel aTL, IZamiaMonitor aMonitor, int aTotalUnits) {

		IGModule module = null;

		fMonitor = aMonitor;

		DMUID duuid = fDUM.getArchDUUID(aTL);

		if (duuid == null) {
			logger.error("IGManager: Failed to find toplevel %s.", aTL);
			fERM.addError(new ZamiaException(ExCat.INTERMEDIATE, true, "IGManager: failed to find toplevel " + aTL, aTL.getLocation()));
			return null;
		}

		//fTotalUnits = aTotalUnits;
		fNumDone = 0;

		String signature = IGInstantiation.computeSignature(duuid, null);

		initIGBuild();

		module = getOrCreateIGModule(new ToplevelPath(aTL, new PathName("")), null, duuid, signature, null, true, aTL.getLocation());

		runIGBuild();

		ZCJInterpreter zti = fZPrj.getZCJ();

		if (zti.hasCommand(PYTHON_BUILD_ELABORATE_CMD)) {

			try {
				String cmd = PYTHON_BUILD_ELABORATE_CMD + " " + duuid.getLibId() + " " + duuid.getId() + " " + duuid.getArchId();
				logger.info("IGManager: python eval '%s'", cmd);
				zti.eval(cmd);
			} catch (Throwable e) {
				el.logException(e);
			}
		}

		// too volatile to make sense
		//		if (fLastWorked < fTotalUnits) {
		//			worked(fTotalUnits - fLastWorked);
		//		}

		return module;
	}

	@SuppressWarnings("unchecked")
	public IGModule getOrCreateIGModule(ToplevelPath aPath, DMUID aParentDUUID, DMUID aDUUID, String aSignature, ArrayList<Pair<String, IGStaticValue>> aActualGenerics,
			boolean aElaborateStatements, SourceLocation aLocation) {

		if (ENABLE_MULTITHREADING) {
			fLock.lock();

			while (!isCanceled() && fModulesBeingCreated.contains(aSignature)) {

				try {
					fModuleCreatedCond.await();
				} catch (InterruptedException e) {
					el.logException(e);
				}

			}
		}

		IGModule module = null;

		long mid = fZDB.getIdx(MODULE_IDX, aSignature);

		if (mid != 0) {

			module = (IGModule) fZDB.load(mid);

		} else {

			try {
				IDesignModule dm = fDUM.getDM(aDUUID);
				if (dm != null) {

					if (ENABLE_MULTITHREADING) {
						fModulesBeingCreated.add(aSignature);
						fLock.unlock();
					}

					module = new IGModule(aPath, aDUUID, dm.getLocation(), fZDB);

					int n = aActualGenerics != null ? aActualGenerics.size() : 0;
					for (int i = 0; i < n; i++) {
						module.addActualGeneric(aActualGenerics.get(i).getSecond());
					}

					dm.computeIG(this, module);

					if (ENABLE_MULTITHREADING) {
						fLock.lock();
					}
					mid = module.storeOrUpdate();
					fZDB.putIdx(IGManager.MODULE_IDX, aSignature, mid);

					if (ENABLE_MULTITHREADING) {
						fModulesBeingCreated.remove(aSignature);
						fModuleCreatedCond.signal();
					}
				} else {
					fERM.addError(new ZamiaException(ExCat.INTERMEDIATE, true, "IGManager: failed to find " + aDUUID, aLocation));
				}
			} catch (ZamiaException e) {
				el.logException(e);
				fERM.addError(new ZamiaException(ExCat.INTERMEDIATE, true, e.getMessage(), e.getLocation()));
			}
		}

		if (module != null) {

			if (!module.isStatementsElaborated() && aElaborateStatements) {

				String uid = aDUUID.getUID();
				fZDB.index(SIGNATURES_IDX, uid, aSignature);
				
				if (!fTodo.contains(aSignature)) {
					fTodo.add(aSignature);

					BuildNodeJob job = new BuildNodeJob(this, aPath, aParentDUUID, aDUUID, aSignature, aActualGenerics, aLocation);

					if (ENABLE_MULTITHREADING) {
						fExecutorService.execute(job);
					} else {
						fTodoStack.push(job);
					}
				}
			}
		}

		if (aParentDUUID != null) {
			String uid = aDUUID.getUID();
			fZDB.index(INSTANTIATORS_IDX, uid, aParentDUUID);
		}

		if (ENABLE_MULTITHREADING) {
			fLock.unlock();
		}

		return module;
	}

	public IGModule findModule(String aSignature) {

		long id = fZDB.getIdx(MODULE_IDX, aSignature);
		if (id == 0) {
			return null;
		}

		IGModule module = (IGModule) fZDB.load(id);

		return module;
	}

	public ZamiaProject getProject() {
		return fZPrj;
	}

	public IGModule findModule(Toplevel aTL) {

		DMUID duuid = fDUM.getArchDUUID(aTL);

		if (duuid == null)
			return null;

		String signature = IGInstantiation.computeSignature(duuid, null);

		return findModule(signature);
	}

	public IGItem findItem(Toplevel aTL, PathName aPath) {

		IGItem item = null;

		IGModule module = findModule(aTL);

		if (module != null && aPath.getNumSegments() > 0) {

			item = module.getStructure();

			int n = aPath.getNumSegments();
			for (int i = 0; i < n; i++) {

				if (!(item instanceof IGConcurrentStatement))
					return null;

				IGConcurrentStatement cs = (IGConcurrentStatement) item;

				String segment = aPath.getSegment(i);

				IGItem childItem = cs.findChild(segment);

				if (childItem != null) {
					item = childItem;
				} else {
					if (segment != null) {
						return null;
					}
				}
			}
		} else {
			item = module;
		}

		return item;
	}

	@SuppressWarnings("unchecked")
	public HashSetArray<DMUID> findInstantiators(String aUID) {

		long mid = fZDB.getIdx(INSTANTIATORS_IDX, aUID);
		if (mid == 0) {
			return null;
		}

		return (HashSetArray<DMUID>) fZDB.load(mid);
	}

	/**
	 * Rebuild all nodes affected by changes in the given DUs.
	 * 
	 * @param aDUUIDs
	 * @return number of rebuilt nodes
	 */
	@SuppressWarnings("unchecked")
	public int rebuildNodes(HashSetArray<DMUID> aDUUIDs, IZamiaMonitor aMonitor) {

		fMonitor = aMonitor;
		ZamiaProfiler.getInstance().startTimer("IG");

		// figure out affected IG nodes,
		// delete them, invalidate parents

		HashSetArray<String> deleteNodes = new HashSetArray<String>();
		HashSetArray<String> invalidateNodes = new HashSetArray<String>();

		for (DMUID duuid : aDUUIDs) {

			DMUID archDUUID = fDUM.getArchDUUID(duuid);

			if (archDUUID == null) {
				logger.info("IGManager: rebuildNodes(): Warning: couldn't find architecture DUUID for %s", duuid);
				continue;
			}

			duuid = archDUUID;

			String uid = duuid.getUID();

			HashSetArray<String> signatures = (HashSetArray<String>) fZDB.getIdxObj(SIGNATURES_IDX, uid);
			if (signatures != null) {
				
				for (String signature : signatures) {

					if (deleteNodes.add(signature)) {
						logger.info("IGManager: Need to re-elaborate completeley: %s", signature);
					}
				}
			}

			long dbid = fZDB.getIdx(INSTANTIATORS_IDX, uid);
			if (dbid != 0) {
				HashSetArray<DMUID> instantiators = (HashSetArray<DMUID>) fZDB.load(dbid);

				for (DMUID instantiator : instantiators) {

					String uidI = instantiator.getUID();

					HashSetArray<String> signaturesI = (HashSetArray<String>) fZDB.getIdxObj(SIGNATURES_IDX, uidI);

					if (signaturesI != null) {
						for (String signature : signaturesI) {
							if (invalidateNodes.add(signature)) {
								logger.info("IGManager: Need to re-elaborate statements: %s", signature);
							}
						}
					}
				}
			}
		}

		for (String signature : deleteNodes) {

			long dbid = fZDB.getIdx(MODULE_IDX, signature);

			if (dbid == 0) {
				continue;
			}

			IGModule module = (IGModule) fZDB.load(dbid);

			DMUID duuid = module.getDUUID();

			// remove list from instantiators list of all instantiated modules
			removeFromInstantiators(duuid, module.getStructure());

			fZDB.delIdx(MODULE_IDX, signature);
			fZDB.delIdx(INSTANTIATORS_IDX, duuid.getUID());

			fZDB.delete(dbid);
		}

		initIGBuild();

		for (String signature : invalidateNodes) {

			long dbid = fZDB.getIdx(MODULE_IDX, signature);

			if (dbid == 0) {
				continue;
			}

			IGModule module = (IGModule) fZDB.load(dbid);

			module.updateInstantiations(deleteNodes);
		}

		// we might have deleted a toplevel node, so make sure
		// we re-build it

		BuildPath bp = fZPrj.getBuildPath();
		for (Toplevel toplevel : bp.toplevels()) {

			if (isCanceled()) {
				logger.info("ZamiaProjectBuilder: ZamiaProjectBuilder: Canceled.");
				break;
			}

			DMUID duuid = fDUM.getArchDUUID(toplevel);

			if (duuid == null) {
				logger.error("IGManager: Failed to find toplevel %s.", toplevel);
				fERM.addError(new ZamiaException(ExCat.INTERMEDIATE, true, "IGManager: failed to find toplevel " + toplevel, toplevel.getLocation()));
				continue;
			}

			String signature = IGInstantiation.computeSignature(duuid, null);

			getOrCreateIGModule(new ToplevelPath(toplevel, new PathName("")), null, duuid, signature, null, true, toplevel.getLocation());

			//			ZamiaTclInterpreter zti = fZPrj.getZTI();
			//
			//			if (zti.hasCommand(TCL_BUILD_ELABORATE_CMD)) {
			//
			//				try {
			//					String cmd = TCL_BUILD_ELABORATE_CMD + " " + duuid.getLibId() + " " + duuid.getId() + " " + duuid.getArchId();
			//					logger.info("IGManager: tcl eval '%s'", cmd);
			//					zti.eval(cmd);
			//				} catch (TclException e) {
			//					el.logException(e);
			//				}
			//			}
		}

		// finally: run the build.

		//fTotalUnits = 1000;
		fNumDone = 0;

		runIGBuild();

		ZamiaProfiler.getInstance().stopTimer("IG");

		return deleteNodes.size() + invalidateNodes.size();
	}

	@SuppressWarnings("unchecked")
	private void removeFromInstantiators(DMUID aDUUID, IGStructure aStructure) {

		for (IGConcurrentStatement stmt : aStructure.getStatements()) {

			if (stmt instanceof IGInstantiation) {

				IGInstantiation inst = (IGInstantiation) stmt;

				DMUID duuid = inst.getChildDUUID();

				String uid = duuid.getUID();

				long mid = fZDB.getIdx(INSTANTIATORS_IDX, uid);
				if (mid != 0) {
					HashSetArray<DMUID> instantiators = (HashSetArray<DMUID>) fZDB.load(mid);

					instantiators.remove(aDUUID);
					fZDB.update(mid, instantiators);
				}

			} else if (stmt instanceof IGStructure) {

				IGStructure struct = (IGStructure) stmt;

				removeFromInstantiators(aDUUID, struct);

			}
		}
	}

	static class NodeCounter implements IGStructureVisitor {

		public int fNumNodes = 0;

		public void visit(IGStructure aStructure, PathName aPath) {
			fNumNodes++;
			//logger.debug("IGManager: Counting nodes. Node %4d %s is %s", fNumNodes, aPath, aStructure);
		}

		public int getNumNodes() {
			return fNumNodes;
		}
	}

	public int countNodes(DMUID aDUUID, int aMaxDepth) throws ZamiaException {

		logger.info("IGManager: Counting nodes in %s", aDUUID);

		String signature = IGInstantiation.computeSignature(aDUUID, null);

		IGModule module = findModule(signature);
		if (module == null) {
			return 0;
		}

		NodeCounter counter = new NodeCounter();
		module.accept(counter, aMaxDepth);

		return counter.getNumNodes();
	}

	public int countNodes(DMUID aDUUID) throws ZamiaException {
		return countNodes(aDUUID, Integer.MAX_VALUE);
	}

	public IGPackage findPackage(String aLibId, String aPkgId, SourceLocation aLocation) {

		if (ENABLE_MULTITHREADING) {
			fLock.lock();
		}

		DMUID duuid = new DMUID(LUType.Package, aLibId, aPkgId, null);

		String uid = duuid.getUID();

		IGPackage pkg = null;

		long id = fZDB.getIdx(PACKAGE_IDX, uid);
		if (id != 0) {
			pkg = (IGPackage) fZDB.load(id);
		}

		if (pkg == null) {

			IDesignModule dm = null;
			try {
				dm = fDUM.getDM(duuid);
			} catch (ZamiaException e) {
				el.logException(e);
			}
			if (dm != null) {

				logger.info("IGManager: building IGPackage for %s", duuid);

				SourceLocation location = dm.getLocation();

				pkg = new IGPackage(duuid, location, fZDB);

				// store it right away to avoid recursion
				id = pkg.store();
				fZDB.putIdx(IGManager.PACKAGE_IDX, duuid.getUID(), id);

				dm.computeIG(this, pkg);
			}
		}

		if (ENABLE_MULTITHREADING) {
			fLock.unlock();
		}

		return pkg;
	}

	static class ObjectCounter implements IGStructureVisitor {

		public int fNumObjects = 0;

		public void visit(IGStructure aStructure, PathName aPath) {
//			int debugInitial = fNumObjects;

			visit(aStructure.getContainer(), aPath.getNumSegments() == 0);

			for (IGConcurrentStatement stmt : aStructure.getStatements()) {
				if (stmt instanceof IGProcess) {
					IGProcess process = (IGProcess) stmt;

					visit(process.getContainer(), false);
				}
			}

//			logger.debug("IGManager: Counting objects. %4d objects contained in %s.", fNumObjects - debugInitial, aPath.toString().isEmpty() ? "'Toplevel'" : aPath);
		}

		private void visit(IGContainer container, boolean isToplevel) {
			if (isToplevel) {
				fNumObjects += container.getNumInterfaces();
			}

			int n = container.getNumLocalItems();
			for (int i = 0; i < n; i++) {
				IGContainerItem localItem = container.getLocalItem(i);
				if (localItem instanceof IGObject) {
					IGObject object = (IGObject) localItem;

					if (object.getCat() == IGObjectCat.SIGNAL && object.getDirection() == IGObject.OIDir.NONE) {
						fNumObjects++;
					} else if (object.getCat() == IGObjectCat.VARIABLE) {
						fNumObjects++;
					}
				}
			}
		}

		public int getNumObjects() {
			return fNumObjects;
		}
	}

	public int countObjects(DMUID aDUUID) throws ZamiaException {

		logger.info("IGManager: Counting objects in %s", aDUUID);

		String signature = IGInstantiation.computeSignature(aDUUID, null);

		IGModule module = findModule(signature);
		if (module == null) {
			return 0;
		}

		ObjectCounter counter = new ObjectCounter();
		module.accept(counter, Integer.MAX_VALUE);

		return counter.getNumObjects();
	}

    /**
     * Counts conditions in the specified range <tt>aStart</tt>:<tt>aEnd</tt> of the specified source file <tt>aPath</tt>
     * in the given design unit.
     *
     * @param aDUUID    ID of the design unit where to count conditions in
     * @param aPath     source file where to count conditions in
     * @param aStart    starting line, inclusive
     * @param aEnd      ending line, inclusive
     * @return      number of atomic conditions in the specified range of the source file
     * @throws ZamiaException
     */
    @SuppressWarnings("UnusedDeclaration")
    public int countConditionsInRange(DMUID aDUUID, String aPath, int aStart, int aEnd) throws ZamiaException {
        return createConditionCounter(aDUUID).getNumConditionsInRange(aPath, aStart, aEnd);
	}

    /**
     * Counts all the conditions in the whole design under the given DUUID.
     *
     * @param aDUUID    ID of the design unit where to count conditions in
     * @return          total number of atomic conditions in the given Design Unit
     * @throws ZamiaException
     */
    public int countConditions(DMUID aDUUID) throws ZamiaException {
        return createConditionCounter(aDUUID).getNumConditions();
    }

	private ConditionCounter createConditionCounter(DMUID aDUUID) throws ZamiaException {

		logger.info("IGManager: Counting conditions in %s", aDUUID);

		String signature = IGInstantiation.computeSignature(aDUUID, null);

		IGModule module = findModule(signature);
		if (module == null) {
			return null;
		}

		ConditionCounter counter = new ConditionCounter();
		module.accept(counter, Integer.MAX_VALUE);

		return counter;
	}

	/*
	 * access to indexed information
	 */

	public Iterator<String> getSignalIdIterator(long aDBID) {
		return fStructSignalIdx.getKeyIterator(aDBID);
	}

	public Iterator<IGInstMapInfo> getInstIterator(long aDBID) {
		return fStructInstIdx.getValueIterator(aDBID);
	}

}
