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
public class ZILJumpNCStmt extends ZILJumpStmt {

	public ZILJumpNCStmt(ZILLabel label_, VHDLNode src_) {
		super(label_, src_);
	}

	@Override
	public ReturnStatus execute(Simulator sim_, ZILInterpreterRuntimeEnv runtime_, ZILInterpreter comp) throws ZamiaException {

		ZILStackFrame sf1 = runtime_.pop();

		String tv;
		tv = sf1.getValue(sim_).getString();

		//		System.out.println("executing:   JUMP NC true value:" + tv + " adr=" + adr);

		if (tv.charAt(0) == '0') {
			runtime_.setPC(adr);
		}

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "JUMP NC " + adr;
	}
}
