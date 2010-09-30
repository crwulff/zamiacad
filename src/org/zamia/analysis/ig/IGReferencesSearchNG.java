/*
 * Copyright 2008-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */
package org.zamia.analysis.ig;

import java.util.ArrayList;

import org.zamia.ExceptionLogger;
import org.zamia.Toplevel;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGConcurrentStatement;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGContainerItem;
import org.zamia.instgraph.IGInstantiation;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGItemAccess;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGMapping;
import org.zamia.instgraph.IGModule;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationObject;
import org.zamia.instgraph.IGProcess;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSequentialStatement;
import org.zamia.instgraph.IGStructure;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.util.HashSetArray;
import org.zamia.util.PathName;
import org.zamia.util.ZStack;
import org.zamia.vg.VGBox;
import org.zamia.vg.VGSignal;
import org.zamia.vg.VisualGraph;


/**
 * 
 * @author aoun and guenter
 * 
 */
public class IGReferencesSearchNG {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private ZamiaProject fZPrj;

	private IGManager fIGM;

	private HashSetArray<SearchJob> fJobs;

	private Toplevel fToplevel;

	static class SearchJob {

		private IGObject fObject;

		private PathName fPath;

		private IGConcurrentStatement fScope;

		private boolean fGlobal = false;

		public SearchJob(IGObject aObject, PathName aPath, IGConcurrentStatement aScope) {
			fObject = aObject;
			fPath = aPath;
			fScope = aScope;
		}

		public IGConcurrentStatement getScope() {
			return fScope;
		}

		public IGObject getObject() {
			return fObject;
		}

		public PathName getPath() {
			return fPath;
		}

		@Override
		public boolean equals(Object aObj) {

			if (!(aObj instanceof SearchJob)) {
				return false;
			}

			if (aObj == this)
				return true;

			SearchJob sj = (SearchJob) aObj;

			if (fScope != sj.getScope())
				return false;

			if (!fPath.equals(sj.getPath()))
				return false;

			if (fObject != sj.getObject())
				return false;

			return true;
		}

		@Override
		public int hashCode() {
			return toString().hashCode();
		}

		@Override
		public String toString() {
			return fPath + ":" + fScope + fObject;
		}

		public boolean isGlobal() {

			if (fGlobal) {
				return true;
			}

			if (fPath.getNumSegments() == 0)
				return true;

			OIDir dir = fObject.getDirection();
			if (dir == OIDir.NONE) {
				return true;
			}

			return false;
		}

		public void setGlobal(boolean aGlobal) {
			fGlobal = aGlobal;
		}
	}

	static class VGSearchJob {

		private final IGObject fObject;

		private final PathName fPath;

		private final VGSignal fSignal;

		private final IGConcurrentStatement fScope;

		public VGSearchJob(IGObject aObject, PathName aPath, IGConcurrentStatement aScope, VGSignal aSignal) {
			fObject = aObject;
			fPath = aPath;
			fScope = aScope;
			fSignal = aSignal;
		}

		public IGObject getObject() {
			return fObject;
		}

		public PathName getPath() {
			return fPath;
		}

		public VGSignal getSignal() {
			return fSignal;
		}

		public IGConcurrentStatement getScope() {
			return fScope;
		}

		@Override
		public boolean equals(Object aObj) {

			if (!(aObj instanceof SearchJob)) {
				return false;
			}

			if (aObj == this)
				return true;

			SearchJob sj = (SearchJob) aObj;

			if (!fPath.equals(sj.getPath()))
				return false;

			if (fObject != sj.getObject())
				return false;

			if (fScope != sj.getScope())
				return false;

			return true;
		}

		@Override
		public int hashCode() {
			return toString().hashCode();
		}

		@Override
		public String toString() {
			return fPath + ":" + fObject;
		}

	}

	public IGReferencesSearchNG(ZamiaProject aPrj) {
		fZPrj = aPrj;
		fIGM = fZPrj.getIGM();
	}

	public VisualGraph search(IGObject aItem, ToplevelPath aPath, boolean aSearchUpward, boolean aSearchDownward) {

		logger.debug("IGObjectReferenceSearch: search(): start. item=%s, path=%s, searchUpward=%b, searchDownward=%b", aItem, aPath, aSearchUpward, aSearchDownward);

		fJobs = new HashSetArray<SearchJob>();
		fToplevel = aPath.getToplevel();

		SearchJob job = findLocalDeclarationScope(aItem, aPath.getPath());

		if (job == null) {
			return null;
		}

		fJobs.add(job);

		logger.debug("IGObjectReferenceSearch: search(): successfully created a search job for this request.");

		if (aSearchUpward) {
			findOriginalDeclarations();
		}

		try {
			return searchReferences(aSearchDownward);
		} catch (ZamiaException e) {
			el.logException(e);
		}

		return null;
	}

