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
import org.zamia.rtlng.RTLModule;
import org.zamia.rtlng.RTLNode;
import org.zamia.rtlng.RTLPort;
import org.zamia.rtlng.RTLPort.PortDir;
import org.zamia.rtlng.RTLSignal;
import org.zamia.rtlng.RTLValue;
import org.zamia.rtlng.RTLValueBuilder;
import org.zamia.rtlng.sim.RTLNodeBehavior;
import org.zamia.rtlng.sim.RTLPortSimAnnotation;
import org.zamia.rtlng.sim.RTLSignalSimAnnotation;
import org.zamia.rtlng.sim.RTLSimContext;
import org.zamia.rtlng.sim.RTLSimulator;

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
