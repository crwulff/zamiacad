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
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationObject;
import org.zamia.instgraph.synth.IGObjectRemapping;
import org.zamia.instgraph.synth.IGOperationSynthAdapter;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.instgraph.synth.model.IGSMExprEngine;
import org.zamia.instgraph.synth.model.IGSMExprNode;
import org.zamia.instgraph.synth.model.IGSMExprNodeSignal;
import org.zamia.instgraph.synth.model.IGSMSequenceOfStatements;
import org.zamia.instgraph.synth.model.IGSMTarget;
import org.zamia.instgraph.synth.model.IGSMTargetSignal;
import org.zamia.rtl.RTLSignal;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSAOperationObject extends IGOperationSynthAdapter {

	@Override
	public IGSMExprNode preprocess(IGOperation aOperation, IGObjectRemapping aOR, IGSMSequenceOfStatements aPreprocessedSOS, IGSynth aSynth) throws ZamiaException {

		IGOperationObject oobj = (IGOperationObject) aOperation;

		IGObject obj = oobj.getObject();

		IGObject obj2 = aOR.get(obj);

		switch (obj2.getCat()) {
		case SIGNAL:

			RTLSignal s = aSynth.getOrCreateSignal(obj2);

			IGSMExprEngine ee = aSynth.getEE();
			
			return ee.signal(s, aSynth, oobj.computeSourceLocation());
		default:
			// FIXME
			throw new ZamiaException("Sorry, not implemented yet.");
		}
	}

	@Override
	public IGSMTarget preprocessTarget(IGOperation aOperation, IGObjectRemapping aOR, IGSMSequenceOfStatements aPreprocessedSOS, IGSynth aSynth) throws ZamiaException {
		IGOperationObject oobj = (IGOperationObject) aOperation;

		IGObject obj = oobj.getObject();

		IGObject obj2 = aOR.get(obj);

		switch (obj2.getCat()) {
		case SIGNAL:

			RTLSignal s = aSynth.getOrCreateSignal(obj2);

			return new IGSMTargetSignal(s, oobj.computeSourceLocation(), aSynth);
		default:
			// FIXME
			throw new ZamiaException("Sorry, not implemented yet.");
		}
	}
}
