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
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class ZILAssertStmt extends ZILStmt {

	public ZILAssertStmt(VHDLNode src_) {
		super(src_);
	}

	public ReturnStatus execute(Simulator sim_, ZILInterpreterRuntimeEnv runtime_, ZILInterpreter comp) throws ZamiaException {

		ZILStackFrame sf2 = runtime_.pop();
		ZILStackFrame sf1 = runtime_.pop();

		char tv = sf1.getValue(sim_).getBit();
		String msg;
		msg = sf2.getValue(sim_).getString();

		if (tv == ZILValue.BIT_0) {
			throw new ZamiaException("Assertion failed: " + msg);
		}

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "ASSERT";
	}
}
