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
import org.zamia.vhdl.ast.ASTObject.ASTErrorMode;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGIndexRefOpStmt extends IGStmt {

	public IGIndexRefOpStmt(SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);

	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		IGStackFrame sfIdx = aRuntime.pop();
		IGStaticValue opIdx = sfIdx.getValue();

		IGStackFrame sf = aRuntime.pop();
		IGObjectWriter writer = sf.getObjectWriter();
		
		writer = new IGObjectWriter(writer, opIdx.getInt(), computeSourceLocation());
		
		aRuntime.push(writer);
		
		return ReturnStatus.CONTINUE;

	}

}
