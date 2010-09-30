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
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.vhdl.ast.OperationCompare.CompareOp;
import org.zamia.zil.ZILType;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class ZILCompareStmt extends ZILStmt {

	private CompareOp op;

	public ZILCompareStmt(CompareOp op_, ASTObject src_) {
		super(src_);
		op = op_;
	}

	@Override
	public ReturnStatus execute(Simulator sim_, ZILInterpreterRuntimeEnv runtime_, ZILInterpreter comp_) throws ZamiaException {

		ZILStackFrame sf2 = runtime_.pop();
		ZILValue op2 = sf2.getValue(sim_);
		ZILType t2 = op2.getType();

		ZILStackFrame sf1 = runtime_.pop();
		ZILValue op1 = sf1.getValue(sim_);
		ZILType t1 = op1.getType();

		if (t2 != null && !t1.isCompatible(t2))
			throw new ZamiaException("Type mismatch.");

		ZILValue res = ZILValue.computeCompare(op1, op2, op, null, getSource());

		//System.out.println("executing:   PUSH LOGIC RESULT: " + res);

		runtime_.push(new ZILStackFrame(res));

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "COMPARE OP #" + op;
	}
}
