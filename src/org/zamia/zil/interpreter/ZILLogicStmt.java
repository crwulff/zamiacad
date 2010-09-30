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
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.vhdl.ast.OperationLogic.LogicOp;
import org.zamia.zil.ZILType;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class ZILLogicStmt extends ZILStmt {

	private LogicOp op;

	public ZILLogicStmt(LogicOp op_, ASTObject src_) {
		super(src_);
		op = op_;
	}

	@Override
	public ReturnStatus execute(Simulator sim_, ZILInterpreterRuntimeEnv runtime_, ZILInterpreter comp_) throws ZamiaException {
		ZILStackFrame sf2 = null;
		ZILValue op2 = null;
		ZILType t2 = null;

		if ((op != LogicOp.BUF) && (op != LogicOp.NOT)) {
			sf2 = runtime_.pop();
			op2 = sf2.getValue(sim_);
			t2 = op2.getType();
		}

		ZILStackFrame sf1 = runtime_.pop();
		ZILValue op1 = sf1.getValue(sim_);
		ZILType t1 = op1.getType();

		if (t2 != null && !t1.isCompatible(t2))
			throw new ZamiaException("Type mismatch.");

		ZILValue res = ZILValue.computeLogic(op1, op2, op, getSource());

		//System.out.println("executing:   PUSH LOGIC RESULT: " + res);

		runtime_.push(new ZILStackFrame(res));

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "LOGIC OP #" + op;
	}
}
