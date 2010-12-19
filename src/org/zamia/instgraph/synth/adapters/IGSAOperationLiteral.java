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
import org.zamia.instgraph.IGOperationLiteral;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.synth.IGObjectRemapping;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.instgraph.synth.IGOperationSynthAdapter;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSAOperationLiteral extends IGOperationSynthAdapter {

	@Override
	public IGOperation inlineSubprograms(IGOperation aOperation, IGObjectRemapping aOR, IGSequenceOfStatements aInlinedSOS, IGSynth aSynth) throws ZamiaException {

		IGOperationLiteral ol = (IGOperationLiteral) aOperation;

		return ol.computeStaticValue(aSynth.getRuntimeEnv());

	}

}
