/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 17, 2010
 */
package org.zamia.rtlng.sim.behaviors;

import org.zamia.ExceptionLogger;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.rtlng.RTLNode;
import org.zamia.rtlng.RTLPort;
import org.zamia.rtlng.RTLType;
import org.zamia.rtlng.RTLValue;
import org.zamia.rtlng.RTLValue.BitValue;
import org.zamia.rtlng.nodes.RTLNRegister;
import org.zamia.rtlng.sim.RTLNodeBehavior;
import org.zamia.rtlng.sim.RTLPortSimAnnotation;
import org.zamia.rtlng.sim.RTLSimContext;
import org.zamia.rtlng.sim.RTLSimulator;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class RTLBRegister implements RTLNodeBehavior {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	@Override
	public void reset(RTLNode aNode, RTLSimulator aSimulator, RTLSimContext aContext) throws ZamiaException {
		RTLNRegister reg = (RTLNRegister) aNode;

		RTLPortSimAnnotation pz = aContext.findPortSimAnnotation(reg.getZ());
		pz.setDriving(true);
	}

	@Override
	public void portChange(RTLPortSimAnnotation aPA, RTLValue aValue, RTLSimulator aSimulator) throws ZamiaException {

		RTLPort port = aPA.getPort();

		RTLNode node = port.getNode();

		RTLNRegister reg = (RTLNRegister) node;

		RTLSimContext context = aPA.getContext();

		RTLPort z = reg.getZ();

		if (port == z) {
			//aSimulator.setValue(aPort, aValue);

			return;
		}

		BitValue oclk;
		BitValue nclk;

		RTLPortSimAnnotation clk = context.findPortSimAnnotation(reg.getClk());

		oclk = clk.getValue().getBit();
		if (clk.getPort() == port) {
			nclk = aValue.getBit();
		} else
			nclk = oclk;

		RTLValue ov = context.findPortSimAnnotation(z).getValue();

		aPA.setValue(aValue);

		RTLValue aev = context.findPortSimAnnotation(reg.getASyncEnable()).getValue();
		RTLValue adv = context.findPortSimAnnotation(reg.getASyncData()).getValue();
		RTLValue sev = context.findPortSimAnnotation(reg.getSyncEnable()).getValue();
		RTLValue sdv = context.findPortSimAnnotation(reg.getSyncData()).getValue();

		logger.debug("RTLSimulator: %s", "       [REG] aev=" + aev + " adv=" + adv + " sev=" + sev + " sdv=" + sdv);

		RTLValue res = computeReg(oclk, nclk, aev, adv, sev, sdv, ov);

		aSimulator.setDelta(context.findPortSimAnnotation(z), res);

	}

	private RTLValue computeReg(BitValue oclk, BitValue nclk, RTLValue aev, RTLValue adv, RTLValue sev, RTLValue sdv, RTLValue ov) throws ZamiaException {

		RTLType t = adv.getType();

		switch (t.getCat()) {
		case BIT:
			if (aev.getBit() == BitValue.BV_1)
				return adv;

			if (sev.getBit() == BitValue.BV_1 && oclk == BitValue.BV_0 && nclk == BitValue.BV_1) {
				return sdv;
			}

			return ov;
		case ARRAY:
			//FIXME: implement
			throw new ZamiaException("Sorry, not implemented yet.");
		case RECORD:
			// FIXME: implement
			throw new ZamiaException("Sorry, not implemented yet.");
		}

		// FIXME: implement
		throw new ZamiaException("Sorry, not implemented yet.");
	}

}
