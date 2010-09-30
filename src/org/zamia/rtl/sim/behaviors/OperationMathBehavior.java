/*
 * Copyright 2007,2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on May 19, 2007
 */

package org.zamia.rtl.sim.behaviors;

import org.zamia.ExceptionLogger;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLOperationMath;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.sim.IRTLModuleBehavior;
import org.zamia.rtl.sim.Simulator;
import org.zamia.vhdl.ast.OperationMath.MathOp;
import org.zamia.zil.ZILType;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class OperationMathBehavior implements IRTLModuleBehavior {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public void init(RTLModule aModule, Simulator aSimulator) {
	}

	public void setPort(RTLPort aPort, ZILValue aValue, Simulator aSimulator) throws ZamiaException {

		RTLModule module = aPort.getModule();

		RTLOperationMath math = (RTLOperationMath) module;

		RTLPort z = math.getZ();

		//			aSimulator.setValue(aPort, aValue);

		if (aPort == z)
			return;

		RTLPort a = math.getA();
		RTLPort b = math.getB();
		MathOp op = math.getOp();

		ZILType t = a.getType();

		ZILValue va = aSimulator.getValue(a);
		ZILValue vb = null;
		if (b != null)
			vb = aSimulator.getValue(b);

		logger.debug("Simulator: %s", "Math operation, type=" + t + ", a=" + va + ", b=" + vb + ", op=" + op);

		ZILValue vres = ZILValue.computeMath(va, vb, op, null);

		aSimulator.setDelta(z, vres);

	}

}
