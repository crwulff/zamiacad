/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on May 11, 2007
 */

package org.zamia.rtl;

import java.io.Serializable;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.util.HashMapArray;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.ZILType;
import org.zamia.zil.ZILTypeArray;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class RTLTargetArray extends RTLModule {

	private RTLPort z;
	private ZILType tIn;
	private ZILTypeArray tOut;
	private HashMapArray<Integer,InputConn> inputs = new HashMapArray<Integer, InputConn>();

	class InputConn implements Serializable {
		int index;
		RTLPort port;
		public InputConn(int index_, RTLPort port_) {
			index = index_;
			port = port_;
		}
	}
	
	public RTLTargetArray(ZILType tIn_, ZILTypeArray tOut_,
			RTLGraph parent_, String instanceName_, VHDLNode src_) {
		super(parent_, instanceName_, src_);

		try {

			tIn = tIn_;
			tOut = tOut_;
			z = createPort(RTLPort.z_str, tOut, PortDir.OUT, src_);

		} catch (ZamiaException e) {
			el.logZamiaException(e);
		}
	}

	public RTLPort addInput(int idx_, VHDLNode src_) throws ZamiaException {
		
		InputConn conn = inputs.get(new Integer(idx_));
		if (conn == null) {
			RTLPort d = createPort("D"+idx_, tIn, PortDir.IN, src_);
			conn = new InputConn(idx_, d);
			inputs.put(idx_, conn);
		}
		
		return conn.port;
	}
	
	public void connectInput(int idx_, RTLSignal s_, VHDLNode src_) throws ZamiaException {
		RTLPort port = addInput(idx_, src_);
		
		RTLSignal s = port.getSignal();
		if (s == null) {
			port.setSignal(s_);
		} else {
			RTLGraph graph = getParent();
			graph.sigJoin(s, s_, src_);
		}
	}

	@Override
	public String getClassName() {
		return "RTLTargetArray";
	}

	public int getNumInputs () {
		return inputs.size();
	}
	
	public int getInputOffset(int index_) {
		return inputs.get(index_).index;
	}
	
	public RTLPort getInput(int index_) {
		return inputs.get(index_).port;
	}

	public RTLPort getZ() {
		return z;
	}

	public ZILType getTIn() {
		return tIn;
	}

	public ZILTypeArray getOutputType() {
		return tOut;
	}
}
