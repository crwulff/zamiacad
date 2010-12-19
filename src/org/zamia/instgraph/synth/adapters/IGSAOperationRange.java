/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 23, 2010
 */
package org.zamia.instgraph.synth.adapters;

import org.zamia.ZamiaException;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationRange;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.synth.IGBindings;
import org.zamia.instgraph.synth.IGClock;
import org.zamia.instgraph.synth.IGObjectRemapping;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.instgraph.synth.IGOperationSynthAdapter;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSAOperationRange extends IGOperationSynthAdapter {

	@Override
	public IGOperation inlineSubprograms(IGOperation aOperation, IGObjectRemapping aOR, IGSequenceOfStatements aInlinedSOS, IGSynth aSynth) throws ZamiaException {

		IGOperationRange orange = (IGOperationRange) aOperation;

		IGOperation op = orange.getOperand();
		IGOperation range = orange.getRange();

		IGOperation op2 = aSynth.getSynthAdapter(op).inlineSubprograms(op, aOR, aInlinedSOS, aSynth);
		IGOperation range2 = aSynth.getSynthAdapter(range).inlineSubprograms(range, aOR, aInlinedSOS, aSynth);

		return new IGOperationRange(range2, op2, orange.getType(), orange.computeSourceLocation(), aSynth.getZDB());
	}

	@Override
	public IGOperation resolveVariables(IGOperation aOperation, IGBindings aBindings, IGSequenceOfStatements aResolvedSOS, IGClock aClock, IGObjectRemapping aOR, IGSynth aSynth)
			throws ZamiaException {

		IGOperationRange orange = (IGOperationRange) aOperation;

		IGOperation op = orange.getOperand();
		IGOperation range = orange.getRange();

		IGOperation op2 = aSynth.getSynthAdapter(op).resolveVariables(op, aBindings, aResolvedSOS, aClock, aOR, aSynth);
		IGOperation range2 = aSynth.getSynthAdapter(range).resolveVariables(range, aBindings, aResolvedSOS, aClock, aOR, aSynth);

		return new IGOperationRange(range2, op2, orange.getType(), orange.computeSourceLocation(), aSynth.getZDB());
	}

}
