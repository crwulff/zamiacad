/* 
 * Copyright 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 3, 2011
 */
package org.zamia.instgraph.synth.model;

import org.zamia.SourceLocation;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.rtlng.RTLSignal;
import org.zamia.rtlng.RTLValue;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSMExprNodeClockEdge extends IGSMExprNode {

	private final RTLSignal fSignal;

	private final boolean fRisingEdge;

	public IGSMExprNodeClockEdge(RTLSignal aSignal, boolean aRisingEdge, SourceLocation aLocation, IGSynth aSynth) {
		super(aSignal.getType(), aLocation, aSynth);
		fSignal = aSignal;
		fRisingEdge = aRisingEdge;
	}

	public RTLSignal getSignal() {
		return fSignal;
	}

	public boolean isRisingEdge() {
		return fRisingEdge;
	}

	public String toString() {
		if (fRisingEdge)
			return "↑" + fSignal.getId();
		return "↓" + fSignal.getId();
	}

	@Override
	public RTLValue getStaticValue() {
		return null;
	}

}
