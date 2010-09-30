/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/
package org.zamia.rtl.sim.behaviors;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLInputPort;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.sim.IRTLModuleBehavior;
import org.zamia.rtl.sim.Simulator;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class InputPortBehavior implements IRTLModuleBehavior {

	public void init(RTLModule aModule, Simulator aSimulator) throws ZamiaException {

		RTLInputPort module = (RTLInputPort) aModule;
		
		RTLPort ep = module.getExternalPort();
		RTLPort ip = module.getInternalPort();
		
		RTLSignal s = ep.getSignal();
		ZILValue v = null;
		if (s != null) {
			v = aSimulator.getValue(s);
		} else {
			v = ep.getInitialValue();
		}
		
		if (v == null) {
			v = ZILValue.generateUValue(ep.getType(), null, null);
		}
		
		aSimulator.setDelta(ep, v);
		aSimulator.setDelta(ip, v);

	}

	public void setPort(RTLPort aPort, ZILValue aValue, Simulator aSimulator) throws ZamiaException {
		
        //sim_.setValue(p_, v_);

		RTLInputPort module = (RTLInputPort) aPort.getModule();
		
		RTLPort ep = module.getExternalPort();
		
		if (aPort == ep) {
			aSimulator.setDelta(module.getInternalPort(), aValue);
		}
		
	}

}
