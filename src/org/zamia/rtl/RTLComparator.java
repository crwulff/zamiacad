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
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.vhdl.ast.OperationCompare.CompareOp;
import org.zamia.zil.ZILType;


/**
 * 
 * @author guenter bartsch
 *
 */
@SuppressWarnings("serial")
public class RTLComparator extends RTLModule {

	private CompareOp op;
	private RTLPort a, b, z;
	private ZILType type;

	public RTLComparator (CompareOp op_, ZILType type_, RTLGraph parent_, String instanceName_, VHDLNode src_) {
		super(parent_, instanceName_, src_);

		op = op_;
		type = type_;
		
		try {
			a = createPort(RTLPort.a_str, type, PortDir.IN, src_);
			b = createPort(RTLPort.b_str, type, PortDir.IN, src_);
			z = createPort(RTLPort.z_str, ZILType.bit, PortDir.OUT, src_);
		} catch (ZamiaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public String getClassName() {
		return "RTLComparator";
	}

	public RTLPort getA() {
		return a;
	}

	public RTLPort getB() {
		return b;
	}

	public RTLPort getZ() {
		return z;
	}

	public CompareOp getOp() {
		return op;
	}

//	private Signal getSignal (Port p_) {
//		if (p_ instanceof RTLPort) 
//			return ((RTLPort) p_).getSignal();
//		
//		PortAggregate pa = (PortAggregate) p_;
//		int n = pa.getNumPorts();
//		
//		SignalAggregate res = new SignalAggregate(getParent(), null, p_.getType(), null, null);
//		
//		for (int i = 0; i<n; i++) {
//			
//			RTLPort pb = pa.getPort(i);
//			
//			res.add(pb.getSignalBit());
//		}
//		return res;
//	}
//	
//	public void dissolve() throws ZamiaException {
//
//		NetList nl = getParent();
//
//		Signal as = getSignal(a);
//		Signal bs = getSignal(b);
//		Signal zs = getSignal(z);
//
//		switch (operation) {
//		case OperationCompare.OP_EQUAL:
//			SignalBit new_zs = nl.createUnnamedSignalBit(getSource());
//			SignalBit tmp = nl.placeSimpleLogic(new Not1(nl), new_zs, null, getSource());
//			nl.sigJoin(tmp, zs, getSource());
//			zs = new_zs;
//		case OperationCompare.OP_NEQUAL:
//			if (as instanceof SignalBit) {
//				SignalBit asb = (SignalBit) as;
//				SignalBit bsb = (SignalBit) bs;
//
//				SignalBit res = nl.placeSimpleLogic(new Xor2(nl, getSource()), asb, bsb, getSource());
//
//				nl.sigJoin(res, (SignalBit) zs, getSource());
//				return;
//			} else {
//				SignalAggregate aa = (SignalAggregate) as;
//				SignalAggregate ba = (SignalAggregate) bs;
//				int n = aa.getNumSignals();
//				ArrayList<SignalBit> diffs = new ArrayList<SignalBit>(n);
//
//				for (int i = 0; i < n; i++) {
//					SignalBit ab = aa.getSignal(i);
//					SignalBit bb = ba.getSignal(i);
//
//					diffs.add(nl.placeSimpleLogic(new Xor2(nl, getSource()), ab, bb, getSource()));
//				}
//				SignalBit zb = nl.placeSimpleLogicTree(new Or2(nl), diffs, getSource());
//				nl.sigJoin(zb, (SignalBit) zs, getSource());
//			}
//			break;
//		case OperationCompare.OP_GREATER:
//		case OperationCompare.OP_GREATEREQ:
//		case OperationCompare.OP_LESS:
//		case OperationCompare.OP_LESSEQ:
//			// FIXME: implement
//			throw new ZamiaException("Sorry, dissolve for cmp operation "
//					+ operation + " is not implemented yet.");
//
//		}
//	}
//

//	public static void setPort(Cmp gate_, RTLPort port_, char value_,
//			Simulator sim_) throws SimException {
//
//		sim_.setValue(port_, value_);
//
//		// is this our output port?
//
//		if (port_.getDirection() == Port.DIR_OUT) {
//			return;
//		}
//
//		Port a = gate_.getA();
//		Port b = gate_.getB();
//
//		String av = sim_.getValue(a);
//		if (!a.getType().isAscending()){
//			StringBuffer buf = new StringBuffer(av);
//			av = buf.reverse().toString();
//		}
//		
//		String bv = null;
//		
//		bv = sim_.getValue(b);
//		if (!b.getType().isAscending()){
//			StringBuffer buf = new StringBuffer(bv);
//			bv = buf.reverse().toString();
//		}
//		
//		Port z = gate_.getZ();
//		
//		if (av.contains("X") || bv.contains("X")){
//			sim_.scheduleEvent(DELAY, z, "X");
//		}
//
//		BigInteger ai = Value.getBigInt(av);
//    	BigInteger bi = Value.getBigInt(bv);
//
//		String zv;
//
//		switch (gate_.getOp()) {
//		case OperationCompare.OP_EQUAL:
//			if (ai.equals(bi))
//				zv = "1";
//			else
//				zv = "0";
//			break;
//		case OperationCompare.OP_NEQUAL:
//			if (!ai.equals(bi))
//				zv = "1";
//			else
//				zv = "0";
//			break;
//		default:
//			throw new SimException("Internal sim error: unknown cmp op "
//					+ gate_.getOp());
//		}
//			
//		sim_.scheduleEvent(DELAY, z, zv);
//		//System.out.println("Gate " + gate_.getInstanceName()+". A = "+av+" B = "+ bv +"Port z scheduled to: " + zv);
//
//	}

}