	private void findOriginalDeclarations() {

		logger.debug("IGObjectReferenceSearch: findOriginalDeclarations(): start. Will make all search jobs global.");

		// loop iterates over all search jobs,
		// tries to make them global

		while (true) {

			SearchJob job = null;
			boolean found = false;
			int nJobs = fJobs.size();
			for (int i = 0; i < nJobs; i++) {
				job = fJobs.get(i);
				if (job == null) {
					continue;
				}
				logger.debug("IGObjectReferenceSearch: findOriginalDeclarations(): job '%s' is global: %b.", job, job.isGlobal());
				if (!job.isGlobal()) {
					found = true;
					break;
				}
			}

			if (!found) {
				logger.debug("IGObjectReferenceSearch: findOriginalDeclarations(): All jobs are global now.");
				break;
			}

			fJobs.remove(job);

			logger.debug("IGObjectReferenceSearch: findOriginalDeclarations(): Will make global: '%s'.", job);

			PathName path = job.getPath().getParent();

			IGItem item = fIGM.findItem(fToplevel, path);

			logger.debug("IGObjectReferenceSearch: findOriginalDeclarations(): path=%s, item=%s", path, item);

			if (item instanceof IGInstantiation) {

				IGInstantiation inst = (IGInstantiation) item;

				boolean foundOriginal = false;

				int n = inst.getNumMappings();
				for (int i = 0; i < n; i++) {

					IGMapping mapping = inst.getMapping(i);

					IGOperation formal = mapping.getFormal();

					HashSetArray<IGItemAccess> accessedItems = new HashSetArray<IGItemAccess>();
					formal.computeAccessedItems(false, job.getObject(), null, 0, accessedItems);

					int m = accessedItems.size();
					if (m > 0) {

						IGOperation actual = mapping.getActual();

						HashSetArray<IGItemAccess> accessedItems2 = new HashSetArray<IGItemAccess>();
						actual.computeAccessedItems(false, null, null, 0, accessedItems2);

						int l = accessedItems2.size();
						for (int j = 0; j < l; j++) {
							IGItemAccess ai = accessedItems2.get(j);

							IGItem item2 = ai.getItem();
							if (item2 instanceof IGObject) {

								IGObject obj2 = (IGObject) item2;

								SearchJob job2 = findLocalDeclarationScope(obj2, path);
								if (job2 != null) {
									fJobs.add(job2);
									foundOriginal = true;
								} else {
									logger.error("IGObjectReferenceSearch: findOriginalDeclarations(): job2 is null!");
								}
							}
						}
					}
				}

				if (!foundOriginal) {
					// happens if 'original' was an literal
					job.setGlobal(true);
					fJobs.add(job);
				}

			} else if (item instanceof IGStructure) {
				logger.debug("IGObjectReferenceSearch: findOriginalDeclarations(): item is an IGStructure -> continue at parent.");

				fJobs.add(new SearchJob(job.getObject(), path, (IGStructure) item));

			} else if (item instanceof IGModule) {

				int n = path.getNumSegments();
				if (n > 0 && path.getSegment(n - 1) == null) {

					logger.debug("IGObjectReferenceSearch: findOriginalDeclarations(): item is an IGModule and last segment is null -> continue at parent.");

					fJobs.add(new SearchJob(job.getObject(), path.getParent(), job.getScope()));
				} else {
					logger.debug("IGObjectReferenceSearch: findOriginalDeclarations(): item is an IGModule, marking job as global (giving up).");
					job.setGlobal(true);
					fJobs.add(job);
				}

			} else {
				logger.debug("IGObjectReferenceSearch: findOriginalDeclarations(): unknown item, marking job as global (giving up).");
				job.setGlobal(true);
				fJobs.add(job);
			}
		}
	}

