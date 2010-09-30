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
import org.zamia.rtl.RTLTargetEMux;
import org.zamia.rtl.sim.IRTLModuleBehavior;
import org.zamia.rtl.sim.Simulator;
import org.zamia.vhdl.ast.OperationLogic.LogicOp;
import org.zamia.zil.ZILRecordField;
import org.zamia.zil.ZILType;
import org.zamia.zil.ZILTypeArray;
import org.zamia.zil.ZILTypeEnum;
import org.zamia.zil.ZILTypeInteger;
import org.zamia.zil.ZILTypePhysical;
import org.zamia.zil.ZILTypeReal;
import org.zamia.zil.ZILTypeRecord;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class TargetEMuxBehavior implements IRTLModuleBehavior {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public void init(RTLModule aModule, Simulator aSimulator) {
	}

	public void setPort(RTLPort aPort, ZILValue aValue, Simulator aSimulator) throws ZamiaException {

		RTLModule module = aPort.getModule();

		RTLTargetEMux emux = (RTLTargetEMux) module;

		//sim_.setValue(aPort, v_);

		RTLPort z = emux.getZ();
		RTLPort ze = emux.getZE();

		if (aPort == z || aPort == ze) {
			return;
		}

		RTLPort d1 = emux.getD1();
		RTLPort d2 = emux.getD2();
		RTLPort e1 = emux.getE1();
		RTLPort e2 = emux.getE2();

		ZILValue vd1 = aSimulator.getValue(d1);
		ZILValue vd2 = aSimulator.getValue(d2);
		ZILValue ve1 = aSimulator.getValue(e1);
		ZILValue ve2 = aSimulator.getValue(e2);

		ZILValue ve = ZILValue.computeLogic(ve1, ve2, LogicOp.OR, null);

		ZILValue vz = calcZ(vd1, ve1, vd2, ve2);

		logger.debug("Simulator: %s", "      [TEMUX] vd1=" + vd1 + " vd2=" + vd2 + " ve1=" + ve1 + " ve2=" + ve2 + " => ve=" + ve + " vz=" + vz);

		aSimulator.setDelta(ze, ve);
		aSimulator.setDelta(z, vz);

	}

	private ZILValue calcZ(ZILValue vd1_, ZILValue ve1_, ZILValue vd2_, ZILValue ve2_) throws ZamiaException {
		ZILType t = vd1_.getType();

		if ((t instanceof ZILTypeInteger) || (t instanceof ZILTypeReal) || (t instanceof ZILTypePhysical) || (t instanceof ZILTypeEnum)) {

			if (ve1_.getBit() == ZILValue.BIT_1)
				return vd1_;

			return vd2_;
		} else if (t instanceof ZILTypeArray) {

			ZILTypeArray at = (ZILTypeArray) t;

			ZILValue res = new ZILValue(at, null, null);

			// FIXME: index range
			int n = (int) at.getIndexType().getCardinality();
			for (int i = 0; i < n; i++) {

				if (ve1_.getValue(i).getBit() == ZILValue.BIT_1) {
					res.setValue(i, vd1_.getValue(i));
				} else {
					res.setValue(i, vd2_.getValue(i));
				}
			}

			return res;
		} else if (t instanceof ZILTypeRecord) {

			ZILTypeRecord rt = (ZILTypeRecord) t;

			ZILValue res = new ZILValue(rt, null, null);

			int n = rt.getNumRecordFields();
			for (int i = 0; i < n; i++) {
				ZILRecordField rf = rt.getRecordField(i);

				if (ve1_.getValue(i).getBit() == ZILValue.BIT_1) {
					res.setValue(rf, vd1_.getValue(rf));
				} else {
					res.setValue(rf, vd2_.getValue(rf));
				}
			}

			return res;

		} else
			throw new ZamiaException("Internal error: Don't know how to compute value for " + t);
	}

	//	private ZILValue calcE(ZILValue e1_, ZILValue e2_) throws ZamiaException,
	//			SimException {
	//
	//		ZILType t = e1_.getType();
	//
	//		if (t.isBit()) {
	//
	//			char bit1 = e1_.getBit();
	//
	//			switch (bit1) {
	//			case ZILValue.BIT_0:
	//				return e1_;
	//			case ZILValue.BIT_1:
	//				return e2_;
	//			default:
	//				return ZILValue.getBit(ZILValue.BIT_X);
	//			}
	//
	//		} else if (t instanceof ZILTypeArray) {
	//
	//			ZILTypeArray at = (ZILTypeArray) t;
	//
	//			ZILValue res = new ZILValue(at);
	//
	//			int n = (int) at.getIndexType().getCardinality();
	//			for (int i = 0; i < n; i++) {
	//				res.addValue(calcE(e1_.getValue(i), e2_.getValue(i)));
	//			}
	//
	//			return res;
	//		} else
	//			throw new SimException("Error: Illegal enable type ");
	//	}

}
