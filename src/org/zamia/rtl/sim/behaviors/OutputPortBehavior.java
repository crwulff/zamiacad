/*
 * Copyright 2007,2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on May 19, 2007
 */

package org.zamia.rtl.sim.behaviors;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLOutputPort;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.sim.IRTLModuleBehavior;
import org.zamia.rtl.sim.Simulator;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class OutputPortBehavior implements IRTLModuleBehavior {

	public void init(RTLModule aModule, Simulator aSimulator) {
	}

	public void setPort(RTLPort aPort, ZILValue aValue, Simulator aSimulator) throws ZamiaException {
		//aSimulator.setValue(aPort, aValue);

		RTLOutputPort module = (RTLOutputPort) aPort.getModule();

		RTLPort ip = module.getInternalPort();

		if (aPort == ip) {
			aSimulator.setDelta(module.getExternalPort(), aValue);
		}
	}

}
