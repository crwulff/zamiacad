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
import org.zamia.rtl.RTLLiteral;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.sim.IRTLModuleBehavior;
import org.zamia.rtl.sim.Simulator;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class LiteralBehavior implements IRTLModuleBehavior {

	public void init(RTLModule aModule, Simulator aSimulator) {
		
		RTLLiteral l = (RTLLiteral) aModule;
		
		ZILValue v = l.getValue();
		
		aSimulator.setDelta(l.getZ(), v);
		
	}

	public void setPort(RTLPort aPort, ZILValue aValue, Simulator aSimulator) throws ZamiaException {
        //aSimulator.setValue(aPort, aValue);
	}

}
