/*
 * Copyright 2008-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */
package org.zamia.analysis.ig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.zamia.ASTNode;
import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.SourceLocation2AST;
import org.zamia.instgraph.Scope;
import org.zamia.instgraph.IGConcurrentStatement;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGContainerItem;
import org.zamia.instgraph.IGInstantiation;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGItemAccess;
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGMapping;
import org.zamia.instgraph.IGModule;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGObject.IGObjectCat;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationObject;
import org.zamia.instgraph.IGProcess;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGStructure;
import org.zamia.util.HashSetArray;
import org.zamia.util.Pair;
import org.zamia.util.PathName;
import org.zamia.util.ZStack;

/**
 * 
 * @author aoun and guenter
 * 
 */
public class IGReferencesSearch {

	 /* NB! debug=false removes titles from search results and breaks their comparison (if different signals assigned in the
	  * same place, second will overwrite the first)!*/
	static boolean debug = false;

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public final ZamiaProject fZPrj;

	private IGManager fIGM;

	HashSetArray<SearchJob> fJobs;

	protected boolean fWritersOnly;
	protected boolean fReadersOnly;
	protected boolean fSearchUpward;
	protected boolean fSearchDownward;

	public static class SearchJob {

		private IGObject fObject;

		private ToplevelPath fPath;

		private IGConcurrentStatement fScope;

		private boolean fGlobal = false;

