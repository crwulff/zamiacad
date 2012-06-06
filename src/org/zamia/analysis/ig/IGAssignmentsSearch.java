package org.zamia.analysis.ig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.zamia.SourceLocation;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ig.IGAssignmentsSearch.RootResult;
import org.zamia.instgraph.IGConcurrentStatement;
import org.zamia.instgraph.IGItemAccess;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperationObject;
import org.zamia.instgraph.IGObject.IGObjectCat;
import org.zamia.instgraph.IGSequentialStatement;
import org.zamia.Utils;
import org.zamia.util.HashSetArray;
import org.zamia.util.Pair;

public class IGAssignmentsSearch extends IGReferencesSearch {

	/**We normally do not need titles for ReferenceSites. But, you may turn it on for debug.
	 * */
	static String debugTitle(IGObject aRefObj) {
		return debug && aRefObj != null ? aRefObj.toString() : null;
	}
	private int maxDepth;
	public IGAssignmentsSearch(ZamiaProject aPrj, int maxDepth) {
		super(aPrj);
		this.maxDepth = maxDepth;
	}

	/**Search depth. Search stops as limits is encountered.*/
	int generation = 0;   

	/**Used to refer and jump to EntryReferenceSite items in the results page.
	 * This default implementation runs search(path) for signals. Variables will have overloaded class that invokes searchReferences(igprocess)*/
	public static abstract class SearchAssignment extends ReferenceSite {
		public SearchAssignment(IGAssignmentsSearch aSearch, SourceLocation assignmentLocation, IGObject aRefObj, ToplevelPath aRefPath) {
			super(debugTitle(aRefObj), assignmentLocation, 0, RefType.Assignment, aRefPath, aRefObj);
			if (aRefObj != null)
				aSearch.nextGeneration.add(this);
		}
		abstract void run();
		public RootResult keyResult;
	}
	
	SearchAssignment newSignalSearch(SourceLocation assignmentLocation, IGObject item, final ToplevelPath path) {
		return new SearchAssignment(this, assignmentLocation, item, path) {
			void run() {
				search(currentObject, currentPath);
			}
		};
	}
	
	// signals and variables to be searched with search context
	ArrayList<SearchAssignment> nextGeneration = new ArrayList<SearchAssignment>();
	
	// TODO: HashSet should be used instead of map. Duplicating the key fields of the value in the key of the map is a waste of memory. 
	// Consider KeyedHashSet from Eclipse core.
	// Moreover, consider if the same object happens in another path. Should it be considered as different signal and searched over?
	// Can/should we store results in ZDB?
	Map<Long, RootResult> completed = new HashMap<Long, RootResult>(); 
	
	/**Object, currently being searched on*/
	IGObject currentObject;
	ToplevelPath currentPath;
	SearchAssignment currentAssignment;
	
	/**
	 * The search results in Assignments rather than Reads and Writes. Found    
	 * Assignments trigger further searches, one per assignment. Assignment keeps    
	 * a ref to the initiated search so that we can navigate to both assignment    
	 * location in the code and further searches it produced. If assigned signal 
	 * was already searched for further assignments, result is just reused.  
	 * This results in a graph.    
	 * */
	public Map<Long, RootResult> assignmentThroughSearch(IGObject aItem, ToplevelPath path, 
			boolean aSearchUpward, boolean aSearchDownward, boolean aWritersOnly, boolean aReadersOnly) {
		
		fWritersOnly = aWritersOnly;
		fReadersOnly = aReadersOnly;
		fSearchUpward = aSearchUpward;
		fSearchDownward = aSearchDownward;

		if (!fReadersOnly && !fWritersOnly)
			throw new IllegalArgumentException("Search through assignments in both directions is not supported. Check either Read Only or Write Only.");
		newSignalSearch(aItem.computeSourceLocation(), aItem, path);
		
		while (!nextGeneration.isEmpty()) {
			ArrayList<SearchAssignment> assignments = nextGeneration;
			nextGeneration = new ArrayList<SearchAssignment>();
			while(!assignments.isEmpty()) {
				
				currentAssignment = Utils.removeLast(assignments);
				
				currentObject = (IGObject) fZPrj.getZDB().load(currentAssignment.getDBID());
	
				currentPath = currentAssignment.getPath().descend();
				if (debug) logger.info("rs.search(" + currentPath + " : " + currentObject + "), " + currentObject.computeSourceLocation());
	
				currentAssignment.run(); // this will produce keyResult with children
	
				if (currentAssignment.keyResult == null) {
					logger.warn("Search " + (completed.size()+1) + " on " + currentPath + " : " + currentObject + " has failed");
					System.err.println("Search " + (completed.size()+1) + " on " + currentPath + " : " + currentObject + " has failed");
				}
					
			}
			
			if (generation++ == maxDepth) {
				for (SearchAssignment a : nextGeneration)
					a.keyResult = new RootResult(completed, (IGObject) fZPrj.getZDB().load(a.getDBID()), a.getPath(), true);
				break;
			}
		}
		
		return completed;
	}
	
