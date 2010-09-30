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
import org.zamia.zil.ZILTypeRecord;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class RTLTargetRecordCSel extends RTLModule {

	private RTLPort z, ze;
	private ZILType tIn, tOut;
	private HashMapArray<String,InputConn> inputs = new HashMapArray<String, InputConn>();

	class InputConn implements Serializable {
		String id;
		RTLPort port;
		public InputConn(String id_, RTLPort port_) {
			id = id_;
			port = port_;
		}
	}
	
	public RTLTargetRecordCSel(ZILType tIn_, ZILType tOut_,
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

	public RTLPort addInput(String id_, ASTObject src_) throws ZamiaException {
		
		InputConn conn = inputs.get(id_);
		if (conn == null) {
			RTLPort d = createPort("D"+id_, tIn, PortDir.IN, src_);
			conn = new InputConn(id_, d);
			inputs.put(id_, conn);
		}
		
		return conn.port;
	}
	
	public void connectInput(String id_, RTLSignal s_, ASTObject src_) throws ZamiaException {
		RTLPort port = addInput(id_, src_);
		
		if (s_ != null) {
		RTLSignal s = port.getSignal();
			if (s == null) {
				port.setSignal(s_);
			} else {
				RTLGraph graph = getParent();
				graph.sigJoin(s, s_, src_);
			}
		}
	}

	@Override
	public String getClassName() {
		return "RTLTargetRecordCSel";
	}

	public int getNumInputs () {
		return inputs.size();
	}
	
	public String getInputId(int idx_) {
		return inputs.get(idx_).id;
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

	public boolean isComplete() {
		
		if (!(tOut instanceof ZILTypeRecord))
			return false;
		
		ZILTypeRecord str = (ZILTypeRecord) tOut;

		int nFields = str.getNumRecordFields();
		
		for (int i = 0; i<nFields; i++) {
			
			String id = str.getRecordField(i).id;
			
			if (!inputs.containsKey(id))
				return false;
		}
		
		return true;
	}
	
	public RTLPort getInputForId(String id_) {
		return inputs.get(id_).port;
	}

	@Override
	public boolean equals(RTLModule module2_) {
		
		if (!(module2_ instanceof RTLTargetRecordCSel))
			return false;
		
		RTLTargetRecordCSel csel2 = (RTLTargetRecordCSel) module2_;

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
			String id = getInputId(i);
			
			RTLPort input2 = csel2.getInputForId(id);
			
			if (input2 == null)
				return false;
			RTLSignal signal2 = input2.getSignal();
			if (signal != signal2)
				return false;
			
		}
		
		return false;
	}

}
