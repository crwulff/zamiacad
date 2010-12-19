/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 17, 2010
 */
package org.zamia.rtlng.sim.behaviors;

import org.zamia.ZamiaException;
import org.zamia.rtlng.RTLNode;
import org.zamia.rtlng.RTLValue;
import org.zamia.rtlng.nodes.RTLNLiteral;
import org.zamia.rtlng.sim.RTLNodeBehavior;
import org.zamia.rtlng.sim.RTLPortSimAnnotation;
import org.zamia.rtlng.sim.RTLSimContext;
import org.zamia.rtlng.sim.RTLSimulator;

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
