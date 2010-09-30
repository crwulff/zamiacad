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
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.ZILType;
import org.zamia.zil.ZILTypeArray;
import org.zamia.zil.ZILTypeDiscrete;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class RTLTargetArrayCSel extends RTLModule {

	private RTLPort z, ze;
	private ZILType tIn, tOut;
	private HashMapArray<Integer,InputConn> inputs = new HashMapArray<Integer, InputConn>();

	class InputConn implements Serializable {
		int index;
		RTLPort port;
		public InputConn(int index_, RTLPort port_) {
			index = index_;
			port = port_;
		}
	}
	
	public RTLTargetArrayCSel(ZILType tIn_, ZILType tOut_,
			RTLGraph parent_, String instanceName_, ASTObject src_) {
		super(parent_, instanceName_, src_);

		try {

			tIn = tIn_;
			tOut = tOut_;
			z = createPort(RTLPort.z_str, tOut, PortDir.OUT, src_);
			ze = createPort(RTLPort.ze_str, tOut, PortDir.OUT, src_);

		} catch (ZamiaException e) {
			el.logZamiaException(e);
		}
	}

	public RTLPort addInput(int idx_, ASTObject src_) throws ZamiaException {
		
		InputConn conn = inputs.get(new Integer(idx_));
		if (conn == null) {
			RTLPort d = createPort("D"+idx_, tIn, PortDir.IN, src_);
			conn = new InputConn(idx_, d);
			inputs.put(idx_, conn);
		}
		
		return conn.port;
	}
	
	public void connectInput(int idx_, RTLSignal s_, ASTObject src_) throws ZamiaException {
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
		return "RTLTargetArrayCSel";
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

	public RTLPort getZE() {
		return ze;
	}

	public ZILType getTIn() {
		return tIn;
	}

	public boolean isComplete() throws ZamiaException {
		
		if (!(tOut instanceof ZILTypeArray))
			return false;
		
		ZILTypeArray sta = (ZILTypeArray) tOut;
		
		ZILTypeDiscrete idxT = sta.getIndexType();
		if (idxT == null)
			return false;
		
		int min = (int) idxT.getLow().getInt(getSource());
		int max = (int) idxT.getHigh().getInt(getSource());
		
		for (int i = min; i<=max; i++) {
			
			if (!inputs.containsKey(i))
				return false;
		}
		
		return true;
	}
	
	public RTLPort getInputForIndex(int idx_) {
		return inputs.get(new Integer(idx_)).port;
	}

	@Override
	public boolean equals(RTLModule module2_) {
		
		if (!(module2_ instanceof RTLTargetArrayCSel))
			return false;
		
		RTLTargetArrayCSel csel2 = (RTLTargetArrayCSel) module2_;

		RTLSignal z = getZ().getSignal();
		RTLSignal z2 = csel2.getZ().getSignal();
		if (z != z2)
			return false;
		RTLSignal ze = getZE().getSignal();
		RTLSignal ze2 = csel2.getZE().getSignal();
		if (ze != ze2)
			return false;
		
		int n = getNumInputs();
		for (int i = 0; i<n; i++) {
			
			RTLPort input = getInput(i);
			RTLSignal signal = input.getSignal();
			int idx = getInputOffset(i);
			
			RTLPort input2 = csel2.getInputForIndex(idx);
			
			if (input2 == null)
				return false;
			RTLSignal signal2 = input2.getSignal();
			if (signal != signal2)
				return false;
			
		}
		
		return false;
	}

}
