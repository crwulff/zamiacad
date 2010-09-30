/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.rtl.sim.behaviors;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLGraph;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLPortModule;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.sim.IRTLModuleBehavior;
import org.zamia.rtl.sim.Simulator;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 *
 */
public class GraphBehavior implements IRTLModuleBehavior {

	public void init(RTLModule module_, Simulator sim_) throws ZamiaException {

		RTLGraph graph = (RTLGraph) module_;

		int n = graph.getNumSignals();
		for (int i = 0; i < n; i++) {
			RTLSignal s = graph.getSignal(i);

			sim_.init(s);
		}
		
		n = graph.getNumSubs();
		for (int i = 0; i < n; i++) {
			RTLModule module = graph.getSub(i);
			sim_.init(module);
		}
	}

	public void setPort(RTLPort p_, ZILValue v_, Simulator sim_)
			throws ZamiaException {
        //sim_.setValue(p_, v_);
        
        RTLGraph graph = (RTLGraph) p_.getModule();
        
        RTLPortModule pm = graph.getPortModule(p_);
        
        RTLPort ip = pm.getInternalPort();
        
        sim_.setDelta(ip, v_);
        
	}

}
