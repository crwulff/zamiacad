/* 
 * Copyright 2008 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Aug 22, 2008
 */
package org.zamia.plugin.views.rtl;

import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.util.HashSetArray;


/**
 * RTLPort counterpart
 * 
 * @author Guenter Bartsch
 *
 */

public class VisualPort {
	
	private VisualModule module;
	private HashSetArray<VisualPort> connections;
	private RTLPort port;
	private VisualSignal signal;

	public VisualPort (VisualModule module_, RTLPort port_) {
		module = module_;
		port = port_;
		connections = new HashSetArray<VisualPort>();
	}
	
	public PortDir getDirection() {
		return port.getDirection();
	}
	
	public int getNumConnections() {
		return connections.size();
	}
	
	public VisualPort getConnection(int i_) {
		return connections.get(i_);
	}
	
	public void addConnection(VisualPort port_) {
		connections.add(port_);
	}
	
	public VisualModule getModule() {
		return module;
	}

	public VisualSignal getSignal() {
		return signal;
	}

	public RTLPort getRTLPort() {
		return port;
	}
	
	public void setSignal(VisualSignal signal_) {
		if (signal == signal_)
			return;

		if (signal != null)
			signal.removePortConn(this);

		if (signal_ != null) {
			signal_.addPortConn(this);
		}
		signal = signal_;
	}

}
