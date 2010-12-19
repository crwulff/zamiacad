/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 11, 2010
 */
package org.zamia.vg;

import org.zamia.util.Position;

/**
 * A symbol is a graphical representation of a node
 * 
 * @author Guenter Bartsch
 *
 */

public interface VGSymbol<NodeType, PortType, SignalType> {

	public int getWidth();

	public int getHeight();

	public Position getPortPosition(PortType aPort);

	public void unTweakPortPosition(PortType aPort);

	public void tweakPortPosition(PortType aPort);
	
	public void paint(NodeType aModule, int aXPos, int aYPos, boolean aHilight);
}
