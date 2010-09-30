/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
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
public class ZILSetConnectionStmt extends ZILStmt {

	private boolean fConnect;

	public ZILSetConnectionStmt(boolean aConnect, ASTObject aSrc) {
		super(aSrc);
		fConnect = aConnect;
	}

	@Override
	public ReturnStatus execute(Simulator aSim, ZILInterpreterRuntimeEnv aRuntime, ZILInterpreter aInterpreter) throws ZamiaException {
		if (!fConnect) {
			throw new ZamiaException ("Sorry, not implemented yet.");
		}
		return ReturnStatus.CONTINUE;
	}
	
	@Override
	public String toString() {
		return "SET CONNECTION "+fConnect;
	}

}
