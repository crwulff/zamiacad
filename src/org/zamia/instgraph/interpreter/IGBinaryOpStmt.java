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
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.IGOperationBinary.BinOp;
import org.zamia.vhdl.ast.ASTObject.ASTErrorMode;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGBinaryOpStmt extends IGOpStmt {

	private BinOp fOp;

	public IGBinaryOpStmt(BinOp aOp, IGType aResType, SourceLocation aLocation, ZDB aZDB) {
		super(aResType, aLocation, aZDB);
		fOp = aOp;
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		IGStackFrame sf2 = aRuntime.pop();
		IGStaticValue op2 = sf2.getValue();

		IGStackFrame sf1 = aRuntime.pop();
		IGStaticValue op1 = sf1.getValue();

		IGTypeStatic rt = getType().computeStaticType(aRuntime, aErrorMode, aReport);
		if (rt == null) {
			return ReturnStatus.ERROR;
		}

		IGStaticValue resValue = IGStaticValue.computeBinary(op1, op2, fOp, rt, computeSourceLocation(), aErrorMode, aReport);
		if (resValue == null) {
			return ReturnStatus.ERROR;
		}
		aRuntime.push(resValue);

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "BINARY OP " + fOp;
	}
}
