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
public class ZILJumpTimeoutStmt extends ZILJumpStmt {

	public ZILJumpTimeoutStmt(ZILLabel aLabel, VHDLNode aSrc) {
		super(aLabel, aSrc);
	}

	@Override
	public ReturnStatus execute(Simulator aSim, ZILInterpreterRuntimeEnv aRuntime, ZILInterpreter aInterpreter) throws ZamiaException {

		if (!aRuntime.isTimeout()) {
			return ReturnStatus.CONTINUE;
		}

		aRuntime.setPC(adr);

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "JUMP TIMEOUT " + adr;
	}
}
