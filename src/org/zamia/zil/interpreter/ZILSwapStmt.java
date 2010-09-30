/*
 * Copyright 2008-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
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
public class ZILSwapStmt extends ZILStmt {

	public ZILSwapStmt(ASTObject src_) {
		super(src_);
	}

	@Override
	public ReturnStatus execute(Simulator sim_, ZILInterpreterRuntimeEnv runtime_, ZILInterpreter comp) throws ZamiaException {
		//System.out.println("executing:   DUP");

		ZILValue v2 = runtime_.pop().getValue(sim_);
		ZILValue v1 = runtime_.pop().getValue(sim_);
		runtime_.push(v2);
		runtime_.push(v1);
		
		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "SWAP";
	}

}
