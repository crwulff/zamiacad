/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 10, 2010
 */
package org.zamia.vg;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public interface VGLabelProvider<NodeType, PortType, SignalType> {

	/*
	 * nodes
	 */
	
	public VGSymbol<NodeType, PortType, SignalType> getNodeSymbol(NodeType aNode, VGLayout<NodeType, PortType, SignalType> aLayout);
	
	public String getNodeLabel(NodeType aNode);
	
	/*
	 * signals
	 */
	
	public String getSignalLabel(SignalType aSignal);
	
	public int getSignalWidth(SignalType aSignal);
	
	/*
	 * ports
	 */

	public String getPortLabel(PortType aPort);

	public int getPortWidth(PortType aPort);
	
}