	private VisualGraph searchReferences(boolean aSearchDownward) throws ZamiaException {

		VisualGraph vg = new VisualGraph(fToplevel, fZPrj);

		ZStack<VGSearchJob> stack = new ZStack<VGSearchJob>();

		int nJobs = fJobs.size();
		for (int i = 0; i < nJobs; i++) {
			SearchJob job = fJobs.get(i);

			IGObject obj = job.getObject();

			PathName path = job.getPath();

			VGBox box = vg.getOrCreateBox(path, null);

			VGSignal s = box.getOrCreateSignal(obj);

			VGSearchJob vgj = new VGSearchJob(obj, path, job.getScope(), s);
			stack.push(vgj);

			//			ReferenceSite site = new ReferenceSite(obj.toString(), location, 0, RefType.Declaration, job.getPath(), obj);
			//
			//			globalResult.add(job.getPath(), site, obj);
		}

		while (!stack.isEmpty()) {

			VGSearchJob job = stack.pop();

			IGConcurrentStatement scope = job.getScope();
			IGObject object = job.getObject();
			PathName path = job.getPath();
			VGSignal signal = job.getSignal();

			if (scope instanceof IGStructure) {

				IGStructure struct = (IGStructure) scope;

				int n = struct.getNumStatements();
				for (int i = 0; i < n; i++) {

					IGConcurrentStatement stmt = struct.getStatement(i);

					String label = stmt.getLabel();

					PathName localPath = label != null ? path.append(label) : path;

					stack.push(new VGSearchJob(object, localPath, stmt, signal));
				}

			} else if (scope instanceof IGInstantiation) {
				IGInstantiation inst = (IGInstantiation) scope;

				// String label = inst.getLabel();
				// PathName localPath = path.append(label);

				IGModule module = fIGM.findModule(inst.getSignature());

				int n = inst.getNumMappings();
				for (int i = 0; i < n; i++) {

					IGMapping mapping = inst.getMapping(i);

					IGOperation formal = mapping.getFormal();

					OIDir dir = OIDir.NONE;
					try {
						dir = formal.getDirection();
					} catch (ZamiaException e) {
						el.logException(e);
					}

					boolean leftSide = dir == OIDir.OUT || dir == OIDir.INOUT;

					IGOperation actual = mapping.getActual();

					HashSetArray<IGItemAccess> accessedItems = new HashSetArray<IGItemAccess>();
					actual.computeAccessedItems(leftSide, object, null, 0, accessedItems);

					int m = accessedItems.size();
					//					for (int j = 0; j < m; j++) {
					//						IGItemAccess ai = accessedItems.get(j);
					//						addResult(globalResult, path.getParent(), ai);
					//					}

					if (m > 0 && aSearchDownward && module != null) {

						HashSetArray<IGObject> objs = new HashSetArray<IGObject>();
						findObjects(formal, objs);

						int l = objs.size();
						for (int k = 0; k < l; k++) {

							IGObject obj = objs.get(k);

							VGBox box = vg.getOrCreateBox(path, null);

							VGSignal s = box.getOrCreateSignal(obj);

							VGSearchJob vgj = new VGSearchJob(obj, path, module.getStructure(), s);

							signal.connect(s);
							s.connectExternal(signal);

							stack.push(vgj);
						}
					}
				}

			} else if (scope instanceof IGProcess) {
				IGProcess proc = (IGProcess) scope;

				String id = proc.getLabel();
				if (id == null) {
					id = "proc#" + proc.hashCode();
					path = path.append(id);
				}

				VGBox procBox = null; // created on demand

				IGSequenceOfStatements sos = proc.getSequenceOfStatements();

				int n = sos.getNumStatements();

				for (int i = 0; i < n; i++) {

					IGSequentialStatement stmt = sos.getStatement(i);
					
					HashSetArray<IGItemAccess> accessedItems = new HashSetArray<IGItemAccess>();
					stmt.computeAccessedItems(object, null, 0, accessedItems);

					int m = accessedItems.size();

					if (m > 0) {

						if (procBox == null) {
							procBox = vg.getOrCreateBox(path, proc);
						}
						
						VGBox box = procBox.createChild(stmt.toString(), stmt.getDBID(), stmt.computeSourceLocation(), path.append("stmt#"+i));

						for (int j = 0; j < m; j++) {
							IGItemAccess ai = accessedItems.get(j);
							//addResult(globalResult, path, ai);

							IGItem item = ai.getItem();

							if (!(item instanceof IGObject)) {
								continue;
							}

							IGObject obj = (IGObject) item;

							VGSignal s = box.getOrCreateSignal(obj);

							signal.connect(s);
						}
					}
				}
			}
		}

		return vg;
	}

	private void findObjects(IGOperation aOperation, HashSetArray<IGObject> aObjs) {
		if (aOperation == null)
			return;

		if (aOperation instanceof IGOperationObject) {
			IGObject obj = ((IGOperationObject) aOperation).getObject();
			aObjs.add(obj);
		}

		int n = aOperation.getNumOperands();
		for (int i = 0; i < n; i++) {
			findObjects(aOperation.getOperand(i), aObjs);
		}
	}

	private SearchJob findLocalDeclarationScope(IGObject aObject, PathName aPath) {

		logger.debug("IGObjectReferenceSearch: findLocalDeclarationScope(): start. object=%s, path=%s", aObject, aPath);

		PathName path = aPath;
		IGObject object = aObject;

		while (true) {

			IGItem item = fIGM.findItem(fToplevel, path);

			logger.debug("IGObjectReferenceSearch: findLocalDeclarationScope(): path=%s corresponds to IGItem %s", path, item);

			IGContainer container = null;
			IGConcurrentStatement scope = null;

			if (item instanceof IGStructure) {

				IGStructure struct = (IGStructure) item;
				container = struct.getContainer();
				scope = struct;

			} else if (item instanceof IGModule) {

				IGStructure struct = ((IGModule) item).getStructure();
				container = struct.getContainer();
				scope = struct;

			}

			logger.debug("IGObjectReferenceSearch: findLocalDeclarationScope(): container=%s scope=%s", container, scope);

			if (container != null) {
				ArrayList<IGContainerItem> localItems = container.findLocalItems(object.getId());

				logger.debug("IGObjectReferenceSearch: findLocalDeclarationScope(): localItems=%s", localItems);

				if (localItems != null && localItems.size() > 0) {
					return new SearchJob(object, path, scope);
				}
			}
			if (item instanceof IGModule) {
				logger.debug("IGObjectReferenceSearch: findLocalDeclarationScope(): item is an IGModule => break");
				break;
			}

			path = path.getNullParent();
		}

		logger.debug("IGObjectReferenceSearch: findLocalDeclarationScope(): return null.");
		return null;
	}
}
