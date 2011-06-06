/* 
 * Copyright 2010,2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 5, 2010
 */
package org.zamia.analysis.ig;

import java.util.HashSet;

import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.vg.VGContentProvider;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGRSVisualGraphContentProvider implements VGContentProvider<IGRSNode, IGRSPort, IGRSSignal> {

	private IGRSResult fRoot;

	private boolean fDynamicMode = false; // false -> everything is visible

	private HashSet<IGRSPort> fExpandedPorts = new HashSet<IGRSPort>();

	private HashSet<IGRSNode> fVisibleNodes = new HashSet<IGRSNode>();

	public IGRSVisualGraphContentProvider(IGRSResult aRoot) {
		fRoot = aRoot;
	}

	@Override
	public IGRSNode getRoot() {
		return fRoot.getRoot();
	}

	@Override
	public int getNumChildren(IGRSNode aParent) {

		return aParent.getNumSubs();

	}

	@Override
	public IGRSNode getChild(IGRSNode aParent, int aIdx) {
		return aParent.getSub(aIdx);
	}

	@Override
	public int getNumPorts(IGRSNode aNode) {
		return aNode.getNumPorts();
	}

	@Override
	public IGRSPort getPort(IGRSNode aNode, int aIdx) {
		return aNode.getPort(aIdx);
	}

	@Override
	public boolean isOutput(IGRSPort aPort) {
		return aPort.getDirection() != OIDir.IN;
	}

	@Override
	public IGRSNode getNode(IGRSPort aPort) {
		return aPort.getNode();
	}

	@Override
	public IGRSSignal getSignal(IGRSPort aPort) {
		return aPort.getSignal();
	}

	@Override
	public int getSignalNumConnections(IGRSSignal aSignal) {
		return aSignal.getNumConns();
	}

	@Override
	public IGRSPort getSignalConnection(IGRSSignal aSignal, int aIdx) {
		return aSignal.getConn(aIdx);
	}

	@Override
	public boolean isNodeVisible(IGRSNode aNode) {
		if (!fDynamicMode)
			return true;
		return fVisibleNodes.contains(aNode);
	}

	@Override
	public boolean isPortExpanded(IGRSPort aPort) {
		if (!fDynamicMode)
			return true;
		return fExpandedPorts.contains(aPort);
	}

	public void setNodeVisible(IGRSNode aNode, boolean aVisible) {
		if (aVisible) {
			fVisibleNodes.add(aNode);
		} else {
			fVisibleNodes.remove(aNode);
		}
	}

	public void setPortExpanded(IGRSPort aPort, boolean aExpanded) {
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

	public void expandPort(IGRSPort aPort) {
		setPortExpanded(aPort, true);

		IGRSSignal signal = aPort.getSignal();
		if (signal != null) {

			int n = signal.getNumConns();

			for (int i = 0; i < n; i++) {

				IGRSPort p = signal.getConn(i);

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
