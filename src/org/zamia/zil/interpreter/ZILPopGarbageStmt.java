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


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class ZILPopGarbageStmt extends ZILStmt {

	public ZILPopGarbageStmt(ASTObject src_) {
		super(src_);
	}

	@Override
	public ReturnStatus execute(Simulator sim_, ZILInterpreterRuntimeEnv runtime_, ZILInterpreter comp) throws ZamiaException {
		//System.out.println("executing:   POP GARBAGE");

		runtime_.pop();
		
		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "POP GARBAGE";
	}

}
