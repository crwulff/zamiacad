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
public class IGMapStmt extends IGStmt {

	public IGMapStmt(SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);
	}

	@Override
	public String toString() {
		return "MAP";
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		IGStackFrame actualSF = aRuntime.pop();
		IGStackFrame formalSF = aRuntime.pop();

		IGObjectDriver actual = actualSF.getObjectDriver();
		IGObjectDriver formal = formalSF.getObjectDriver();

		if (formal == null) {
			throw new ZamiaException("IGMapStmt: Invalid formal", computeSourceLocation());
		}
		
		if (actual == null) {
			
			IGStaticValue v = actualSF.getValue();

			if (v == null) {
				ZamiaException e = new ZamiaException ("IGMapStmt: actual is unitialized.", computeSourceLocation());
				if (aErrorMode == ASTErrorMode.RETURN_NULL) {
					if (aReport != null) {
						aReport.append(e);
					}
					return ReturnStatus.ERROR;
				} else {
					throw e;
				}
			}
			
			formal.setValue(v, computeSourceLocation());
			
		} else {
			formal.map(actual, computeSourceLocation());
		}
		
		if (IGInterpreterRuntimeEnv.dump) {
			logger.debug ("Interpreter: mapping done. result: %s", formal);
		}

		return ReturnStatus.CONTINUE;
	}

}
