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


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class ZILCallStmt extends ZILStmt {

	private ZILInterpreterCode fCode;

	public ZILCallStmt(ZILInterpreterCode aCode, ASTObject aSrc) {
		super(aSrc);

		fCode = aCode;
	}

	@Override
	public ReturnStatus execute(Simulator aSim, ZILInterpreterRuntimeEnv aRuntime, ZILInterpreter aInterpreter) throws ZamiaException {

		aRuntime.call(fCode, aSim, aInterpreter);

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "CALL " + fCode.getId();
	}
}
