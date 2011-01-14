/* 
 * Copyright 2010, 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 23, 2010
 */
package org.zamia.instgraph.synth.adapters;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGItemAccess;
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGObject.IGObjectCat;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSequentialAssignment;
import org.zamia.instgraph.IGSequentialStatement;
import org.zamia.instgraph.synth.IGBindingNodeValue;
import org.zamia.instgraph.synth.IGBindings;
import org.zamia.instgraph.synth.IGClock;
import org.zamia.instgraph.synth.IGObjectRemapping;
import org.zamia.instgraph.synth.IGStmtSynthAdapter;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.instgraph.synth.model.IGSMAssignment;
import org.zamia.instgraph.synth.model.IGSMExprNode;
import org.zamia.instgraph.synth.model.IGSMSequenceOfStatements;
import org.zamia.instgraph.synth.model.IGSMTarget;
import org.zamia.util.HashSetArray;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSASequentialAssignment extends IGStmtSynthAdapter {

	@Override
	public void preprocess(IGSequentialStatement aStmt, IGObjectRemapping aOR, IGSMSequenceOfStatements aPreprocessedSOS, String aReturnVarName, IGClock aClock, IGSynth aSynth)
			throws ZamiaException {

		SourceLocation location = aStmt.computeSourceLocation();

		IGSequentialAssignment assign = (IGSequentialAssignment) aStmt;

		IGOperation reject = assign.getReject();
		if (reject != null) {
			throw new ZamiaException("Not synthesizable.", location);
		}

		IGOperation delay = assign.getDelay();
		if (delay != null) {
			throw new ZamiaException("Not synthesizable.", location);
		}

		IGOperation value = assign.getValue();

		IGOperation target = assign.getTarget();

		IGSMExprNode valueExpr = value != null ? aSynth.getSynthAdapter(value).preprocess(value, aOR, aPreprocessedSOS, aSynth) : null;
		IGSMTarget targetExpr = target != null ? aSynth.getSynthAdapter(target).preprocessTarget(target, aOR, aPreprocessedSOS, aSynth) : null;

		IGSMAssignment ssa = new IGSMAssignment(valueExpr, targetExpr, assign.getId(), assign.computeSourceLocation(), aSynth);
		aPreprocessedSOS.add(ssa);

	}


	
	@Override
	public void inlineSubprograms(IGSequentialStatement aStmt, IGObjectRemapping aOR, IGSequenceOfStatements aInlinedSOS, String aReturnVarName, IGSynth aSynth)
			throws ZamiaException {

		SourceLocation location = aStmt.computeSourceLocation();

		IGSequentialAssignment assign = (IGSequentialAssignment) aStmt;

		IGOperation reject = assign.getReject();
		if (reject != null) {
			throw new ZamiaException("Not synthesizable.", location);
		}

		IGOperation delay = assign.getDelay();
		if (delay != null) {
			throw new ZamiaException("Not synthesizable.", location);
		}

		IGOperation value = assign.getValue();

		IGOperation target = assign.getTarget();

		value = value != null ? aSynth.getSynthAdapter(value).inlineSubprograms(value, aOR, aInlinedSOS, aSynth) : null;
		target = target != null ? aSynth.getSynthAdapter(target).inlineSubprograms(target, aOR, aInlinedSOS, aSynth) : null;

		IGSequentialAssignment ssa = new IGSequentialAssignment(value, target, assign.isInertial(), null, assign.getId(), assign.computeSourceLocation(), aSynth.getZDB());
		aInlinedSOS.add(ssa);

	}

	@Override
	public IGBindings resolveVariables(IGSequentialStatement aStmt, IGSequenceOfStatements aInlinedSOS, IGBindings aBindings, IGSequenceOfStatements aResolvedSOS, IGClock aClock,
			IGObjectRemapping aOR, IGSynth aSynth) throws ZamiaException {

		IGSequentialAssignment assign = (IGSequentialAssignment) aStmt;

		IGOperation value = assign.getValue();

		IGOperation target = assign.getTarget();

		HashSetArray<IGItemAccess> accessedItems = new HashSetArray<IGItemAccess>();

		target.computeAccessedItems(true, null, AccessType.Write, 0, accessedItems);

		int n = accessedItems.size();
		IGObject targetObject = null;
		for (int i = 0; i < n; i++) {

			IGItemAccess item = accessedItems.get(i);

			if (item.getItem() instanceof IGObject) {
				targetObject = (IGObject) item.getItem();
				break;
			}
		}

		if (targetObject == null) {
			throw new ZamiaException("Internal error: target object is null.");
		}

		value = value != null ? aSynth.getSynthAdapter(value).resolveVariables(value, aBindings, aResolvedSOS, aClock, aOR, aSynth) : null;
		target = target != null ? aSynth.getSynthAdapter(target).resolveVariables(target, aBindings, aResolvedSOS, aClock, aOR, aSynth) : null;

		IGSequentialAssignment ssa = new IGSequentialAssignment(value, target, assign.isInertial(), null, assign.getId(), assign.computeSourceLocation(), aSynth.getZDB());

		IGBindings resBindings = new IGBindings();

		if (targetObject.getCat() == IGObjectCat.VARIABLE) {

			IGBindingNodeValue node = new IGBindingNodeValue(targetObject, ssa);

			resBindings.bind(targetObject, node, aClock);

		} else {

			aResolvedSOS.add(ssa);

		}

		return resBindings;
	}

	@Override
	public IGBindings computeBindings(IGSequentialStatement aStmt, IGSequenceOfStatements aResolvedSOS, IGBindings aLastBindings, IGClock aClock, IGSynth aSynth)
			throws ZamiaException {

		IGSequentialAssignment assign = (IGSequentialAssignment) aStmt;

		IGOperation target = assign.getTarget();

		IGBindings resBindings = new IGBindings();

		HashSetArray<IGItemAccess> accessedItems = new HashSetArray<IGItemAccess>();

		target.computeAccessedItems(true, null, AccessType.Write, 0, accessedItems);

		int n = accessedItems.size();
		IGObject targetObject = null;
		for (int i = 0; i < n; i++) {

			IGItemAccess item = accessedItems.get(i);

			if (item.getItem() instanceof IGObject) {
				targetObject = (IGObject) item.getItem();
				break;
			}
		}

		if (targetObject == null) {
			throw new ZamiaException("Internal error: target object is null.");
		}

		IGBindingNodeValue node = new IGBindingNodeValue(targetObject, assign);

		resBindings.bind(targetObject, node, aClock);

		return resBindings;
	}

}
