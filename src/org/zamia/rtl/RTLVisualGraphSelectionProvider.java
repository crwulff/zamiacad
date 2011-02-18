/* 
 * Copyright 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 18, 2011
 */
package org.zamia.rtl;

import java.util.HashSet;

import org.zamia.vg.VGSelectionProvider;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class RTLVisualGraphSelectionProvider implements VGSelectionProvider<RTLNode, RTLSignal> {

	private final HashSet<RTLNode> fSelectedNodes = new HashSet<RTLNode>();

	private final HashSet<RTLSignal> fSelectedSignals = new HashSet<RTLSignal>();

	public void clear() {
		fSelectedNodes.clear();
		fSelectedSignals.clear();
	}

	public void setSignalSelection(RTLSignal aSignal, boolean aSelected) {
		if (aSelected) {
			fSelectedSignals.add(aSignal);
		} else {
			fSelectedSignals.remove(aSignal);
		}
	}

	public void setNodeSelection(RTLNode aNode, boolean aSelected) {
		if (aSelected) {
			fSelectedNodes.add(aNode);
		} else {
			fSelectedNodes.remove(aNode);
		}
	}

	@Override
	public boolean isNodeSelected(RTLNode aNode) {
		return fSelectedNodes.contains(aNode);
	}

	@Override
	public boolean isSignalSelected(RTLSignal aSignal) {
		return fSelectedSignals.contains(aSignal);
	}

}
