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
public class ZILEnterNewContextStmt extends ZILStmt {

	public ZILEnterNewContextStmt(ASTObject src_) {
		super(src_);
	}

	@Override
	public ReturnStatus execute(Simulator sim_, ZILInterpreterRuntimeEnv runtime_, ZILInterpreter comp) throws ZamiaException {
		//System.out.println("executing:   ENTER NEW CONTEXT");

		runtime_.enterContext();

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "ENTER NEW CONTEXT";
	}

}

