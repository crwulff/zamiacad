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


/**
 * fixed, elaborated record selector in the RTL Graph
 * 
 * @author Guenter Bartsch
 */

@SuppressWarnings("serial")
public class RTLRecordSel extends RTLModule {

	private RTLPort a;
	private ZILType inType;
	private ZILType resType;
	private HashMapArray<String,OutputConn> outputs = new HashMapArray<String, OutputConn>() ;

	class OutputConn implements Serializable {
		String id;
		RTLPort port;
		public OutputConn(String id_, RTLPort port_) {
			id = id_;
			port = port_;
		}
	}

	public RTLRecordSel (ZILType inType_, ZILType resType_, RTLGraph parent_, String instanceName_, VHDLNode src_) {
		super(parent_, instanceName_, src_);

		inType = inType_;
		resType = resType_;
		
		try {
			a = createPort(RTLPort.a_str, inType, PortDir.IN, src_);
		} catch (ZamiaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public RTLPort addOutput(String id_, VHDLNode src_) throws ZamiaException {
		
		OutputConn conn = outputs.get(id_);
		if (conn == null) {
			RTLPort d = createPort("Z"+id_, resType, PortDir.OUT, src_);
			conn = new OutputConn(id_, d);
			outputs.put(id_, conn);
		}
		
		return conn.port;
	}
	
	public void connectOutput(String id_, RTLSignal s_, VHDLNode src_) throws ZamiaException {
		RTLPort port = addOutput(id_, src_);
		
		RTLSignal s = port.getSignal();
		if (s == null) {
			port.setSignal(s_);
		} else {
			RTLGraph graph = getParent();
			graph.sigJoin(s, s_, src_);
		}
	}

	public RTLPort getOutput(int i) {
		return outputs.get(i).port;
	}

	public String getOutputId(int i) {
		return outputs.get(i).id;
	}

	@Override
	public int getNumOutputs() {
		return outputs.size();
	}

	@Override
	public String getClassName() {
		return "RTLRecordSel";
	}

	public RTLPort getA() {
		return a;
	}

	public void dissolve() throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException ("Internal error: Sorry, MultiMux.dissolve not implemented yet.");
	}

	@Override
	public boolean equals(RTLModule module2_) {
		
		if (!(module2_ instanceof RTLRecordSel))
			return false;
		
		RTLRecordSel csel2 = (RTLRecordSel) module2_;

		RTLSignal a = getA().getSignal();
		RTLSignal a2 = csel2.getA().getSignal();
		if (a != a2)
			return false;
		
		int n = getNumOutputs();
		for (int i = 0; i<n; i++) {
			
			RTLPort out = getOutput(i);
			RTLSignal signal = out.getSignal();
			String id = getOutputId(i);
			
			RTLPort out2 = csel2.getOutputForId(id);
			
			if (out2 == null)
				return false;
			RTLSignal signal2 = out2.getSignal();
			if (signal != signal2)
				return false;
			
		}
		
		return false;
	}

	private RTLPort getOutputForId(String id) {
		OutputConn out = outputs.get(id);
		if (out == null)
			return null;
		return out.port;
	}

}
