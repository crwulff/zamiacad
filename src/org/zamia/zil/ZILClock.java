/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 28, 2009
 */
package org.zamia.zil;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class ZILClock {
	
	private ZILSignal fSignal;
	private boolean fRisingEdge;
	
	public ZILClock (ZILSignal aSignal, boolean aRisingEdge) {
		fSignal = aSignal;
		fRisingEdge = aRisingEdge;
	}
	
	public ZILSignal getSignal () {
		return fSignal;
	}
	
	public boolean isRisingEdge() {
		return fRisingEdge;
	}
	
	public String toString () {
		String edge;
		if (fRisingEdge)
			edge = " rising edge";
		else
			edge = " falling edge";
		return "Clock: "+fSignal+edge;
	}
}
