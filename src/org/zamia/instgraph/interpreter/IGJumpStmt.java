/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 24, 2008
 */
package org.zamia.instgraph.interpreter;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 *
 */
@SuppressWarnings("serial")
public class IGJumpStmt extends IGStmt {

	protected int fAdr;

	public IGJumpStmt(IGLabel aLabel, SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);
		fAdr = aLabel.getAdr(this);
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		//System.out.println("executing:   JUMP adr=" + adr);

		aRuntime.setPC(fAdr);

		return ReturnStatus.CONTINUE;
	}

	public void setAdr(int adr_) {
		fAdr = adr_;
	}

	@Override
	public String toString() {
		return "JUMP " + fAdr;
	}
}