		public SearchJob(IGObject aObject, ToplevelPath aPath, IGConcurrentStatement aScope) {
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

		public ToplevelPath getPath() {
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

			PathName path = fPath.getPath();
			if (path.getNumSegments() == 0)
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

	public IGReferencesSearch(ZamiaProject aPrj) {
		fZPrj = aPrj;
		fIGM = fZPrj.getIGM();
		
	}

	public ReferenceSearchResult search(IGObject aItem, ToplevelPath aPath) {

		logger.debug("IGObjectReferenceSearch: search(): start. item=%s, path=%s, searchUpward=%b, searchDownward=%b", aItem, aPath, fSearchUpward, fSearchDownward);

		fJobs = new HashSetArray<SearchJob>();
		SearchJob job = findLocalDeclarationScope(fIGM, aItem, aPath);

		if (job == null) {
			return null;
		}

		fJobs.add(job);

		logger.debug("IGObjectReferenceSearch: search(): successfully created a search job for this request.");

		if (fSearchUpward) {
			findOriginalDeclarations();
		}

		return searchReferences();
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

			ToplevelPath path = job.getPath().getParent();

			IGItem item = fIGM.findItem(path.getToplevel(), path.getPath());

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

								SearchJob job2 = findLocalDeclarationScope(fIGM, obj2, path);
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

				PathName pn = path.getPath();
				int n = pn.getNumSegments();
				if (n > 0 && pn.getSegment(n - 1) == null) {

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

	/**This is an extension to arguments, supplied to computeAccessedItems. 
	 * Currently, only SequentialAssignment needs the extension, for signal through search.*/
	public class AccessedItems extends HashSetArray<IGItemAccess> {
		
		/**Collection of signals connected by assignment*/
		final ToplevelPath path;
		AccessedItems(ToplevelPath path) {
			this.path = path;
		}
		public boolean add(IGItemAccess item) {
			if (addResult(path, item)) {
				size++;
				return true;
			}
			return false;
		}
		
		/**Used by AccesseItems user to see if items were added.
		 * TODO: it seems that it would be more convenient if computeAccessedItems would return boolean instead.*/
		public int size = 0;
		public int size() {
			return size;
		}
		
	}
	
	IGSearchResultBuilder resultBuilder;
	
	IGSearchResultBuilder createResultBuilder(ZamiaProject fZPrj) {
		return new IGSearchResultBuilder(fZPrj);
	}
	
	ReferenceSearchResult searchReferences() {

		resultBuilder = createResultBuilder(fZPrj);
		
		ZStack<SearchJob> stack = new ZStack<SearchJob>();

		int nJobs = fJobs.size();
		for (int i = 0; i < nJobs; i++) {
			SearchJob job = fJobs.get(i);

			IGObject obj = job.getObject();

//			if (!fWritersOnly && !fReadersOnly) {
//				addResult(obj.toString(), obj.computeSourceLocation(), RefType.Declaration, job.getPath(), obj);
//			}
			
			if (createEntryResult(obj, job.getPath()))
				stack.push(job);
		}
		fJobs.clear();

		while (!stack.isEmpty()) {

			SearchJob job = stack.pop();

			IGConcurrentStatement scope = job.getScope();
			IGObject object = job.getObject();
			ToplevelPath path = job.getPath();
			if (debug) logger.info(" Job: " + path + " " + object + " in " + scope.getClass().getName() );

			if (scope instanceof IGStructure) {

				IGStructure struct = (IGStructure) scope;

				int n = struct.getNumStatements();
				for (int i = 0; i < n; i++) {

					IGConcurrentStatement stmt = struct.getStatement(i);

					String label = stmt.getLabel();

					ToplevelPath localPath = label != null ? path.append(label) : job.getPath();

					stack.push(new SearchJob(object, localPath, stmt));
				}

			} else if (scope instanceof IGInstantiation) {
				IGInstantiation inst = (IGInstantiation) scope;

				// String label = inst.getLabel();
				// ToplevelPath localPath = path.append(label);

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

					AccessedItems accessedItems = new AccessedItems(path.getParent());
					actual.computeAccessedItems(leftSide, object, null, 0, accessedItems);

					if (accessedItems.size() > 0 && fSearchDownward && module != null) {

						HashSetArray<IGObject> objs = new HashSetArray<IGObject>();
						findObjects(formal, objs);

						int l = objs.size();
						for (int k = 0; k < l; k++) {

							IGObject obj = objs.get(k);

							stack.push(new SearchJob(obj, path, module.getStructure()));
						}
					}
				}

			} else if (scope instanceof IGProcess) {
				IGProcess proc = (IGProcess) scope;

				IGSequenceOfStatements sos = proc.getSequenceOfStatements();

				sos.computeAccessedItems(object, null, 0, createAccessedItems(path, scope));
			}
		}
		
		return resultBuilder.getResult();
	}

	protected boolean createEntryResult(IGObject obj, ToplevelPath path) {
		addResult(obj.toString(), obj.computeSourceLocation(), RefType.Declaration, path, obj);
		return true;
	}

	AccessedItems createAccessedItems(ToplevelPath path, IGConcurrentStatement scope) {
		return new AccessedItems(path);
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

	private boolean addResult(ToplevelPath aPath, IGItemAccess aAi) {

		RefType refType = RefType.Unknown;
		switch (aAi.getAccessType()) {
		case Call:
			refType = RefType.Call;
			break;
		case Declaration:
			refType = RefType.Declaration;
			break;
		case Instantiation:
			refType = RefType.Instantiation;
			break;
		case Read:
			refType = RefType.Read;
			break;
		case ReadWrite:
			refType = RefType.ReadWrite;
			break;
		case Unknown:
			refType = RefType.Unknown;
			break;
		case Write:
			refType = RefType.Write;
			break;
		}

		if ((!fWritersOnly || refType == RefType.Write) && (!fReadersOnly || refType == RefType.Read)) {
			SourceLocation location = aAi.getLocation();

			String title = location.toString();

			IGObject obj = aAi.getItem() instanceof IGObject ? (IGObject) aAi.getItem() : null;

			// let's see if we can come up with a better title
			if (obj != null) {
				title = obj.toString();
			} else {
				try {
					ASTNode asto = SourceLocation2AST.findNearestASTNode(location, true, fZPrj);

					if (asto != null) {

						title = asto.toString();

					}
				} catch (Throwable e) {
				}
			}

			addResult(title, location, refType, aPath, obj);
			return true;
		}
		
		return false;
	}

	void addResult(String title, SourceLocation location, RefType refType, ToplevelPath aPath, IGObject obj) {
		ReferenceSite rs = new ReferenceSite(title, location, 0, refType, aPath, obj);
		resultBuilder.add(rs, obj);
	}

	public static IGObject asObject(IGItem item) {
		if (item instanceof IGObject) {
			return (IGObject) item;
		} else if (item instanceof IGOperationObject) {
			return ((IGOperationObject) item).getObject();
		}
		return null;
	}
	
	public static SearchJob findLocalDeclarationScope(IGManager aIGM, IGObject aObject, ToplevelPath aPath) {

		logger.debug("IGObjectReferenceSearch: findLocalDeclarationScope(): start. object=%s, path=%s", aObject, aPath);

		ToplevelPath tlp = aPath;
		IGObject object = aObject;

		while (true) {
			
			IGItem item = aIGM.findItem(tlp.getToplevel(), tlp.getPath());

			logger.debug("IGObjectReferenceSearch: findLocalDeclarationScope(): path=%s corresponds to IGItem %s", tlp, item);

			IGContainer container = null;
			IGConcurrentStatement scope = null;

			if (item instanceof Scope) {

				Scope s = (Scope) item;
				container = s.getContainer();
				scope = s.getStructure();

			}

			logger.debug("IGObjectReferenceSearch: findLocalDeclarationScope(): container=%s scope=%s", container, scope);

			if (container != null) {
				ArrayList<IGContainerItem> localItems = container.findLocalItems(object.getId());

				logger.debug("IGObjectReferenceSearch: findLocalDeclarationScope(): localItems=%s", localItems);

				if (localItems != null && localItems.size() > 0) {
					return new SearchJob(object, tlp, scope);
				}
			}
			if (item instanceof IGModule) {
				logger.debug("IGObjectReferenceSearch: findLocalDeclarationScope(): item is an IGModule => break");
				break;
			}

			tlp = tlp.getNullParent();
		}

		logger.debug("IGObjectReferenceSearch: findLocalDeclarationScope(): return null.");
		return null;
	}

	public ReferenceSearchResult search(IGObject aItem, ToplevelPath aPath,
			boolean aSearchUpward, boolean aSearchDownward, boolean aWritersOnly, boolean aReadersOnly
			) {
		fWritersOnly = aWritersOnly;
		fReadersOnly = aReadersOnly;
		fSearchUpward = aSearchUpward;
		fSearchDownward = aSearchDownward;
		return search(aItem, aPath);
	}
}
