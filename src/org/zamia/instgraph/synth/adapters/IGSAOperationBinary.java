/* 
 * Copyright 2010,2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 23, 2010
 */
package org.zamia.instgraph.synth.adapters;

import org.zamia.ZamiaException;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationBinary;
import org.zamia.instgraph.synth.IGObjectRemapping;
import org.zamia.instgraph.synth.IGOperationSynthAdapter;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.instgraph.synth.model.IGSMExprNode;
import org.zamia.instgraph.synth.model.IGSMExprNodeClockEdge;
import org.zamia.instgraph.synth.model.IGSMSequenceOfStatements;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSAOperationBinary extends IGOperationSynthAdapter {

	@Override
	public IGSMExprNode preprocess(IGOperation aOperation, IGObjectRemapping aOR, IGSMSequenceOfStatements aPreprocessedSOS, IGSynth aSynth) throws ZamiaException {

		IGOperationBinary bop = (IGOperationBinary) aOperation;

		IGSMExprNodeClockEdge en = aSynth.findClock(bop);
		if (en != null) {
			return en;
		}

		// FIXME: implement
		throw new ZamiaException("Sorry, not implemented yet");

	}

}
