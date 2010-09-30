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
import org.zamia.rtl.RTLComparator;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.sim.IRTLModuleBehavior;
import org.zamia.rtl.sim.Simulator;
import org.zamia.vhdl.ast.OperationCompare.CompareOp;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class OperationCompareBehavior implements IRTLModuleBehavior {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public void init(RTLModule aModule, Simulator aSimulator) {
	}

	public void setPort(RTLPort aPort, ZILValue aValue, Simulator aSimulator) throws ZamiaException {
		RTLModule module = aPort.getModule();

		RTLComparator comparator = (RTLComparator) module;

		RTLPort z = comparator.getZ();

		if (aPort == z)
			return;

		RTLPort a = comparator.getA();
		RTLPort b = comparator.getB();
		CompareOp op = comparator.getOp();

		ZILValue va = aPort == a ? aValue : aSimulator.getValue(a);
		ZILValue vb = null;
		if (b != null)
			vb = aPort == b ? aValue : aSimulator.getValue(b);

		logger.debug("%s", "Simulator: Compare operation a=" + va + ", b=" + vb + ", op=" + op);

		ZILValue res = ZILValue.computeCompare(va, vb, op, null, module.getSource());
		logger.debug("Simulator:   ...res=%s", res);

		aSimulator.setDelta(z, res);

	}
}
