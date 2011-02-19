/* 
 * Copyright 2010,2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 5, 2010
 */
package org.zamia.rtl;

import java.util.HashSet;

import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.vg.VGContentProvider;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class RTLVisualGraphContentProvider implements VGContentProvider<RTLNode, RTLPort, RTLSignal> {

	private RTLModule fRoot;
	
	private boolean fDynamicMode = false; // false -> everything is visible
	
	private HashSet<RTLPort> fExpandedPorts = new HashSet<RTLPort>();
	
	private HashSet<RTLNode> fVisibleNodes = new HashSet<RTLNode>();

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

	@Override
	public boolean isNodeVisible(RTLNode aNode) {
		if (!fDynamicMode)
			return true;
		return fVisibleNodes.contains(aNode);
	}

	@Override
	public boolean isPortExpanded(RTLPort aPort) {
		if (!fDynamicMode)
			return true;
		return fExpandedPorts.contains(aPort);
	}
	
	public void setNodeVisible (RTLNode aNode, boolean aVisible) {
		if (aVisible) {
			fVisibleNodes.add(aNode);
		} else {
			fVisibleNodes.remove(aNode);
		}
	}
	
	public void setPortExpanded(RTLPort aPort, boolean aExpanded) {
		if (aExpanded) {
			fExpandedPorts.add(aPort);
		} else {
			fExpandedPorts.remove(aPort);
		}
	}

	@Override
	public boolean isDynamicMode() {
		return fDynamicMode;
	}

	public void setDynamicMode(boolean aDynamicMode) {
		fDynamicMode = aDynamicMode;
	}

	public void expandPort(RTLPort aPort) {
		setPortExpanded(aPort, true);
		
		RTLSignal signal = aPort.getSignal();
		if (signal != null) {
			
			int n = signal.getNumConns();
			
			for (int i = 0; i<n; i++ ) {
				
				RTLPort p = signal.getConn(i);
				
				setPortExpanded(p, true);
				
				setNodeVisible(p.getNode(), true);
			}
		}
		
	}

	public void clearVisibility() {
		fExpandedPorts.clear();
		fVisibleNodes.clear();
	}

}
