/* 
 * Copyright 2010, 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 5, 2010
 */
package org.zamia.vg;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public interface VGContentProvider<NodeType, PortType, SignalType> {

	public boolean isDynamicMode();
	
	/*
	 * nodes
	 */

	public NodeType getRoot();

	public int getNumChildren(NodeType aParent);

	public NodeType getChild(NodeType aParent, int aIdx);
	
	public boolean isNodeVisible(NodeType aNode);

	/*
	 * ports
	 */

	public int getNumPorts(NodeType aNode);

	public PortType getPort(NodeType aNode, int aIdx);

	public boolean isOutput(PortType aPort);

	public NodeType getNode(PortType aPort);
	
	public boolean isPortExpanded(PortType aPort);

	/*
	 * signals
	 */

	public SignalType getSignal(PortType aPort);

	public int getSignalNumConnections(SignalType aSignal);

	public PortType getSignalConnection(SignalType aSignal, int aIdx);
	
}
