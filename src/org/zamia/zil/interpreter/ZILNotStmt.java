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
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class ZILNotStmt extends ZILStmt {

	public ZILNotStmt(ASTObject src_) {
		super(src_);
	}

	@Override
	public ReturnStatus execute(Simulator sim_, ZILInterpreterRuntimeEnv runtime_, ZILInterpreter comp_) throws ZamiaException {

		//System.out.println("executing:   NOT");

		ZILStackFrame sf = runtime_.pop();

		ZILValue v = sf.getValue(sim_);

		runtime_.push(new ZILStackFrame(v.not()));

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "NOT";
	}
}
