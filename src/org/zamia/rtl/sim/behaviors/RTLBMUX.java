/*
 * Copyright 2007,2009,2010 by the authors indicated in the @author tags.
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
import org.zamia.rtl.RTLValue;
import org.zamia.rtl.RTLValueBuilder;
import org.zamia.rtl.nodes.RTLNMUX;
import org.zamia.rtl.sim.RTLNodeBehavior;
import org.zamia.rtl.sim.RTLPortSimAnnotation;
import org.zamia.rtl.sim.RTLSimContext;
import org.zamia.rtl.sim.RTLSimulator;

/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class RTLBMUX implements RTLNodeBehavior {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	@Override
	public void portChange(RTLPortSimAnnotation aPA, RTLValue aValue, RTLSimulator aSimulator) throws ZamiaException {

		RTLPort port = aPA.getPort();

		RTLNode node = port.getNode();

		RTLNMUX mux = (RTLNMUX) node;

		RTLPort z = mux.getZ();

		if (port == z)
			return;

		aPA.setValue(aValue);

		RTLSimContext context = aPA.getContext();

		RTLPortSimAnnotation a = context.findPortSimAnnotation(mux.getA());
		RTLPortSimAnnotation b = context.findPortSimAnnotation(mux.getB());
		RTLPortSimAnnotation s = context.findPortSimAnnotation(mux.getS());

		RTLValue va = a.getValue();
		RTLValue vb = b.getValue();
		RTLValue vs = s.getValue();

		logger.debug("RTLSimulator: %s", "MUX a=" + va + ", b=" + vb + ", s=" + s);

		SourceLocation location = port.computeSourceLocation();

		RTLValue vz = null;

		switch (vs.getBit()) {
		case BV_0:
			vz = vb;
			break;

		case BV_1:
			vz = va;
			break;

		case BV_U:
		case BV_X:
		case BV_Z:
			vz = RTLValueBuilder.generateUValue(va.getType(), location, aSimulator.getZDB());
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
		RTLNMUX mux = (RTLNMUX) aNode;

		RTLPortSimAnnotation pz = aContext.findPortSimAnnotation(mux.getZ());
		pz.setDriving(true);
	}

}
