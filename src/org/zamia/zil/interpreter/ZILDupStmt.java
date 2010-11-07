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
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class ZILDupStmt extends ZILStmt {

	public ZILDupStmt(VHDLNode src_) {
		super(src_);
	}

	@Override
	public ReturnStatus execute(Simulator sim_, ZILInterpreterRuntimeEnv runtime_, ZILInterpreter comp) throws ZamiaException {
		//System.out.println("executing:   DUP");

		ZILValue v = runtime_.pop().getValue(sim_);
		runtime_.push(v);
		runtime_.push(v);

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "DUP";
	}

}
