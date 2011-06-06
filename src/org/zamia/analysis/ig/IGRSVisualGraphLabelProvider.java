/* 
 * Copyright 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 10, 2010
 */
package org.zamia.analysis.ig;

import org.zamia.vg.VGLabelProvider;
import org.zamia.vg.VGLayout;
import org.zamia.vg.VGSymbol;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGRSVisualGraphLabelProvider implements VGLabelProvider<IGRSNode, IGRSPort, IGRSSignal> {

	private static final int SIGNAL_WIDTH_BIT = 1;

	private static final int PORT_WIDTH_BIT = 1;

	public IGRSVisualGraphLabelProvider(IGRSResult aModule) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public VGSymbol<IGRSNode, IGRSPort, IGRSSignal> getNodeSymbol(IGRSNode aNode, VGLayout<IGRSNode, IGRSPort, IGRSSignal> aLayout) {
		return null;
	}

	@Override
	public String getNodeLabel(IGRSNode aNode) {
		return aNode.getInstanceName();
	}

	@Override
	public String getSignalLabel(IGRSSignal aSignal) {
		return aSignal.getId();
	}

	@Override
	public String getPortLabel(IGRSPort aPort) {
		return aPort.getId();
	}

	@Override
	public int getSignalWidth(IGRSSignal aSignal) {
		return SIGNAL_WIDTH_BIT;
	}

	@Override
	public int getPortWidth(IGRSPort aPort) {
		return PORT_WIDTH_BIT;
	}

}
