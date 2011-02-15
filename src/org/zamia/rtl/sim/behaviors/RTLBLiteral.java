/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 17, 2010
 */
package org.zamia.rtl.sim.behaviors;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLNode;
import org.zamia.rtl.RTLValue;
import org.zamia.rtl.nodes.RTLNLiteral;
import org.zamia.rtl.sim.RTLNodeBehavior;
import org.zamia.rtl.sim.RTLPortSimAnnotation;
import org.zamia.rtl.sim.RTLSimContext;
import org.zamia.rtl.sim.RTLSimulator;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class RTLBLiteral implements RTLNodeBehavior {

	@Override
	public void reset(RTLNode aNode, RTLSimulator aSimulator, RTLSimContext aContext) throws ZamiaException {
		RTLNLiteral l = (RTLNLiteral) aNode;

		RTLValue v = l.getValue();

		RTLPortSimAnnotation pz = aContext.findPortSimAnnotation(l.getZ());
		pz.setDriving(true);

		aSimulator.setDelta(pz, v);
	}

	@Override
	public void portChange(RTLPortSimAnnotation aPA, RTLValue aValue, RTLSimulator aSimulator) throws ZamiaException {
	}

}
