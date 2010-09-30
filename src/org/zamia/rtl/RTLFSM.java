/*
 * Copyright 2007 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on May 11, 2007
 */

package org.zamia.rtl;

import java.util.HashMap;

import org.zamia.ZamiaException;
import org.zamia.fsm.FSM;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.vhdl.ast.ASTObject;


/**
 * 
 * @author guenter bartsch
 *
 */

@SuppressWarnings("serial")
public class RTLFSM extends RTLModule {

	private FSM fsm;
	private HashMap<RTLSignal, RTLPort> signalMap;

	public RTLFSM(FSM fsm_, RTLGraph parent_, String instanceName_,
			ASTObject src_) throws ZamiaException {
		super(parent_, instanceName_, src_);

		signalMap = new HashMap<RTLSignal, RTLPort>();
		setFSM(fsm_);
	}

	@Override
	public String getClassName() {
		return "FSM";
	}

	public void setFSM(FSM fsm_) throws ZamiaException {
		fsm = fsm_;

		RTLSignal s = fsm.getStateSignal();
		getSignalPort(s, PortDir.OUT);
		s = fsm.getNextStateSignal();
		getSignalPort(s, PortDir.OUT);

		int n = fsm.getNumOutputs();
		for (int i = 0; i < n; i++) {
			RTLSignal so = fsm.getOutput(i);
			getSignalPort(so, PortDir.OUT);
		}

		// HashSetArray<String> inputs = fsm.getInputs();
		// n = inputs.size();
		// for (int i = 0; i<n; i++) {
		// String input = inputs.get(i);
		// RTLSignal si = getParent().findSignal(input);
		// getSignalPort(si, PortDir.IN);
		// }

	}

	public RTLPort lookupSignalPort(RTLSignal s_) {
		return signalMap.get(s_);
	}

	private RTLPort getSignalPort(RTLSignal s_, PortDir dir_)
			throws ZamiaException {

		ASTObject src = getSource();

		RTLPort port = signalMap.get(s_);
		if (port == null) {
			port = createPort(s_.getId(), s_.getType(), dir_, src);

			if (!port.getType().isCompatible(s_.getType()))
				throw new ZamiaException("Type mismatch", src);

			port.setSignal(s_);

			signalMap.put(s_, port);
		}
		return port;
	}

	public FSM getFSM() {
		return fsm;
	}
}
