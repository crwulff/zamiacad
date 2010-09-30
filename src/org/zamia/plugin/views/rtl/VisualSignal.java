/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.plugin.views.rtl;

import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.util.HashSetArray;
import org.zamia.zil.ZILType;


/**
 * 
 * @author guenter bartsch
 *
 */
public class VisualSignal {

	private VisualGraph vg;
	private RTLSignal signal;
	private HashSetArray<VisualPort> conns;
	
	public VisualSignal(VisualGraph vg_, RTLSignal s_) {
		vg = vg_;
		signal = s_;
		conns = new HashSetArray<VisualPort>();
	}

	public void addPortConn(VisualPort port_) {
		conns.add(port_);
	}

	public RTLSignal getRTLSignal() {
		return signal;
	}

	public VisualGraph getGraph() {
		return vg;
	}

	public int getNumConns() {
		return conns.size();
	}

	public VisualPort getConn(int idx_) {
		return conns.get(idx_);
	}

	public ZILType getType() {
		return signal.getType();
	}

	public String getId() {
		return signal.getId();
	}

	public void removePortConn(VisualPort visualPort) {
		conns.remove(visualPort);
	}

	public int getNumDrivers() {
		
		int nDrivers = 0;
		
		int n = conns.size();
		for (int i = 0; i<n; i++) {
			VisualPort p = conns.get(i);
			if (p.getDirection() != PortDir.IN)
				nDrivers++;
		}
		
		return nDrivers;
	}

	public VisualPort getDriver(int idx_) {
		int n = conns.size();
		int count = 0;
		for (int i = 0; i<n; i++) {
			VisualPort p = conns.get(i);
			if (p.getDirection() == PortDir.IN)
				continue;
			if (count == idx_)
				return p;
			count++;
			
		}
		return null;
	}
	
}
