/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 24, 2008
 */
package org.zamia.zil.interpreter;

import org.zamia.ZamiaException;
import org.zamia.rtl.sim.Simulator;
import org.zamia.vhdl.ast.VHDLNode;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class ZILBuiltinFunctionStmt extends ZILStmt {

	public enum BUILTINS {
		CONV_INTEGER, CONV_STD_LOGIC_VECTOR, TO_INTEGER, TO_UNSIGNED, WRITE, NOW, ENDFILE, WRITELINE, READLINE, READ, DEALLOCATE, STD_MATCH
	};

	private BUILTINS f;

	public ZILBuiltinFunctionStmt(BUILTINS f_, VHDLNode src_) throws ZamiaException {
		super(src_);

		f = f_;

		//		switch (f) {
		//		case CONV_STD_LOGIC_VECTOR:
		//			if (!(resType instanceof ZILTypeArray))
		//				throw new ZamiaException ("Internal error: return type mismatch in builtin funtion");
		//			
		//		}

	}

	@Override
	public String toString() {
		return "BUILTIN " + f.name();
	}

	@Override
	public ReturnStatus execute(Simulator sim_, ZILInterpreterRuntimeEnv runtime_, ZILInterpreter comp) throws ZamiaException {

//		switch (f) {
//		case CONV_STD_LOGIC_VECTOR:
//		case TO_UNSIGNED:
//
//			ZILStackFrame sizeSF = runtime_.pop();
//			ZILStackFrame valueSF = runtime_.pop();
//
//			int size = sizeSF.getInt(sim_);
//
//			ZILTypeDiscrete idxType = ZILType.intType.createSubType(size - 1, 0, false, null, null, null);
//
//			ZILTypeArray at = new ZILTypeArray(idxType, ZILType.bit, null, null, null);
//
//			String s = ZILValue.convert(valueSF.getInt(sim_), size);
//
//			ZILValue res = new ZILValue(s, at, null, null);
//
//			runtime_.push(res);
//
//			break;
//
//		case CONV_INTEGER:
//
//			valueSF = runtime_.pop();
//			ZILValue v = valueSF.getValue(sim_);
//
//			ZILType t = v.getType();
//
//			if (t instanceof ZILTypeArray) {
//				ZILTypeArray sta = (ZILTypeArray) t;
//
//				if (!sta.isLogic())
//					throw new ZamiaException("CONV_INTEGER called on a non-logic arrray");
//
//				res = new ZILValue(ZILValue.getInt(v.toString()), ZILType.intType, null, null);
//
//			} else {
//				res = new ZILValue(v.getInt(getSource()), ZILType.intType, null, null);
//
//			}
//
//			runtime_.push(res);
//			break;
//
//		default:
//			throw new ZamiaException("Not implemented builtin function called.");
//
//		}
		return ReturnStatus.CONTINUE;
	}

}

//class conv_std_logic_vector implements SubProgramExec {
//
//	public void executeSubProgram(Stack<StackFrame> stack_, RTLInterpreter comp, Simulator sim_) throws SimException,
//			ZamiaException {
//
//		// FIXME: re-implement using Values instead of Strings
//
//		throw new SimException("Internal error: unimplemented method called.");
//
//		// StackFrame sfBits = stack_.pop();
//		// StackFrame sfVal = stack_.pop();
//		//
//		// int bitNum = sfBits.getIntValue(sim_);
//		// String value = sfVal.getStringValue(sim_);
//		//
//		// BigInteger bi = new BigInteger(value, 2);
//		// StringBuffer buf = new StringBuffer(bi.toString(2));
//		//
//		// if (buf.length() > bitNum)
//		// throw new SimException("ZILValue is outside range");
//		//
//		// char sign = '1';
//		// if (bi.intValue() >= 0)
//		// sign = '0';
//		//
//		// int ext = bitNum - buf.length();
//		//
//		// for (int i = 0; i < ext; i++)
//		// buf.insert(0, sign);
//		//
//		// String s = buf.toString();
//		//
//		// ZILType idxType = new ZILType(0, s.length() - 1, true, null, null);
//		//
//		// ZILType type = new ZILType(idxType, ZILType.bit, null, null);
//		// stack_.push(new StackFrame(s, type));
//		//
//	}
//
//	public ZILType getType(ArrayList<Operation> params, OperationCache cache_) throws ZamiaException {
//
//		Operation bits = params.get(1);
//		int width = bits.getConstant(cache_, ZILType.intType, null).getInt();
//
//		ZILTypeInteger idxType = new ZILTypeInteger(width - 1, 0, false, null, null, null);
//
//		return new ZILTypeArray(idxType, ZILType.bit, null, null);
//	}
//}
