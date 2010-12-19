/*
 * Copyright 2005-2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.instgraph.synth;

import org.zamia.ZamiaException;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGSequentialAssignment;
import org.zamia.rtlng.RTLSignal;
import org.zamia.rtlng.RTLSignalAE;

/**
 * 
 * @author guenter bartsch
 * 
 */

public class IGBindingNodeValue extends IGBindingNode {

	public static final boolean dump = false;

	private final IGSequentialAssignment fValue;

	public IGBindingNodeValue(IGObject aObj, IGSequentialAssignment aValue) {
		super(aObj);
		fValue = aValue;
	}

	@Override
	public String toString() {
		return "IGBindingNodeValue(value=" + fValue + ")";
	}

	public IGSequentialAssignment getValue() {
		return fValue;
	}


	@Override
	public void dump(int aI) {
		logger.debug(aI, "IGBindingNodeValue");
		logger.debug(aI + 2, "value=");
		fValue.dump(aI + 2);
	}

	@Override
	public RTLSignalAE synthesize(IGSynth aSynth) throws ZamiaException {
		
		IGOperation value = fValue.getValue();
		IGOperation target = fValue.getTarget();
		
		RTLSignal s = aSynth.getSynthAdapter(value).synthesizeValue(value, aSynth);
		RTLSignal e = aSynth.getSynthAdapter(target).synthesizeEnable(target, aSynth);
		
		RTLSignalAE res = new RTLSignalAE(s, e, fValue.computeSourceLocation(), aSynth.getZDB());
		
		return res;
	}
}
