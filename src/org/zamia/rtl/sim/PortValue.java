/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
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
		
public class PortValue {

	public RTLPort fPort;
	public ZILValue fValue;
	
	public PortValue (RTLPort aPort, ZILValue aValue) {
		fPort = aPort;
		fValue = aValue;
	}
	
	@Override
	public String toString() {
		return "PV("+fPort+" => "+fValue+")";
	}
}
