/* 
 * Copyright 2008 by the authors indicated in the @author tags. 
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
import org.zamia.vhdl.ast.OperationMath.MathOp;
import org.zamia.zil.ZILType;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class ZILMathStmt extends ZILStmt {

	private MathOp op;

	public ZILMathStmt(MathOp op_, VHDLNode src_) {
		super(src_);
		op = op_;
	}

	@Override
	public ReturnStatus execute(Simulator sim_, ZILInterpreterRuntimeEnv runtime_, ZILInterpreter comp_) throws ZamiaException {
		//System.out.println("executing:   MATH " + op);

		ZILStackFrame sf2, sf1;
		sf2 = null;

		ZILValue op1 = null;
		ZILValue op2 = null;
		ZILType t2 = null;

		if ((op != MathOp.NEG) && (op != MathOp.ABS)) {
			sf2 = runtime_.pop();
			op2 = sf2.getValue(sim_);
			t2 = op2.getType();
		}
		sf1 = runtime_.pop();
		op1 = sf1.getValue(sim_);

		ZILType t1 = op1.getType();

		if (t2 != null && !t1.isCompatible(t2))
			throw new ZamiaException("Type mismatch.");

		ZILValue resValue = ZILValue.computeMath(op1, op2, op, getSource());
		runtime_.push(new ZILStackFrame(resValue));

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "MATH OP #" + op;
	}
}
