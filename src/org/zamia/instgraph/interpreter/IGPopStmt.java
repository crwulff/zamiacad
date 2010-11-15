/*
 * Copyright 2005-2010 by the authors indicated in the @author tags.
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
public class IGPopStmt extends IGStmt {

	private boolean fInertial;

	private boolean fHaveDelay;

	private boolean fHaveReject;

	public IGPopStmt(boolean aInertial, boolean aHaveDelay, boolean aHaveReject, SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);

		fInertial = aInertial;
		fHaveDelay = aHaveDelay;
		fHaveReject = aHaveReject;

	}

	@Override
	public String toString() {
		return "POP";
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		IGStaticValue reject = null, delay = null;

		if (fHaveReject) {
			reject = aRuntime.pop().getValue();
		}

		if (fHaveDelay) {
			delay = aRuntime.pop().getValue();
		}

		IGStackFrame valueSF = aRuntime.pop();

		IGStackFrame targetSF = aRuntime.pop();

		IGObjectDriver driver = targetSF.getObjectDriver();

		if (driver == null) {
			throw new ZamiaException("IGPopStmt: Invalid target", computeSourceLocation());
		}

		IGStaticValue v = valueSF.getValue();
		if (v == null) {
			ZamiaException e = new ZamiaException("IGMapStmt: actual is unitialized.", computeSourceLocation());
			if (aErrorMode == ASTErrorMode.RETURN_NULL) {
				if (aReport != null) {
					aReport.append(e);
				}
				return ReturnStatus.ERROR;
			} else {
				throw e;
			}
		}

		driver.schedule(fInertial, delay, reject, v, aRuntime, computeSourceLocation());

		return ReturnStatus.CONTINUE;
	}

}
