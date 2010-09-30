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
import org.zamia.vhdl.ast.ASTObject.ASTErrorMode;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGJumpNCStmt extends IGJumpStmt {

	public IGJumpNCStmt(IGLabel aLabel, SourceLocation aLocation, ZDB aZDB) {
		super(aLabel, aLocation, aZDB);
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		IGStackFrame sf1 = aRuntime.pop();

		boolean b = sf1.getBool();

		if (!b) {
			aRuntime.setPC(fAdr);
		}

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "JUMP NC " + fAdr;
	}
}
