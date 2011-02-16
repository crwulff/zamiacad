/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 8, 2010
 */
package org.zamia.vg;

import java.util.ArrayList;

import org.zamia.util.HashSetArray;
import org.zamia.util.Position;

/**
 * Wrapper around underlying graph nodes
 * 
 * contains utility functions (getSuccessors, num connected inputs, ...)
 * and layout information
 * 
 * @author Guenter Bartsch
 *
 */

public class VGBox<NodeType, PortType, SignalType> {

	private final NodeType fNode;

	private final PortType fPrimaryPort;

	private final VGContentProvider<NodeType, PortType, SignalType> fContentProvider;

	private final VGLabelProvider<NodeType, PortType, SignalType> fLabelProvider;

	private final VGLayout<NodeType, PortType, SignalType> fLayout;

	private final VGSymbol<NodeType, PortType, SignalType> fSymbol;

	private ArrayList<VGPort<NodeType, PortType, SignalType>> fInputs;

	private ArrayList<VGPort<NodeType, PortType, SignalType>> fOutputs;

	private ArrayList<VGPort<NodeType, PortType, SignalType>> fPorts;

	private ArrayList<VGBox<NodeType, PortType, SignalType>> fDrivers;

	private ArrayList<VGBox<NodeType, PortType, SignalType>> fReceivers;

	private int fCol;

	private int fYPos;

	/**
	 * One of the parameters has to be NULL - a VGBox can represent
	 * either a node or a primary input/output
	 * 
	 * @param aNode
	 * @param aPrimaryPort
	 */
	public VGBox(NodeType aNode, PortType aPrimaryPort, VGLayout<NodeType, PortType, SignalType> aLayout, HashSetArray<VGPort<NodeType, PortType, SignalType>> aExpandablePorts) {

		fLayout = aLayout;
		fContentProvider = fLayout.getContentProvider();
		fLabelProvider = fLayout.getLabelProvider();

		fNode = aNode;
		fPrimaryPort = aPrimaryPort;

		fPorts = new ArrayList<VGPort<NodeType, PortType, SignalType>>();
		fInputs = new ArrayList<VGPort<NodeType, PortType, SignalType>>();
		fOutputs = new ArrayList<VGPort<NodeType, PortType, SignalType>>();

		if (fPrimaryPort != null) {

			/*
			 * create artificial internal port with opposite direction
			 * (primare input is _driving_ the internal circuit
			 * so needs to be an output viewed from inside)
			 * 
			 */

			int width = fLabelProvider.getPortWidth(fPrimaryPort);
			VGPort<NodeType, PortType, SignalType> internalPort = new VGPort<NodeType, PortType, SignalType>(width, fPrimaryPort, !fContentProvider.isOutput(fPrimaryPort), this, fLayout);

			fPorts.add(internalPort);

			SignalType signal = fContentProvider.getSignal(fPrimaryPort);

			if (signal != null) {
				if (fContentProvider.isPortExpanded(fPrimaryPort)) {
					VGSignal<NodeType, PortType, SignalType> s = fLayout.getOrCreateSignal(signal);
					internalPort.connect(s);
				} else {
					aExpandablePorts.add(internalPort);
				}
			}

		} else {

			int n = fContentProvider.getNumPorts(fNode);
			for (int i = 0; i < n; i++) {
				PortType port = fContentProvider.getPort(fNode, i);
				int width = fLabelProvider.getPortWidth(port);

				VGPort<NodeType, PortType, SignalType> p = new VGPort<NodeType, PortType, SignalType>(width, port, this, fLayout);

				fPorts.add(p);

				SignalType signal = fContentProvider.getSignal(port);

				if (signal != null) {

					if (fContentProvider.isPortExpanded(port)) {
						VGSignal<NodeType, PortType, SignalType> s = fLayout.getOrCreateSignal(signal);
						p.connect(s);
					} else {
						aExpandablePorts.add(p);
					}

				}
			}
		}

		if (fNode != null) {
			VGSymbol<NodeType, PortType, SignalType> symbol = fLabelProvider.getNodeSymbol(fNode, fLayout);
			if (symbol == null) {
				symbol = new VGGenericSymbol<NodeType, PortType, SignalType>(fNode, fLayout, this);
			}
			fSymbol = symbol;
		} else {
			fSymbol = new VGPortSymbol<NodeType, PortType, SignalType>(fContentProvider.isOutput(fPrimaryPort), fLabelProvider.getPortLabel(fPrimaryPort), fLayout);
		}

	}

