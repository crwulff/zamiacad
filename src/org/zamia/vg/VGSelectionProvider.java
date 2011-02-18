/* 
 * Copyright 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 18, 2011
 */
package org.zamia.vg;

/**
 * Used to highlight nodes and signals while painting a layout
 * 
 * @author Guenter Bartsch
 *
 */

public interface VGSelectionProvider<NodeType, SignalType> {

	public boolean isNodeSelected(NodeType aNode);
	
	public boolean isSignalSelected(SignalType aSignal);
	
}
