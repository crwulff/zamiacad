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
public class ZILJumpStmt extends ZILStmt {

	protected int adr;

	public ZILJumpStmt(ZILLabel label_, ASTObject src_) {
		super(src_);
		adr = label_.getAdr(this);
	}

	@Override
	public ReturnStatus execute(Simulator sim_, ZILInterpreterRuntimeEnv runtime_, ZILInterpreter comp) throws ZamiaException {

		//System.out.println("executing:   JUMP adr=" + adr);

		runtime_.setPC(adr);

		return ReturnStatus.CONTINUE;
	}

	public void setAdr(int adr_) {
		adr = adr_;
	}

	@Override
	public String toString() {
		return "JUMP " + adr;
	}
}

