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
import org.zamia.vhdl.ast.OperationMath.MathOp;
import org.zamia.zil.ZILType;


/**
 * A math operation node in the RTL Graph
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class RTLOperationMath extends RTLModule {

	private MathOp op;
	private RTLPort a, b, z;
	private ZILType inType, outType;

	public RTLOperationMath (MathOp op_, ZILType inType_, ZILType outType_, RTLGraph parent_, String instanceName_, ASTObject src_) {
		super(parent_, instanceName_, src_);

		op = op_;
		inType = inType_;
		outType = outType_;
		
		try {
			a = createPort(RTLPort.a_str, inType, PortDir.IN, src_);
			if (op != MathOp.ABS && op != MathOp.NEG)
				b = createPort(RTLPort.b_str, inType, PortDir.IN, src_);
			z = createPort(RTLPort.z_str, outType, PortDir.OUT, src_);
		} catch (ZamiaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public String getClassName() {
		return "RTLOpMath";
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

	public MathOp getOp() {
		return op;
	}
	
//	private void genCLA(NetList nl_, Signal a_, Signal b_, Signal z_,
//			SignalBit carryin_) throws ZamiaException {
//
//		// special case: 1-bit adder
//		if (a_ instanceof SignalBit) {
//
//			SignalBit asb = (SignalBit) a_;
//			SignalBit bsb = (SignalBit) b_;
//
//			SignalBit res = nl_.placeSimpleLogic(new Xor2(nl_, getSource()), asb, bsb, getSource());
//
//			nl_.sigJoin(res, (SignalBit) z_, getSource());
//			return;
//		}
//
//		SignalAggregate a = (SignalAggregate) a_;
//		SignalAggregate b = (SignalAggregate) b_;
//
//		// generate and propagate signals:
//
//		int n = a.getNumSignals();
//
//		ArrayList<SignalBit> generates = new ArrayList<SignalBit>(n);
//		ArrayList<SignalBit> propagates = new ArrayList<SignalBit>(n);
//		ArrayList<SignalBit> hsum = new ArrayList<SignalBit>(n);
//
//		for (int i = 0; i < n; i++) {
//			SignalBit ab = a.getSignal(i);
//			SignalBit bb = b.getSignal(i);
//
//			generates.add(nl_.placeSimpleLogic(new And2(nl_), ab, bb, getSource()));
//			propagates.add(nl_.placeSimpleLogic(new Or2(nl_), ab, bb, getSource()));
//			hsum.add(nl_.placeSimpleLogic(new Xor2(nl_,getSource()), ab, bb, getSource()));
//
//		}
//
//		// carry logic
//
//		ArrayList<SignalBit> carries = new ArrayList<SignalBit>(n + 1);
//		for (int i = 0; i <= n; i++) {
//			if (i == 0) {
//				carries.add(carryin_);
//			} else {
//				carries.add(nl_.placeSimpleLogic(new Or2(nl_), 
//						generates.get(i-1), 
//						nl_.placeSimpleLogic(new And2(nl_), propagates.get(i-1), carries.get(i-1), getSource())
//						,getSource()));
//			}
//		}
//
//		// generate result
//
//		String id = nl_.getUnnamedSignalId();
//		SignalAggregate res = new SignalAggregate(nl_, id, type, null, getSource());
//
//		for (int i = 0; i < n; i++) {			
//			SignalBit resb = nl_.placeSimpleLogic(new Xor2(nl_, getSource()), (SignalBit) hsum.get(i), (SignalBit) carries.get(i), null);
//			res.add(resb);
//		}
//		nl_.add (res);
//		
//		nl_.sigJoin(res, z_, null);
//		
//	}
//	
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
//		Signal asb = getSignal(a);
//		Signal bsb = getSignal(b);
//		Signal zsb = getSignal(z);
//
//		switch (operation) {
//			case OperationMath.OP_NEG : /* * (-1) */
//				// FIXME: implement
//				throw new ZamiaException(
//						"Sorry, dissolve for negation not implemented yet.");
//			case OperationMath.OP_ADD :
//
//				genCLA(nl, asb, bsb, zsb, nl.getZero());
//
//				break;
//			case OperationMath.OP_SUB :
//				Signal res = nl.placeSimpleLogic(new Not1(nl), bsb, null, getSource());
//				genCLA(nl, asb, res, zsb, nl.getOne());
//				break;
//			default :
//				// FIXME: implement mul, div, ...
//				throw new ZamiaException(
//						"Sorry, dissolve for operation math #" + operation
//								+ " not implemented yet.");
//		}
//	}
//
//
	
//	public static void setPort(MathOp gate_, RTLPort port_, char value_,
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
//		if (b != null){
//			bv = sim_.getValue(b);
//			if (!b.getType().isAscending()){
//				StringBuffer buf = new StringBuffer(bv);
//				bv = buf.reverse().toString();
//			}
//		}
//
//		BigInteger ai = Value.getBigInt(av);
//		BigInteger bi = null;
//		if (bv != null)
//			bi = Value.getBigInt(bv);
//
//		BigInteger zi = ai;
//
//		switch (gate_.getOp()) {
//		case OperationMath.OP_NEG:
//			zi = bi.multiply(new BigInteger("-1"));
//			break;
//		case OperationMath.OP_ADD:
//			zi = ai.add(bi);
//			break;
//		case OperationMath.OP_SUB:
//			zi = ai.subtract(bi);
//			break;
//		case OperationMath.OP_MUL:
//			zi = ai.multiply(bi);
//			break;
//		case OperationMath.OP_DIV:
//			zi = ai.divide(bi);
//			break;
//		case OperationMath.OP_MOD:
//			zi = ai.mod(bi);
//			break;
//		case OperationMath.OP_REM:
//			zi = ai.remainder(bi);
//			break;
//		case OperationMath.OP_POWER:
//			zi = ai.pow(bi.intValue());
//			break;
//		case OperationMath.OP_ABS:
//			zi = ai.abs();
//			break;
//		default:
//			throw new SimException("Internal sim error: unknown math op "
//					+ gate_.getOp());
//		}
//		String zv = Value.convert(zi, gate_.getType().getWidth());
//		Port z = gate_.getZ();
//		
//		if (!z.getType().isAscending())
//		{
//			StringBuffer buf = new StringBuffer(zv);
//			zv = buf.reverse().toString();
//		}
//		
//		sim_.scheduleEvent(DELAY, z, zv);
//		//System.out.println("Gate " + gate_.getInstanceName()+". A = "+av+" B = "+ bv +"Port z scheduled to: " + zv);
//
//	}


	@Override
	public boolean equals(RTLModule module2_) {
		
		if (!(module2_ instanceof RTLOperationMath))
			return false;
		
		RTLOperationMath math2 = (RTLOperationMath) module2_;
		
		if (math2.getOp() != getOp())
			return false;
		
		return super.equals(module2_);
	}

}
