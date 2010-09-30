/*
 * Copyright 2008,2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Nov 28, 2008
 */

package org.zamia.rtl;

import org.zamia.ComponentStub;
import org.zamia.ZamiaException;
import org.zamia.vhdl.ast.ASTObject;


/**
 * 
 * @author guenter bartsch
 * 
 */

@SuppressWarnings("serial")
public class RTLComponent extends RTLModule {

	private ComponentStub stub;

//	private HashMap<RTLSignal, RTLPort> signalMap;

	public RTLComponent(ComponentStub stub_, RTLGraph parent_, String instanceName_, ASTObject src_) throws ZamiaException {
		super(parent_, instanceName_, src_);

		stub = stub_;

	}

	@Override
	public String getClassName() {

		// FIXME: use DUUID.getID() ?

		return "Component";
	}

	//	public void setFSM(FSM fsm_) throws ZamiaException {
	//
	//		RTLSignal s = fsm.getStateSignal();
	//		getSignalPort(s, PortDir.OUT);
	//		s = fsm.getNextStateSignal();
	//		getSignalPort(s, PortDir.OUT);
	//
	//		int n = fsm.getNumOutputs();
	//		for (int i = 0; i < n; i++) {
	//			RTLSignal so = fsm.getOutput(i);
	//			getSignalPort(so, PortDir.OUT);
	//		}
	//
	//		// HashSetArray<String> inputs = fsm.getInputs();
	//		// n = inputs.size();
	//		// for (int i = 0; i<n; i++) {
	//		// String input = inputs.get(i);
	//		// RTLSignal si = getParent().findSignal(input);
	//		// getSignalPort(si, PortDir.IN);
	//		// }
	//
	//	}

//	public RTLPort lookupSignalPort(RTLSignal s_) {
//		return signalMap.get(s_);
//	}

//	public DUUID getDUUID() {
//		return stub.getDUUID();
//	}

	public ComponentStub getStub() {
		return stub;
	}

	//	private RTLPort getSignalPort(RTLSignal s_, PortDir dir_)
	//			throws ZamiaException {
	//
	//		IntermediateObject src = getSource();
	//
	//		RTLPort port = signalMap.get(s_);
	//		if (port == null) {
	//			port = createPort(s_.getId(), s_.getType(), dir_, src);
	//
	//			if (!port.getType().isCompatible(s_.getType()))
	//				throw new ZamiaException("Type mismatch", src, null);
	//
	//			port.setSignal(s_);
	//
	//			signalMap.put(s_, port);
	//		}
	//		return port;
	//	}

}
