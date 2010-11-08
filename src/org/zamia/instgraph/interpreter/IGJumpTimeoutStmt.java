/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jun 19, 2009
 */
package org.zamia.instgraph.interpreter;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.zdb.ZDB;

import java.math.BigInteger;

/**
 * 
 * @author Guenter Bartsch
 *
 */
@SuppressWarnings("serial")
public class IGJumpTimeoutStmt extends IGJumpStmt {

	public IGJumpTimeoutStmt(IGLabel aLabel, SourceLocation aLocation, ZDB aZDB) {
		super(aLabel, aLocation, aZDB);
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
		IGStackFrame stackFrame = aRuntime.pop();
		BigInteger timeout = stackFrame.getLiteral().getNum();

		BigInteger simTime = aRuntime.getCurrentTime(computeSourceLocation());

		if (timeout.compareTo(simTime) == 0) {
			aRuntime.setPC(fAdr);
		}

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "JUMP TIMEOUT "+fAdr;
	}

}
