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
import org.zamia.instgraph.IGRange;
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

public class IGSARange extends IGOperationSynthAdapter {

	@Override
	public IGOperation inlineSubprograms(IGOperation aOperation, IGObjectRemapping aOR, IGSequenceOfStatements aInlinedSOS, IGSynth aSynth) throws ZamiaException {

		IGRange orange = (IGRange) aOperation;

		IGOperation l = orange.getLeft();
		IGOperation a = orange.getAscending();
		IGOperation r = orange.getRight();

		IGOperation l2 = aSynth.getSynthAdapter(l).inlineSubprograms(l, aOR, aInlinedSOS, aSynth);
		IGOperation r2 = aSynth.getSynthAdapter(r).inlineSubprograms(r, aOR, aInlinedSOS, aSynth);
		IGOperation a2 = aSynth.getSynthAdapter(a).inlineSubprograms(a, aOR, aInlinedSOS, aSynth);

		return new IGRange(l2, r2, a2, orange.computeSourceLocation(), aSynth.getZDB());
	}

	@Override
	public IGOperation resolveVariables(IGOperation aOperation, IGBindings aBindings, IGSequenceOfStatements aResolvedSOS, IGClock aClock, IGObjectRemapping aOR, IGSynth aSynth)
			throws ZamiaException {

		IGRange orange = (IGRange) aOperation;

		IGOperation l = orange.getLeft();
		IGOperation a = orange.getAscending();
		IGOperation r = orange.getRight();

		IGOperation l2 = aSynth.getSynthAdapter(l).resolveVariables(l, aBindings, aResolvedSOS, aClock, aOR, aSynth);
		IGOperation r2 = aSynth.getSynthAdapter(r).resolveVariables(r, aBindings, aResolvedSOS, aClock, aOR, aSynth);
		IGOperation a2 = aSynth.getSynthAdapter(a).resolveVariables(a, aBindings, aResolvedSOS, aClock, aOR, aSynth);

		return new IGRange(l2, r2, a2, orange.computeSourceLocation(), aSynth.getZDB());
	}

}
