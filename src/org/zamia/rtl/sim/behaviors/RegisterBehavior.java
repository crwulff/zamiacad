/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
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
import org.zamia.rtl.RTLRegister;
import org.zamia.rtl.sim.IRTLModuleBehavior;
import org.zamia.rtl.sim.Simulator;
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
 * @author guenter bartsch
 */

public class RegisterBehavior implements IRTLModuleBehavior {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();
	public final static ExceptionLogger el = ExceptionLogger.getInstance();
	
	public void init(RTLModule aModule, Simulator aSimulator) {
	}

	public void setPort(RTLPort aPort, ZILValue aValue, Simulator aSimulator) throws ZamiaException {
		
		RTLModule module = aPort.getModule();
		
		RTLRegister reg = (RTLRegister) module;

        try {
			RTLPort z = reg.getZ();
		
			if (aPort == z) {
				//aSimulator.setValue(aPort, aValue);
				
				return;
			}	

			char oclk, nclk;

			RTLPort clk = reg.getClk();
			oclk = aSimulator.getValue(clk).getBit();
			if (clk == aPort) {
				nclk = aValue.getBit();
			} else
				nclk = oclk;

			ZILValue ov = aSimulator.getValue(z);

			aSimulator.setDelta(aPort, aValue);
			
			ZILValue adv, sdv, aev, sev;
			
			aev = aSimulator.getValue(reg.getASyncEnable());
			adv = aSimulator.getValue(reg.getASyncData());
			sev = aSimulator.getValue(reg.getSyncEnable());
			sdv = aSimulator.getValue(reg.getSyncData());
			
			logger.debug("Simulator: %s", "       [REG] aev="+aev+" adv="+adv+" sev="+sev+" sdv="+sdv);
			
			ZILValue res = computeReg (oclk, nclk, aev, adv, sev, sdv, ov);
			
			aSimulator.setDelta(z, res);
			
		} catch (ZamiaException e) {
			el.logException(e);
			throw  new ZamiaException(e.toString());
		}

	}

	private ZILValue computeReg(char oclk, char nclk, ZILValue aev, ZILValue adv, ZILValue sev, ZILValue sdv, ZILValue ov) throws ZamiaException {
		
		ZILType t = adv.getType();

		if ((t instanceof ZILTypeInteger)
				|| (t instanceof ZILTypeReal)
				|| (t instanceof ZILTypePhysical)
				|| (t instanceof ZILTypeEnum)) {

			if (aev.getBit() == ZILValue.BIT_1)
				return adv;

			if (sev.getBit() == ZILValue.BIT_1 && oclk == ZILValue.BIT_0 && nclk == ZILValue.BIT_1) {
				return sdv;
			}
			
			return ov;
		} else if (t instanceof ZILTypeArray) {

			ZILTypeArray at = (ZILTypeArray) t;

			ZILValue res = new ZILValue(at, null, null);

			// FIXME: lower/upper bounds
			int n = (int) at.getIndexType().getCardinality();
			for (int i = 0; i < n; i++) {

				res.setValue(i, computeReg(oclk, nclk, aev.getValue(i), adv.getValue(i), sev.getValue(i), sdv.getValue(i), ov.getValue(i)));
			}

			return res;
		} else if (t instanceof ZILTypeRecord) {

			ZILTypeRecord rt = (ZILTypeRecord) t;

			ZILValue res = new ZILValue(rt, null, null);

			int n = rt.getNumRecordFields();
			for (int i = 0; i < n; i++) {
				ZILRecordField rf = rt.getRecordField(i);

				res.setValue(rf,computeReg(oclk, nclk, aev.getValue(rf), adv.getValue(rf), sev.getValue(rf), sdv.getValue(rf), ov.getValue(rf)));
			}

			return res;

		} else
			throw new ZamiaException(
					"Internal error: Don't know how to compute value for " + t);
	}

}
