/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
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

		IGStaticValue v = aRuntime.pop().getValue();

		IGStackFrame targetSF = aRuntime.pop();

		IGObjectWriter svw = targetSF.getObjectWriter();

		svw.setValue(v);

		svw.schedule(fInertial, delay, reject, aRuntime);
		
		return ReturnStatus.CONTINUE;
	}

}
