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
import org.zamia.rtl.RTLTargetCond;
import org.zamia.rtl.sim.IRTLModuleBehavior;
import org.zamia.rtl.sim.Simulator;
import org.zamia.zil.ZILType;
import org.zamia.zil.ZILTypeArray;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class TargetCondBehavior implements IRTLModuleBehavior {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public void init(RTLModule aModule, Simulator aSimulator) {
	}

	public void setPort(RTLPort aPort, ZILValue aValue, Simulator aSimulator) throws ZamiaException {

		RTLModule module = aPort.getModule();

		RTLTargetCond cond = (RTLTargetCond) module;

		//sim_.setValue(p_, v_);

		RTLPort z = cond.getZ();
		RTLPort ze = cond.getZE();

		if (aPort == z || aPort == ze) {
			return;
		}

		RTLPort d = cond.getD();
		if (aPort == d) {
			aSimulator.setDelta(z, aValue);
			return;
		}

		RTLPort e = cond.getE();
		RTLPort c = cond.getC();

		ZILValue ve = aSimulator.getValue(e);
		ZILValue vc = aSimulator.getValue(c);

		ZILValue res = calcAnd(ve, vc.getBit());

		//System.out.println ("TargetCond calc: ve="+ve+", vc="+vc+", res="+res);

		aSimulator.setDelta(ze, res);

	}

	private ZILValue calcAnd(ZILValue ve, char bit) throws ZamiaException {
		ZILType t = ve.getType();

		if (t.isBit()) {

			switch (bit) {
			case ZILValue.BIT_0:
				return ZILValue.getBit(bit, null);
			case ZILValue.BIT_1:
				return ve;
			default:
				return ZILValue.getBit(ZILValue.BIT_X, null);
			}

		} else if (t instanceof ZILTypeArray) {

			ZILTypeArray at = (ZILTypeArray) t;

			ZILValue res = new ZILValue(at, null, null);

			// FIXME: index range (low/high)
			int n = (int) at.getIndexType().getCardinality();
			for (int i = 0; i < n; i++) {
				res.setValue(i, calcAnd(ve.getValue(i), bit));
			}

			return res;
		} else
			throw new ZamiaException("Error: Illegal enable type ");
	}
}
