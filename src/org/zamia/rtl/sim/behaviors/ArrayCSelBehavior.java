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
import org.zamia.rtl.RTLArrayCSel;
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

public class ArrayCSelBehavior implements IRTLModuleBehavior {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public void init(RTLModule aModule, Simulator aSimulator) {
	}

	public void setPort(RTLPort aPort, ZILValue aValue, Simulator aSimulator) throws ZamiaException {

		RTLModule module = aPort.getModule();

		RTLArrayCSel csel = (RTLArrayCSel) module;

		RTLPort d = csel.getD();

		if (aPort != d) {
			return;
		}

		int n = csel.getNumOutputs();
		for (int i = 0; i < n; i++) {

			int offset = csel.getOutputOffset(i);

			ZILValue v = aValue.getValue(offset);

			aSimulator.setDelta(csel.getOutput(i), v);
		}
	}

}
