/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.instgraph.interpreter;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGIndexOpStmt extends IGStmt {

	public IGIndexOpStmt(SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);

	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		IGStackFrame sfIdx = aRuntime.pop();
		IGStaticValue opIdx = sfIdx.getValue();

		IGStackFrame sf = aRuntime.pop();
		IGStaticValue op = sf.getValue();

		IGStaticValue resValue = op.getValue((int) opIdx.getOrd(), computeSourceLocation());

		aRuntime.push(resValue);

		return ReturnStatus.CONTINUE;

	}

	@Override
	public String toString() {
		return "INDEX";
	}

}
