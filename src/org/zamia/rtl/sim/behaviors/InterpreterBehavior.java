/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/
package org.zamia.rtl.sim.behaviors;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.sim.IRTLModuleBehavior;
import org.zamia.rtl.sim.Simulator;
import org.zamia.zil.ZILValue;
import org.zamia.zil.interpreter.ZILInterpreter;
import org.zamia.zil.interpreter.ZILInterpreterRuntimeEnv;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class InterpreterBehavior implements IRTLModuleBehavior {

	public void init(RTLModule aModule, Simulator aSimulator) throws ZamiaException {
		ZILInterpreter interpreter = (ZILInterpreter) aModule;

		interpreter.reset(aSimulator);
		ZILInterpreterRuntimeEnv runtime = interpreter.getRuntime();
		runtime.resume(aSimulator, null, 0);
	}

	public void setPort(RTLPort aPort, ZILValue aValue, Simulator aSimulator) throws ZamiaException {

		ZILInterpreter interpreter = (ZILInterpreter) aPort.getModule();
		ZILInterpreterRuntimeEnv runtime = interpreter.getRuntime();
		
		//sim_.setValue(p_, v_);

		runtime.resume(aSimulator, aPort, 0);


	}

}
