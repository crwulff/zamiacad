/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on May 11, 2007
 */

package org.zamia.rtl;


import org.zamia.ZamiaException;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.ZILType;


/**
 * 
 * @author guenter bartsch
 *
 */
@SuppressWarnings("serial")
public class RTLArraySel extends RTLModule {

	private RTLPort a, s, z;
	private ZILType inType;
	private ZILType indexType;
	private ZILType resType;

	public RTLArraySel (ZILType inType_, ZILType indexType_, ZILType resType_, RTLGraph parent_, String instanceName_, ASTObject src_) {
		super(parent_, instanceName_, src_);

		inType = inType_;
		indexType = indexType_;
		resType = resType_;
		
		try {
			a = createPort(RTLPort.a_str, inType, PortDir.IN, src_);
			s = createPort(RTLPort.s_str, indexType, PortDir.IN, src_);
			z = createPort(RTLPort.z_str, resType, PortDir.OUT, src_);
		} catch (ZamiaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public String getClassName() {
		return "RTLArraySel";
	}

	public RTLPort getA() {
		return a;
	}

	public RTLPort getS() {
		return s;
	}

	public RTLPort getZ() {
		return z;
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

}
