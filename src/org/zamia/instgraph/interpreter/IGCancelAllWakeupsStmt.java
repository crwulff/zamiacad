/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Sep 3, 2009
 */
package org.zamia.instgraph.interpreter;

import java.math.BigInteger;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.vhdl.ast.ASTObject.ASTErrorMode;
import org.zamia.zdb.ZDB;

/**
 * @author guenter bartsch
 */

@SuppressWarnings("serial")
public class IGCancelAllWakeupsStmt extends IGStmt {

	public IGCancelAllWakeupsStmt(SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		aRuntime.cancelAllWakeups(computeSourceLocation());

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "CANCEL ALL WAKEUPS";
	}

}
