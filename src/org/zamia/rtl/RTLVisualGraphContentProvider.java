/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 5, 2010
 */
package org.zamia.rtl;

import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.vg.VGContentProvider;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class RTLVisualGraphContentProvider implements VGContentProvider<RTLNode, RTLPort, RTLSignal> {

	private RTLModule fRoot;

	public RTLVisualGraphContentProvider(RTLModule aRoot) {
		fRoot = aRoot;
	}

	@Override
	public RTLNode getRoot() {
		return fRoot;
	}

	@Override
	public int getNumChildren(RTLNode aParent) {

		if (!(aParent instanceof RTLModule))
			return 0;

		RTLModule module = (RTLModule) aParent;

		return module.getNumNodes();
	}

	@Override
	public RTLNode getChild(RTLNode aParent, int aIdx) {
		if (!(aParent instanceof RTLModule))
			return null;

		RTLModule module = (RTLModule) aParent;

		return module.getNode(aIdx);
	}

	@Override
	public int getNumPorts(RTLNode aNode) {
		return aNode.getNumPorts();
	}

	@Override
	public RTLPort getPort(RTLNode aNode, int aIdx) {
		return aNode.getPort(aIdx);
	}

	@Override
	public boolean isOutput(RTLPort aPort) {
		return aPort.getDirection() != PortDir.IN;
	}

	@Override
	public RTLNode getNode(RTLPort aPort) {
		return aPort.getNode();
	}

	@Override
	public RTLSignal getSignal(RTLPort aPort) {
		return aPort.getSignal();
	}

	@Override
	public int getSignalNumConnections(RTLSignal aSignal) {
		return aSignal.getNumConns();
	}

	@Override
	public RTLPort getSignalConnection(RTLSignal aSignal, int aIdx) {
		return aSignal.getConn(aIdx);
	}

}