	@Override
	AccessedItems createAccessedItems(ToplevelPath path, IGConcurrentStatement scope) {
		return new AccessedThroughItems(path, scope);
	}
	
	public class AccessedThroughItems extends AccessedItems {
		IGConcurrentStatement scope;
		public final Stack<Pair<IGSequentialStatement, HashSetArray<IGItemAccess>>> ifStack = new Stack<Pair<IGSequentialStatement, HashSetArray<IGItemAccess>>>();
		
		AccessedThroughItems(ToplevelPath path, IGConcurrentStatement scope) {
			super(path);
			this.scope = scope;
		}
		
		/**@see org.zamia.instgraph.IGSequentialAssignment#computeAccessedItems*/
		public void scheduleAssignments(HashSetArray<IGItemAccess> list, SourceLocation assignmentLocation) {
			
			for (IGItemAccess ia : list) {
				IGObject obj = asObject(ia.getItem());
				
//				if (useItemLocation)
//					assignmentLocation = obj.computeSourceLocation();  

				SearchAssignment assignment;
				
				if (obj.getCat() == IGObjectCat.VARIABLE) {
					assignment = new SearchAssignment(IGAssignmentsSearch.this, assignmentLocation, obj, path) {
						void run() {
							//TODO: decouple this search assignment from reference to scope, once 
							// run is executed (e.g. scope = null). The null reference is also unnecessary.
							// remove altogether with fDepth.
							fJobs.add(new SearchJob(currentObject // doing object was supplied to us by caller
									, path, scope));
							searchReferences();
							
						}
					};					
				} else if (obj.getCat() == IGObjectCat.SIGNAL)  
					assignment = newSignalSearch(assignmentLocation, obj, path);
				else
					assignment = newConstAssignment(assignmentLocation, path);
				
				scheduleAssignment(assignment);
				
			}
			
			if (list.isEmpty()) {
				scheduleAssignment(newConstAssignment(assignmentLocation, path)); // dummy location
			}
			
		}
		
		private SearchAssignment newConstAssignment(SourceLocation assignmentLocation, final ToplevelPath path) {
			return newSignalSearch(assignmentLocation, null, path);
		}
		
		private void scheduleAssignment(SearchAssignment assignment) {
			if (debug) logger.info (" todo assignment: " + assignment + " : " + assignment.countRefs());
			resultBuilder.add(path, assignment);							
		}

	}

	@Override 
	protected boolean createEntryResult(IGObject obj, ToplevelPath aPath) {
		
		currentAssignment.keyResult = completed.get(obj.getDBID());
		if (currentAssignment.keyResult != null) {
			if (debug) logger.info("will not search assignments of " + currentObject + ". It already has a result, " + obj);
			return false;
		} else {
			currentAssignment.keyResult = new RootResult(completed, obj, aPath, false);
		}
		
		return true;
	}

	public static class RootResult extends ReferenceSite {
		public final int num_prefix;
		public final boolean skippedDueToDepth; // Only a small fraction of results is terminated due to depth. Should we return them in a separate collection instead of marking every one? Just to save a little memory.  
		public RootResult(Map<Long, RootResult> completed, IGObject obj, ToplevelPath aPath, boolean skip) {
			super(debugTitle(obj), obj.computeSourceLocation(), 0, RefType.Declaration, aPath, obj);
			completed.put(obj.getDBID(), this);
			num_prefix = completed.size();
			skippedDueToDepth = skip;
		}
	}
	
	@Override
	IGSearchResultBuilder createResultBuilder(ZamiaProject fZPrj) {
		return new IGSearchResultBuilder(fZPrj) {
			@Override
			public ReferenceSearchResult add(ToplevelPath aPath, ReferenceSearchResult aRSR) {
				if (aRSR instanceof ReferenceSite && ((ReferenceSite) aRSR).getRefType() == RefType.Assignment) {
					currentAssignment.keyResult.add(aRSR);
				}
					
				return aRSR;
			}
		};
	}
	
}
