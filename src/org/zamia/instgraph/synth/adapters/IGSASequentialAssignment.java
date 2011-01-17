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
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGSequentialAssignment;
import org.zamia.instgraph.IGSequentialStatement;
import org.zamia.instgraph.synth.IGClock;
import org.zamia.instgraph.synth.IGObjectRemapping;
import org.zamia.instgraph.synth.IGStmtSynthAdapter;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.instgraph.synth.model.IGSMAssignment;
import org.zamia.instgraph.synth.model.IGSMExprNode;
import org.zamia.instgraph.synth.model.IGSMSequenceOfStatements;
import org.zamia.instgraph.synth.model.IGSMTarget;

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

}