	/**
	 * precompute all available information about this boxes connections
	 */
	void compute(VGContentProvider<NodeType, PortType, SignalType> aContentProvider) {

		fDrivers = new ArrayList<VGBox<NodeType, PortType, SignalType>>();
		fReceivers = new ArrayList<VGBox<NodeType, PortType, SignalType>>();

		// connected inputs and outputs

		int n = fPorts.size();

		for (int i = 0; i < n; i++) {

			VGPort<NodeType, PortType, SignalType> port = fPorts.get(i);

			if (port.isOutput()) {
				fOutputs.add(port);
			} else {
				fInputs.add(port);
			}

			VGSignal<NodeType, PortType, SignalType> signal = port.getSignal();

			if (signal == null) {
				continue;
			}

			int m = signal.getNumConnections();
			for (int j = 0; j < m; j++) {

				VGPort<NodeType, PortType, SignalType> conn = signal.getConnection(j);

				VGBox<NodeType, PortType, SignalType> box = conn.getBox();

				if (box == this) {
					continue;
				}

				if (port.isOutput()) {
					if (!conn.isOutput()) {
						fReceivers.add(box);
					}
				} else {
					if (conn.isOutput()) {
						fDrivers.add(box);
					}
				}
			}
		}
	}

	Position getPortPosition(VGPort<NodeType, PortType, SignalType> aPort) {
		Position p1 = getPortOffset(aPort);
		if (p1 == null)
			return new Position(getXPos(), getYPos());
		return new Position(p1.getX() + getXPos(), p1.getY() + getYPos());
	}

	Position getPortOffset(VGPort<NodeType, PortType, SignalType> aPort) {
		return fSymbol.getPortPosition(aPort.getPort());
	}

	int getNumDrivers() {
		return fDrivers.size();
	}

	int getNumReceivers() {
		return fReceivers.size();
	}

	VGBox<NodeType, PortType, SignalType> getReceiver(int aIdx) {
		return fReceivers.get(aIdx);
	}

	void setCol(int aCol) {
		fCol = aCol;
	}

	int getCol() {
		return fCol;
	}

	@Override
	public String toString() {
		if (fPrimaryPort != null) {
			return "VGBox[" + fPrimaryPort + "]";
		}
		return "VGBox[" + fNode + "]";
	}

	VGBox<NodeType, PortType, SignalType> getDriver(int aIdx) {
		return fDrivers.get(aIdx);
	}

	int getNumPorts() {
		return fPorts.size();
	}

	VGPort<NodeType, PortType, SignalType> getPort(int aIdx) {
		return fPorts.get(aIdx);
	}

	int getWidth() {
		return fSymbol.getWidth();
	}

	int getHeight() {
		return fSymbol.getHeight();
	}

	void setYPos(int yPos) {
		fYPos = yPos;
	}

	int getYPos() {
		return fYPos;
	}

	int getXPos() {
		VGChannel<NodeType, PortType, SignalType> channel = fLayout.getChannel(fCol);
		return channel.getModulesPos();
	}

	void paint(boolean aHighlight) {
		if (fSymbol != null) {
			fSymbol.paint(fNode, getXPos(), fYPos, aHighlight);
		}
	}

	VGSymbol<NodeType, PortType, SignalType> getSymbol() {
		return fSymbol;
	}

}
