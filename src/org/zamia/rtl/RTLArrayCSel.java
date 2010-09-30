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


/**
 * 
 * @author guenter bartsch
 *
 */
@SuppressWarnings("serial")
public class RTLArrayCSel extends RTLModule {

	private RTLPort d;
	private ZILType inType;
	private ZILType indexType;
	private ZILType resType;
	private HashMapArray<Integer,OutputConn> outputs = new HashMapArray<Integer, OutputConn>();

	class OutputConn implements Serializable {
		int index;
		RTLPort port;
		public OutputConn(int index_, RTLPort port_) {
			index = index_;
			port = port_;
		}
	}

	public RTLArrayCSel (ZILType inType_, ZILType indexType_, ZILType resType_, RTLGraph parent_, String instanceName_, ASTObject src_) {
		super(parent_, instanceName_, src_);

		inType = inType_;
		indexType = indexType_;
		resType = resType_;
		
		try {
			d = createPort(RTLPort.d_str, inType, PortDir.IN, src_);
		} catch (ZamiaException e) {
			el.logZamiaException(e);
		}
	}
	
	@Override
	public String getClassName() {
		return "RTLArrayCSel";
	}

	public RTLPort getD() {
		return d;
	}

	public void dissolve() throws ZamiaException {
//		NetList nl_ = cache_.getNetList();
//		
//		int resWidth = type.getWidth();
//		int aWidth = cache_.getType(a).getWidth();
//		int selWidth = cache_.getType(s).getWidth();
//
//		Signal res = nl_.createUnnamedSignal(type, this);
//		Signal as = a.elaborate(cache_);
//
//		if (!(as instanceof SignalAggregate)) {
//			return nl_.sigJoin(as, res, this);
//		}
//		SignalAggregate aBus = (SignalAggregate) as;
//
//		if (s.isConstant(cache_)) {
//
//			int sel = s.getInt(cache_);
//
//			for (int resIdx = 0; resIdx < resWidth; resIdx++) {
//
//				SignalBit target;
//
//				if (res instanceof SignalBit) {
//					target = (SignalBit) res;
//				} else {
//					target = ((SignalAggregate) res).getSignal(resIdx);
//				}
//
//				SignalBit source = aBus.getSignal (resIdx + sel * resWidth);
//
//				nl_.sigJoin(source, target, this);
//			}
//		} else {
//
//			Signal sel = s.elaborate(cache_);
//
//			for (int resIdx = 0; resIdx < resWidth; resIdx++) {
//
//				SignalBit target;
//
//				if (res instanceof SignalBit) {
//					target = (SignalBit) res;
//				} else {
//					target = ((SignalAggregate) res).getSignal(resIdx);
//				}
//
//				// create a list of signal bits that should be
//				// muxed into target
//
//				ArrayList<SignalBit> sources = new ArrayList<SignalBit>();
//				int aIdx = resIdx;
//				while (aIdx < aWidth) {
//
//					sources.add(aBus.getSignal(aIdx));
//
//					aIdx += resWidth;
//				}
//
//				// now, create a muxer tree of these
//				int selIdx = 0;
//				while (sources.size() > 1) {
//					ArrayList<SignalBit> newSources = new ArrayList<SignalBit>();
//
//					SignalBit selBit = null;
//					if (sel instanceof SignalBit) {
//						if (selIdx == 0)
//							selBit = (SignalBit) sel;
//					} else {
//						if (selIdx < selWidth)
//							selBit = ((SignalAggregate)sel).getSignal( reverseIndex(selIdx, selWidth));
//					}
//					selIdx++;
//
//					int i = 0;
//					while (i < sources.size()) {
//						SignalBit s1 = (SignalBit) sources.get(i);
//						i++;
//						if (i < sources.size()) {
//							SignalBit s2 = (SignalBit) sources.get(i);
//							i++;
//
//							if (selBit != null)
//								newSources.add(nl_.placeMux(s1, s2, selBit, this));
//							else
//								newSources.add(s1);
//						} else
//							newSources.add(s1);
//					}
//
//					sources = newSources;
//				}
//
//				nl_.sigJoin((Signal) sources.get(0), target, this);
//			}
//		}
//
//		return res.getCurrent();

		// FIXME: implement
		throw new ZamiaException ("Internal error: Sorry, MultiMux.dissolve not implemented yet.");
	}

	public ZILType getIndexType() {
		return indexType;
	}

	@Override
	public int getNumOutputs() {
		return outputs.size();
	}

	public RTLPort addOutput(int idx_, ASTObject src_) throws ZamiaException {
		
		OutputConn conn = outputs.get(new Integer(idx_));
		if (conn == null) {
			RTLPort d = createPort("Z"+idx_, resType, PortDir.OUT, src_);
			conn = new OutputConn(idx_, d);
			outputs.put(idx_, conn);
		}
		
		return conn.port;
	}
	
	public void connectOutput(int idx_, RTLSignal s_, ASTObject src_) throws ZamiaException {
		RTLPort port = addOutput(idx_, src_);
		
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

	public int getOutputOffset(int i) {
		return outputs.get(i).index;
	}
	
	public RTLPort getOutputForIndex(int idx_) {
		
		OutputConn out = outputs.get(new Integer(idx_));
		
		if (out == null)
			return null;
		
		return out.port;
	}
	
	@Override
	public boolean equals(RTLModule module2_) {
		
		if (!(module2_ instanceof RTLArrayCSel))
			return false;
		
		RTLArrayCSel csel2 = (RTLArrayCSel) module2_;

		RTLSignal d = getD().getSignal();
		RTLSignal d2 = csel2.getD().getSignal();
		if (d != d2)
			return false;
		
		int n = getNumOutputs();
		for (int i = 0; i<n; i++) {
			
			RTLPort out = getOutput(i);
			RTLSignal signal = out.getSignal();
			int idx = getOutputOffset(i);
			
			RTLPort out2 = csel2.getOutputForIndex(idx);
			
			if (out2 == null)
				return false;
			RTLSignal signal2 = out2.getSignal();
			if (signal != signal2)
				return false;
			
		}
		
		return false;
	}
}
