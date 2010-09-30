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
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLTargetArray;
import org.zamia.rtl.sim.IRTLModuleBehavior;
import org.zamia.rtl.sim.Simulator;
import org.zamia.zil.ZILTypeArray;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class TargetArrayBehavior implements IRTLModuleBehavior {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public void init(RTLModule aModule, Simulator aSimulator) {
	}

	public void setPort(RTLPort aPort, ZILValue aValue, Simulator aSimulator) throws ZamiaException {

		RTLModule module = aPort.getModule();

		RTLTargetArray targetArray = (RTLTargetArray) module;

		//sim_.setValue(p_, v_);

		RTLPort z = targetArray.getZ();

		if (aPort == z) {
			return;
		}

		ZILTypeArray at = targetArray.getOutputType();
		ZILValue zv = new ZILValue(at, null, null);
		
		int n = targetArray.getNumInputs();
		for (int i = 0; i<n; i++) {
			
			int offset = targetArray.getInputOffset(i);
			RTLPort inputPort = targetArray.getInput(i);
			
			ZILValue v = aPort == inputPort ? aValue : aSimulator.getValue(inputPort);
			
			zv.setValue(offset, v);
		}
		
		aSimulator.setDelta(z, zv);
	}

}
