/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 9, 2010
 */
package org.zamia.vg;

import org.zamia.util.HashSetArray;

/**
 * Wrapper around underlying signal type
 * 
 * It is nice to have a complete abstraction layer here so we can
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

public class VGSignal<NodeType, PortType, SignalType> {

	private final SignalType fSignal;

	private final HashSetArray<VGPort<NodeType, PortType, SignalType>> fConnections;

	private final int fWidth;

	public VGSignal(SignalType aSignal, int aWidth, VGLayout<NodeType, PortType, SignalType> aLayout) {

		fSignal = aSignal;
		fWidth = aWidth;

		fConnections = new HashSetArray<VGPort<NodeType, PortType, SignalType>>();
	}

	public void connect(VGPort<NodeType, PortType, SignalType> aPort) {

		fConnections.add(aPort);

	}

	public int getNumConnections() {
		return fConnections.size();
	}

	public VGPort<NodeType, PortType, SignalType> getConnection(int aIdx) {
		return fConnections.get(aIdx);
	}

	public SignalType getSignal() {
		return fSignal;
	}

	public int getWidth() {
		return fWidth;
	}

	@Override
	public String toString() {
		return "VGSignal[" + fSignal + "]";
	}

}
