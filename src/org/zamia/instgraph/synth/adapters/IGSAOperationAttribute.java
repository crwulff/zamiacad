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
import org.zamia.instgraph.IGOperationAttribute;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.synth.IGBindings;
import org.zamia.instgraph.synth.IGClock;
import org.zamia.instgraph.synth.IGObjectRemapping;
import org.zamia.instgraph.synth.IGOperationSynthAdapter;
import org.zamia.instgraph.synth.IGSynth;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSAOperationAttribute extends IGOperationSynthAdapter {

	@Override
	public IGOperation inlineSubprograms(IGOperation aOperation, IGObjectRemapping aOR, IGSequenceOfStatements aInlinedSOS, IGSynth aSynth) throws ZamiaException {

		IGOperationAttribute oattr = (IGOperationAttribute) aOperation;

		IGOperation op = oattr.getOp();

		IGOperation op2 = op != null ? aSynth.getSynthAdapter(op).inlineSubprograms(op, aOR, aInlinedSOS, aSynth) : null;

		return new IGOperationAttribute(oattr.getAttrOp(), oattr.getItem(), op2, oattr.getType(), oattr.computeSourceLocation(), aSynth.getZDB());
	}

	@Override
	public IGOperation resolveVariables(IGOperation aOperation, IGBindings aBindings, IGSequenceOfStatements aResolvedSOS, IGClock aClock, IGObjectRemapping aOR, IGSynth aSynth)
			throws ZamiaException {

		IGOperationAttribute oattr = (IGOperationAttribute) aOperation;

		IGOperation op = oattr.getOp();

		IGOperation op2 = op != null ? aSynth.getSynthAdapter(op).resolveVariables(op, aBindings, aResolvedSOS, aClock, aOR, aSynth) : null;

		return new IGOperationAttribute(oattr.getAttrOp(), oattr.getItem(), op2, oattr.getType(), oattr.computeSourceLocation(), aSynth.getZDB());
	}

}
