package org.zamia.analysis.ig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.zamia.SourceLocation;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite;
import org.zamia.instgraph.IGConcurrentStatement;
import org.zamia.instgraph.IGItemAccess;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGObject.IGObjectCat;
import org.zamia.Utils;
import org.zamia.util.HashSetArray;
import org.zamia.util.PathName;

public class IGReferencesSearchThrough extends IGReferencesSearch {

	public IGReferencesSearchThrough(ZamiaProject aPrj) {
		super(aPrj);
	}

	
	/**Used to refer and jump to EntryReferenceSite items in the results page.
	 * This default implementation runs search(path) for signals. Variables will have overloaded class that invokes searchReferences(igprocess)*/
	public static abstract class SearchAssignment extends ReferenceSearchResult {
		public final IGObject fObj;
		public final ToplevelPath fPath;
		
		public SearchAssignment(Collection<SearchAssignment> assignments, SourceLocation assignmentLocation, IGObject aRefObj, ToplevelPath aRefPath) {
			super("<= involves " + aRefObj, assignmentLocation, 0);
			fObj = aRefObj;
			fPath = aRefPath;
			if (aRefObj != null)
				assignments.add(this);
		}
		abstract ReferenceSearchResult run();
		public SearchResultEntrySite keyResult;
	}
	
	SearchAssignment newSignalSearch(SourceLocation assignmentLocation, IGObject item, final ToplevelPath path) {
		return new SearchAssignment(assignments, assignmentLocation, item, path) {
			ReferenceSearchResult run() {
				return search(doingObject, doingPath);
			}
		};
	}
	ArrayList<SearchAssignment> assignments = new ArrayList<SearchAssignment>(); // signals and variables to be searched with context
	Map<IGObject, SearchResultEntrySite> completed = new HashMap<IGObject, SearchResultEntrySite>(); // TODO: consider if the same object happens in another path. Should it be considered as different signal and searched ofver?
	
	/**Object, currently being searched on*/
	IGObject doingObject;
	ToplevelPath doingPath;
	SearchAssignment doingAssignment;
	/**
	 * During the course of execution, new assignments will be created in addition to Read/Write results.
	 * Assignments will initiate new searches. Every search results in entry result. We assign this entry
	 * to the assignment result to that we can jump to both assignment location in code and additional
	 * search (results) it produced.  
	 * */
	public Map<IGObject, SearchResultEntrySite> assignmentThroughSearch(IGObject aItem, ToplevelPath path, 
			boolean aSearchUpward, boolean aSearchDownward, boolean aWritersOnly, boolean aReadersOnly) {
		
		fWritersOnly = aWritersOnly;
		fReadersOnly = aReadersOnly;
		fSearchUpward = aSearchUpward;
		fSearchDownward = aSearchDownward;

		if (!fReadersOnly && !fWritersOnly)
			throw new IllegalArgumentException("Search through assignments in both directions is not supported. Check either Read Only or Write Only.");
		newSignalSearch(aItem.computeSourceLocation(), aItem, path);
		
		while(!assignments.isEmpty()) {
			
			doingAssignment = Utils.removeLast(assignments);
			
			doingObject = doingAssignment.fObj;

			doingPath = doingAssignment.fPath.descend();
			logger.info("rs.search(" + doingPath + " : " + doingObject + "), " + doingObject.computeSourceLocation());

			{
				String newPath = doingPath.getPath().toString();
				//if (!newPath.endsWith(doingObject.getId()))
					newPath += " : " + doingObject.getId();
				char newSeparator = PathName.separator == '.' ? '/' : '.'; // use another separator for prefix. Otherwise, it interferes with path and search result presentation will be broken
				prefix = new PathName((completed.size() == 0 ? "Entry " : "") + "search on " + newPath.replace(PathName.separator, newSeparator) + ""); 			
				
			}
			ReferenceSearchResult rsr = doingAssignment.run(); // this will produce both keyResult and rsr
			if (doingAssignment.keyResult.restOfResults == null) // do not overrwrite results if they were generated previously
				doingAssignment.keyResult.restOfResults = rsr;
				
		}
		return completed;
	}
	
	@Override
	AccessedItems createAccessedItems(ToplevelPath path, IGConcurrentStatement scope) {
		return new AccessedThroughItems(path, scope);
	}
	
	PathName prefix; 
	@Override
	ToplevelPath prefixPathWithSearchName(ToplevelPath path) {
		return new ToplevelPath(doingPath.getToplevel(), prefix.append(path.getPath()));
	}
	
	public class AccessedThroughItems extends AccessedItems {
		IGConcurrentStatement scope;
		
		AccessedThroughItems(ToplevelPath path, IGConcurrentStatement scope) {
			super(path);
			this.scope = scope;
		}
		
		/**Called from IGSequentialAssignement.*/
		public void schedule(HashSetArray<IGItemAccess> list, SourceLocation assignmentLocation) {
			
			for (IGItemAccess ia : list) {
				IGObject obj = (IGObject)ia.getItem();
				SearchAssignment assignment = null;
				
				//TODO: add only in case of signal-through search
				if (obj.getCat() == IGObjectCat.VARIABLE) { 
					assignment = new SearchAssignment(assignments, assignmentLocation, obj, path) {
						ReferenceSearchResult run() {
							fJobs.add(new SearchJob(doingObject, path, scope));
							return searchReferences();
						}
					};					
				} else if (obj.getCat() == IGObjectCat.SIGNAL)  
					assignment = newSignalSearch(assignmentLocation, obj, path);
				
				if (assignment != null) 
					scheduleAssignment(assignment);
				
			}
			
			if (list.isEmpty()) {
				scheduleAssignment(newSignalSearch(assignmentLocation, null, path)); // dummy location
			}
		}
		
		private void scheduleAssignment(SearchAssignment assignment) {
			logger.info(" todo: " + assignment.getLocation() + " : " + assignment.fObj);
			resultBuilder.add(prefixPathWithSearchName(path), assignment);							
		}

	}

	public static class SearchResultEntrySite extends ReferenceSite {
		public ReferenceSearchResult restOfResults = null;
		public SearchResultEntrySite(IGObject aObject, ToplevelPath aPath) {
			super(aObject.toString(), aObject.computeSourceLocation(), 0, RefType.Declaration, aPath, aObject);
			logger.info("new search entry " + Integer.toHexString(System.identityHashCode(this)) + " for " + aObject.getDBID() + "=" + aObject);
		}

	}
	
	@Override 
	boolean createEntryResult(IGObject obj, ToplevelPath aPath) {

		if (completed.containsKey(obj)) {
			logger.info("will not " + doingObject + " already has a result as " + obj);
			doingAssignment.keyResult = completed.get(obj);
			return false;
		}

		doingAssignment.keyResult = new SearchResultEntrySite(obj, aPath);
		addResult(doingAssignment.keyResult, obj);
		completed.put(obj, doingAssignment.keyResult);
		return true;
	}
}
