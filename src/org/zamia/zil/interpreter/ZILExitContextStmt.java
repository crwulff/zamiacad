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
public class ZILExitContextStmt extends ZILStmt {

	public ZILExitContextStmt(VHDLNode src_) {
		super(src_);
	}

	@Override
	public ReturnStatus execute(Simulator sim_, ZILInterpreterRuntimeEnv runtime_, ZILInterpreter comp) throws ZamiaException {
		//System.out.println("executing:   EXIT");

		runtime_.exitContext();

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "EXIT CONTEXT";
	}
}

