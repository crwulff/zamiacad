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
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLNode;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.RTLValue;
import org.zamia.rtl.RTLValueBuilder;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.rtl.sim.RTLNodeBehavior;
import org.zamia.rtl.sim.RTLPortSimAnnotation;
import org.zamia.rtl.sim.RTLSignalSimAnnotation;
import org.zamia.rtl.sim.RTLSimContext;
import org.zamia.rtl.sim.RTLSimulator;

public class RTLBModule implements RTLNodeBehavior {

	@Override
	public void reset(RTLNode aNode, RTLSimulator aSimulator, RTLSimContext aContext) throws ZamiaException {

		RTLModule module = (RTLModule) aNode;

		int n = module.getNumPorts();
		for (int i = 0; i < n; i++) {
			RTLPort port = module.getPort(i);

			RTLPortSimAnnotation pa = aContext.findPortSimAnnotation(port);

			// primary ports have opposite direction if viewed from inside
			pa.setDriving(port.getDirection() == PortDir.IN);
		}

		
		n = module.getNumSignals();
		for (int i = 0; i < n; i++) {
			RTLSignal signal = module.getSignal(i);

			RTLSignalSimAnnotation si = aContext.findSignalSimAnnotation(signal);
			
			RTLValue v = signal.getInitialValue();
			if (v == null) {
				v = RTLValueBuilder.generateUValue(signal.getType(), null, aSimulator.getZDB());
			}
			si.add(0, v, false);
		}
		
		n = module.getNumNodes();
		for (int i = 0; i < n; i++) {
			RTLNode node = module.getNode(i);
			aSimulator.reset(node, aContext);
		}
	}

	@Override
	public void portChange(RTLPortSimAnnotation aPA, RTLValue aValue, RTLSimulator aSimulator) throws ZamiaException {
		// TODO Auto-generated method stub
		
	}


}
