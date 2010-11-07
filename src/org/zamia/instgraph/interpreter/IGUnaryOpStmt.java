/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 11, 2009
 */
package org.zamia.instgraph.interpreter;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGOperationUnary.UnaryOp;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGUnaryOpStmt extends IGStmt {

	private UnaryOp fOp;

	public IGUnaryOpStmt(UnaryOp aOp, SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);
		fOp = aOp;
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		IGStackFrame sf1 = aRuntime.pop();
		IGStaticValue op1 = sf1.getValue();

		IGStaticValue resValue = IGStaticValue.computeUnary(op1, fOp, computeSourceLocation(), aErrorMode, aReport);
		if (resValue == null) {
			return ReturnStatus.ERROR;
		}
		aRuntime.push(resValue);

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "UNARY OP " + fOp;
	}
}
