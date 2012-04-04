/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on May 23, 2009
 */
package org.zamia.instgraph;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ig.IGAssignmentsSearch;
import org.zamia.analysis.ig.IGAssignmentsSearch.AccessedThroughItems;
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGPopStmt;
import org.zamia.util.HashSetArray;
import org.zamia.util.Pair;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class IGSequentialAssignment extends IGSequentialStatement {

	private IGOperation fValue;

	private IGOperation fTarget;

	private IGOperation fReject;

	private boolean fInertial;

	private IGOperation fDelay;

	public IGSequentialAssignment(IGOperation aValue, IGOperation aTarget, boolean aInertial, IGOperation aReject, String aId, SourceLocation aSrc, ZDB aZDB) {
		super(aId, aSrc, aZDB);
		fValue = aValue;
		fTarget = aTarget;
		fReject = aReject;
		fInertial = aInertial;
	}

	public void setDelay(IGOperation aDelay) {
		fDelay = aDelay;
	}

	
	/**
	 * 
	 * The idea is that COND and DRV are never written while TARGET is never read in
	 * (Correction: this is a wrong assumption with A(IDX) <= DRV as a counterexample.)
	 *  
	 * 		if COND then TARGET <= DRV; 
	 * 
	 * If user captured a writing into TARGET, we he might be also interested in capturing the sources:
	 * the right hand of the assignment, DRV, and COND. So, we propagate the search on them. On the other 
	 * hand, when DRV or COND is recorded as a result, we might be interested in the targets. So, we 
	 * schedule a new search on TARGET.
	 * 
	 * In short, this can be rewritten as 
	 * 		TARGET <= DRV, COND 
	 * */	
	public void propagateSearch(boolean left, int aDepth, HashSetArray<IGItemAccess> newItems, AccessedThroughItems todo) {
		newItems.clear();
		fTarget.computeAccessedItems(true, null, null, aDepth, newItems);
		fValue.computeAccessedItems(false, null, null, aDepth, newItems);
		
		AccessType filterType = left ? AccessType.Write : AccessType.Read;
		HashSetArray<IGItemAccess> filteredItems = new HashSetArray<IGItemAccess>();
		int n = newItems.size();
		for (IGItemAccess a : newItems)
			if (a.getAccessType() == filterType) 
				filteredItems.add(a);
		
		todo.scheduleAssignments(filteredItems, true, this.computeSourceLocation());
		
	}
	
	@Override
	public void computeAccessedItems(IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {
		
		HashSetArray<IGItemAccess> newItems = new HashSetArray<IGItemAccess>();
		fValue.computeAccessedItems(false, aFilterItem, aFilterType, aDepth, newItems);
		fTarget.computeAccessedItems(true, aFilterItem, aFilterType, aDepth, newItems);
		
		if (aAccessedItems instanceof AccessedThroughItems) {
			
			HashSetArray<IGItemAccess> leftItems = new HashSetArray<IGItemAccess>();
			HashSetArray<IGItemAccess> rightItems = new HashSetArray<IGItemAccess>();
			
			for (IGItemAccess a: newItems)
				if (a.getAccessType() == AccessType.Read)
					rightItems.add(a);
				else 
					leftItems.add(a);
			
			AccessedThroughItems todo = ((AccessedThroughItems) aAccessedItems);
			
			//see if left items are written
			int sizeBefore = todo.size();
			todo.addAll(leftItems);
			if (todo.size() != sizeBefore) { 
				
				// then we depend on all right items - propagate search on them
				propagateSearch(false, aDepth, newItems, todo);
				
				// treat IF conditions as right items
				for (Pair<IGSequentialStatement, HashSetArray<IGItemAccess>> parent: todo.ifStack) {
					newItems.clear();
					IGSequentialStatement ifStatement = parent.getFirst(); 
					IGOperation conditional = (IGOperation) (ifStatement instanceof IGSequentialLoop 
							? ((IGSequentialLoop) ifStatement).getChild(2)
							: ((IGSequentialIf) ifStatement).getCond());
					conditional.computeAccessedItems(false, null, null, 0, newItems);
					todo.scheduleAssignments(newItems, true, parent.getFirst().computeSourceLocation());
				}
			}
			
			//see if right items are read
			sizeBefore = todo.size();
			todo.addAll(rightItems);
			for (Pair<IGSequentialStatement, HashSetArray<IGItemAccess>> parent: todo.ifStack) {// IF conditions 
				// throughItems.addAll(parent.getSecond()); // addAll does not call overriden add()
				for (IGItemAccess condItem: parent.getSecond())
					todo.add(condItem);
			}
			if (todo.size() != sizeBefore) {
				propagateSearch(true, aDepth, newItems, todo); // propagate search to the left
			}
			
		} else {
			aAccessedItems.addAll(newItems);
		}

		if (fReject != null) {
			fReject.computeAccessedItems(false, aFilterItem, aFilterType, aDepth, aAccessedItems);
		}

		if (fDelay != null) {
			fDelay.computeAccessedItems(false, aFilterItem, aFilterType, aDepth, aAccessedItems);
		}
	}

	@Override
	public void generateCode(IGInterpreterCode aCode) throws ZamiaException {

		fTarget.generateCode(true, aCode);
		fValue.generateCode(true, aCode);

		if (fDelay != null) {
			fDelay.generateCode(true, aCode);
		}

		if (fReject != null) {
			fReject.generateCode(true, aCode);
		}

		aCode.add(new IGPopStmt(fInertial, fDelay != null, fReject != null, computeSourceLocation(), getZDB()));
	}

	@Override
	public IGItem getChild(int aIdx) {
		switch (aIdx) {
		case 0:
			return fDelay;
		case 1:
			return fReject;
		case 2:
			return fTarget;
		}
		return fValue;
	}

	@Override
	public int getNumChildren() {
		return 4;
	}

	@Override
	public String toString() {
		return fTarget.toString() + " <= " + fValue;
	}

	@Override
	public String toHRString() {
		return fTarget.toHRString() + " <= " + fValue.toHRString();
	}

	public IGOperation getValue() {
		return fValue;
	}

	public IGOperation getTarget() {
		return fTarget;
	}

	public IGOperation getReject() {
		return fReject;
	}

	public boolean isInertial() {
		return fInertial;
	}

	public IGOperation getDelay() {
		return fDelay;
	}

	@Override
	public void dump(int aIndent) {
		logger.debug(aIndent, "%s := %s [delay=%s, inertial=%s, reject=%s]", fTarget, fValue, fDelay, fInertial, fReject);
	}
}
