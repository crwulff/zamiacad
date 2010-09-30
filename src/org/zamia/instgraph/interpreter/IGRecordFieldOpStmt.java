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
import org.zamia.instgraph.IGRecordField;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.vhdl.ast.ASTObject.ASTErrorMode;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGRecordFieldOpStmt extends IGStmt {

	private IGRecordField fRF;

	public IGRecordFieldOpStmt(IGRecordField aRF, SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);
		fRF = aRF;
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		IGStackFrame sf = aRuntime.pop();
		IGStaticValue op = sf.getValue();

		IGStaticValue resValue = op.getRecordFieldValue(fRF.getId(), null);

		aRuntime.push(resValue);

		return ReturnStatus.CONTINUE;

	}

}
