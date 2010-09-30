/*
 * Copyright 2007,2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.rtl.sim;

import org.zamia.rtl.RTLPort;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class SimEvent {

	private RTLPort port;

	private ZILValue value;

	public SimEvent(RTLPort port_, ZILValue value_) {
		value = value_;
		port = port_;
	}

	public void dump() {
		//System.out.println("New Event : " );
		//System.out.println("port =" + port);
		//System.out.println("value = : " + value);
		System.out.println("event: gate = " + port.getModule().getInstanceName() + " port = " + port + " value = " + value);
		// FIXME: implement
	}

	public RTLPort getPort() {
		return port;
	}

	public ZILValue getValue() {
		return value;
	}

	public String toString() {
		return "SimEvent @" + Integer.toHexString(hashCode()) + " " + port + " => " + value;
	}

}
