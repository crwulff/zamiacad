/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 9, 2010
 */
package org.zamia.vg;

/**
 * Wrapper around underlying port type
 * 
 * Main reason this exists is that we have VGBoxes for primary inputs/outputs
 * which have no gate (and therefore no port) counterparts in the underlying model
 * 
 * Also, it is nice to have a complete abstraction layer here so we can
 * store additional information in the future and do not have to fall back
 * to VGContentProvider during place&route once the VG* wrappers have been
 * generated.
 * 
 * @author Guenter Bartsch
 *
 * @param <NodeType>
 * @param <PortType>
 * @param <SignalType>
 */

public class VGPort<NodeType, PortType, SignalType> {

	private final PortType fPort;

	private final VGBox<NodeType, PortType, SignalType> fBox;

	private final VGLayout<NodeType, PortType, SignalType> fLayout;

	private final boolean fOutput;

	private final VGContentProvider<NodeType, PortType, SignalType> fContentProvider;

	private VGSignal<NodeType, PortType, SignalType> fSignal;

	private VGLabelProvider<NodeType, PortType, SignalType> fLabelProvider;

	private final String fLabel;

	private final int fWidth;

	public VGPort(int aWidth, PortType aPort, VGBox<NodeType, PortType, SignalType> aBox, VGLayout<NodeType, PortType, SignalType> aLayout) {
		fWidth = aWidth;
		fPort = aPort;
		fBox = aBox;
		fLayout = aLayout;
		fContentProvider = fLayout.getContentProvider();
		fLabelProvider = fLayout.getLabelProvider();
		fOutput = fContentProvider.isOutput(fPort);
		fLabel = fLabelProvider.getPortLabel(fPort); 
	}

	public VGPort(int aWidth, PortType aPort, boolean aOutput, VGBox<NodeType, PortType, SignalType> aBox, VGLayout<NodeType, PortType, SignalType> aLayout) {
		fWidth = aWidth;
		fPort = aPort;
		fBox = aBox;
		fLayout = aLayout;
		fContentProvider = fLayout.getContentProvider();
		fLabelProvider = fLayout.getLabelProvider();
		fOutput = aOutput;
		fLabel = fLabelProvider.getPortLabel(fPort); 
	}

	public VGBox<NodeType, PortType, SignalType> getBox() {
		return fBox;
	}

	public boolean isOutput() {
		return fOutput;
	}

	public void connect(VGSignal<NodeType, PortType, SignalType> aSignal) {
		fSignal = aSignal;
		fSignal.connect(this);
	}
	
	public VGSignal<NodeType, PortType, SignalType> getSignal() {
		return fSignal;
	}

	public PortType getPort() {
		return fPort;
	}

	public String getLabel() {
		return fLabel;
	}

	public int getWidth() {
		return fWidth;
	}

	@Override
	public String toString() {
		return "VGPort["+fLabel+"]";
	}
}
