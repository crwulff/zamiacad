/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 9, 2009
 */
package org.zamia.zil.interpreter;

import org.zamia.ZamiaException;
import org.zamia.rtl.sim.Simulator;
import org.zamia.vhdl.ast.ASTObject;


/**
 * Set timeout (value comes from stack)
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class ZILTimeoutStmt extends ZILStmt {

	public ZILTimeoutStmt(ASTObject aSrc) {
		super(aSrc);
	}

	@Override
	public ReturnStatus execute(Simulator aSim, ZILInterpreterRuntimeEnv aRuntime, ZILInterpreter aInterpreter) throws ZamiaException {
		ZILStackFrame sf = aRuntime.pop();

		long t = sf.getLiteral().getReal(getSource()).longValue();
		//System.out.println("Waiting for " + t + " ps");

		aRuntime.requestWakeup(t + aSim.getCurrentTime(), aSim);

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "TIMEOUT";
	}

}
