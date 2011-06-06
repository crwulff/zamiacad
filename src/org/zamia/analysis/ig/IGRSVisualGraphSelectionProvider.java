/* 
 * Copyright 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 18, 2011
 */
package org.zamia.analysis.ig;

import java.util.HashSet;

import org.zamia.vg.VGSelectionProvider;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGRSVisualGraphSelectionProvider implements VGSelectionProvider<IGRSNode, IGRSSignal> {

	private final HashSet<IGRSNode> fSelectedNodes = new HashSet<IGRSNode>();

	private final HashSet<IGRSSignal> fSelectedSignals = new HashSet<IGRSSignal>();

	public void clear() {
		fSelectedNodes.clear();
		fSelectedSignals.clear();
	}

	public void setSignalSelection(IGRSSignal aSignal, boolean aSelected) {
		if (aSelected) {
			fSelectedSignals.add(aSignal);
		} else {
			fSelectedSignals.remove(aSignal);
		}
	}

	public void setNodeSelection(IGRSNode aNode, boolean aSelected) {
		if (aSelected) {
			fSelectedNodes.add(aNode);
		} else {
			fSelectedNodes.remove(aNode);
		}
	}

	@Override
	public boolean isNodeSelected(IGRSNode aNode) {
		return fSelectedNodes.contains(aNode);
	}

	@Override
	public boolean isSignalSelected(IGRSSignal aSignal) {
		return fSelectedSignals.contains(aSignal);
	}

}
