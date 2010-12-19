/* 
 * Copyright 2009,2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 28, 2009
 */
package org.zamia.instgraph.synth;

import org.zamia.instgraph.IGObject;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGClock {

	private IGObject fSignal;

	private boolean fRisingEdge;

	public IGClock(IGObject aSignal, boolean aRisingEdge) {
		fSignal = aSignal;
		fRisingEdge = aRisingEdge;
	}

	public IGObject getSignal() {
		return fSignal;
	}

	public boolean isRisingEdge() {
		return fRisingEdge;
	}

	public String toString() {
		String edge;
		if (fRisingEdge)
			edge = " rising edge";
		else
			edge = " falling edge";
		return "Clock: " + fSignal + edge;
	}
}
