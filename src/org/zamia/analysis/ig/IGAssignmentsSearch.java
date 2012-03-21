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
	 * NB! removing titles breaks the comparison (difference) between results!
	 * */
	static String debugTitle(IGObject aRefObj) {
		return debug && aRefObj != null ? aRefObj.toString() : null;
	}
	private int maxDepth;
	public IGAssignmentsSearch(ZamiaProject aPrj, int maxDepth) {
		super(aPrj);
		this.maxDepth = maxDepth;
	}

	/**Used to refer and jump to EntryReferenceSite items in the results page.
	 * This default implementation runs search(path) for signals. Variables will have overloaded class that invokes searchReferences(igprocess)*/
	public static abstract class SearchAssignment extends ReferenceSite {
		int fDepth; //TODO: this field is not necessary, once we finished the search. 
		public SearchAssignment(Collection<SearchAssignment> assignments, SourceLocation assignmentLocation, IGObject aRefObj, ToplevelPath aRefPath, int aDepth) {
			super(debugTitle(aRefObj), assignmentLocation, 0, RefType.Assignment, aRefPath, aRefObj);
			if (aRefObj != null)
				assignments.add(this);
			fDepth = aDepth;
		}
		abstract void run();
		public RootResult keyResult;
	}
	
	SearchAssignment newSignalSearch(SourceLocation assignmentLocation, IGObject item, final ToplevelPath path, int depth) {
		return new SearchAssignment(assignments, assignmentLocation, item, path, depth) {
			void run() {
				search(doingObject, doingPath);
			}
		};
	}
	ArrayList<SearchAssignment> assignments = new ArrayList<SearchAssignment>(); // signals and variables to be searched with context
	Map<Long, RootResult> completed = new HashMap<Long, RootResult>(); // TODO: consider if the same object happens in another path. Should it be considered as different signal and searched ofver?
	
	/**Object, currently being searched on*/
	IGObject doingObject;
	ToplevelPath doingPath;
	SearchAssignment doingAssignment;
	
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
		newSignalSearch(aItem.computeSourceLocation(), aItem, path, 0);
		
		while(!assignments.isEmpty()) {
			
			doingAssignment = Utils.removeLast(assignments);
			
			doingObject = (IGObject) fZPrj.getZDB().load(doingAssignment.getDBID());

			doingPath = doingAssignment.getPath().descend();
			if (debug) logger.info("rs.search(" + doingPath + " : " + doingObject + "), " + doingObject.computeSourceLocation());

			doingAssignment.run(); // this will produce keyResult with children

			if (doingAssignment.keyResult == null) {
				logger.warn("Search " + (completed.size()+1) + " on " + doingPath + " : " + doingObject + " has failed");
				System.err.println("Search " + (completed.size()+1) + " on " + doingPath + " : " + doingObject + " has failed");
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
		public void scheduleAssignments(HashSetArray<IGItemAccess> list, boolean useItemLocation, SourceLocation assignmentLocation) {
			//useItemLocation = true;
			
			int newDepth = doingAssignment.fDepth+1;
			
			for (IGItemAccess ia : list) {
				IGObject obj = null;
				if (ia.getItem() instanceof IGOperationObject) {
					obj = ((IGOperationObject) ia.getItem()).getObject(); // cases like A'event
				} else if (ia.getItem() instanceof IGObject) {
					obj = (IGObject) ia.getItem();
				}

//				if (useItemLocation)
//					assignmentLocation = obj.computeSourceLocation();  

				SearchAssignment assignment = null;
				
				if (obj.getCat() == IGObjectCat.VARIABLE) {
					assignment = new SearchAssignment(assignments, assignmentLocation, obj, path, newDepth) {
						void run() {
							//TODO: decouple this search assignment from reference to scope, once 
							// run is executed (e.g. scope = null). The null referenece is also unnecessary.
							// remove altogether with fDepth.
							fJobs.add(new SearchJob(doingObject, path, scope));
							searchReferences();
							
						}
					};					
				} else if (obj.getCat() == IGObjectCat.SIGNAL)  
					assignment = newSignalSearch(assignmentLocation, obj, path, newDepth);
				
				if (assignment != null) 
					scheduleAssignment(assignment);
			}
			
			if (list.isEmpty()) {
				scheduleAssignment(newSignalSearch(assignmentLocation, null, path, newDepth)); // dummy location
			}
			
		}
		
		private void scheduleAssignment(SearchAssignment assignment) {
			if (debug) logger.info (" todo assignment: " + assignment + " : " + assignment.countRefs());
			resultBuilder.add(path, assignment);							
		}

	}

	@Override 
	protected boolean createEntryResult(IGObject obj, ToplevelPath aPath) {

		RootResult root = doingAssignment.keyResult = completed.get(obj.getDBID());
		if (root != null) {
			if (debug) logger.info("will not search assignments of " + doingObject + ". It already has a result, " + obj);
			if (!root.skippedDueToDepth) 
				return false;
		} else {

			root = new RootResult(completed.size()+1, obj, aPath); 
			
			// grandfa is not shown in results - fix by one more level
			doingAssignment.keyResult = new RootResult(completed.size()+1, obj, aPath);
			root.add(doingAssignment.keyResult);
			
			completed.put(obj.getDBID(), doingAssignment.keyResult);
		}
		
		root.skippedDueToDepth = doingAssignment.keyResult.skippedDueToDepth = 
				doingAssignment.fDepth == maxDepth;
		
		return !root.skippedDueToDepth;
	}

	public static class RootResult extends ReferenceSite {
		public final int num_prefix;
		public boolean skippedDueToDepth;
		public RootResult(int order, IGObject obj, ToplevelPath aPath) {
			super(debugTitle(obj), obj.computeSourceLocation(), 0, RefType.Declaration, aPath, obj);
			num_prefix = order;
		}
	}
	
	@Override
	IGSearchResultBuilder createResultBuilder(ZamiaProject fZPrj) {
		return new IGSearchResultBuilder(fZPrj) {
			@Override
			public ReferenceSearchResult add(ToplevelPath aPath, ReferenceSearchResult aRSR) {
				if (aRSR instanceof ReferenceSite && ((ReferenceSite) aRSR).getRefType() == RefType.Assignment) {
					doingAssignment.keyResult.add(aRSR);
				}
					
				return aRSR;
			}
		};
	}
	
}
