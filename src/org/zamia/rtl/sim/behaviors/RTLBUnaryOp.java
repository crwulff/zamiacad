/*
 * Copyright 2007,2009,2010,2011 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on May 19, 2007
 */

package org.zamia.rtl.sim.behaviors;

import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.rtl.RTLNode;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLType;
import org.zamia.rtl.RTLValue;
import org.zamia.rtl.RTLValueBuilder;
import org.zamia.rtl.RTLValue.BitValue;
import org.zamia.rtl.nodes.RTLNUnaryOp;
import org.zamia.rtl.nodes.RTLNUnaryOp.UnaryOp;
import org.zamia.rtl.sim.RTLNodeBehavior;
import org.zamia.rtl.sim.RTLPortSimAnnotation;
import org.zamia.rtl.sim.RTLSimContext;
import org.zamia.rtl.sim.RTLSimulator;

/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class RTLBUnaryOp implements RTLNodeBehavior {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	@Override
	public void portChange(RTLPortSimAnnotation aPA, RTLValue aValue, RTLSimulator aSimulator) throws ZamiaException {

		RTLPort port = aPA.getPort();

		RTLNode node = port.getNode();

		RTLNUnaryOp uop = (RTLNUnaryOp) node;

		RTLPort z = uop.getZ();

		if (port == z)
			return;

		aPA.setValue(aValue);

		RTLSimContext context = aPA.getContext();

		RTLPortSimAnnotation a = context.findPortSimAnnotation(uop.getA());
		UnaryOp op = uop.getOp();

		RTLType t = a.getPort().getType();

		RTLValue va = a.getValue();

		logger.debug("RTLSimulator: %s", "Unary operation, type=" + t + ", a=" + va + ", op=" + op);

		SourceLocation location = port.computeSourceLocation();

		RTLValue vz = null;

		switch (t.getCat()) {
		case BIT:
			switch (op) {
			case NOT:

				switch (va.getBit()) {
				case BV_0:
					vz = RTLValueBuilder.generateBit(t, BitValue.BV_1, location, aSimulator.getZDB());
					break;

				case BV_1:
					vz = RTLValueBuilder.generateBit(t, BitValue.BV_0, location, aSimulator.getZDB());
					break;

				case BV_U:
				case BV_X:
				case BV_Z:
					vz = RTLValueBuilder.generateBit(t, BitValue.BV_X, location, aSimulator.getZDB());
					break;
				}
				break;

			case BUF:
				vz = va;
				break;
			}
			break;
		}

		if (vz != null) {
			aSimulator.setDelta(context.findPortSimAnnotation(z), vz);
			return;
		}

		// FIXME: implement other operations
		throw new ZamiaException("Sorry, not implemented yet.");

	}

	@Override
	public void reset(RTLNode aNode, RTLSimulator aSimulator, RTLSimContext aContext) throws ZamiaException {
		RTLNUnaryOp uop = (RTLNUnaryOp) aNode;

		RTLPortSimAnnotation pz = aContext.findPortSimAnnotation(uop.getZ());
		pz.setDriving(true);
	}

}
