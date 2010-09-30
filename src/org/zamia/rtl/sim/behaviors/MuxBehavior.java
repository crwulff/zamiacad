/*
 * Copyright 2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */
package org.zamia.rtl.sim.behaviors;

import org.zamia.ExceptionLogger;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLMux;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.sim.IRTLModuleBehavior;
import org.zamia.rtl.sim.Simulator;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class MuxBehavior implements IRTLModuleBehavior {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public void init(RTLModule aModule, Simulator aSimulator) {
	}

	public void setPort(RTLPort aPort, ZILValue aValue, Simulator aSimulator) throws ZamiaException {

		RTLModule module = aPort.getModule();

		RTLMux mux = (RTLMux) module;

		RTLPort z = mux.getZ();

		if (aPort == z) {
			return;
		}

		RTLPort d1 = mux.getD1();
		RTLPort d2 = mux.getD2();
		RTLPort s = mux.getS();

		ZILValue vd1 = aPort == d1 ? aValue : aSimulator.getValue(d1);
		ZILValue vd2 = aPort == d2 ? aValue : aSimulator.getValue(d2);
		ZILValue vs = aPort == s ? aValue : aSimulator.getValue(s);

		if (vs.isLogicOne()) {
			aSimulator.setDelta(z, vd2);
		} else {
			aSimulator.setDelta(z, vd1);
		}
	}

}
