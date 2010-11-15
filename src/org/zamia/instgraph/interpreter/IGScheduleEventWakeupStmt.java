/* 
 * Copyright 2009,2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jun 19, 2009
 */
package org.zamia.instgraph.interpreter;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGScheduleEventWakeupStmt extends IGStmt {

	public IGScheduleEventWakeupStmt(SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		IGStackFrame sf = aRuntime.pop();

		IGObjectDriver driver = sf.getObjectDriver();

		if (driver == null) {
			throw new ZamiaException("Internal error: signal expected in sensitivity list, found: " + sf, computeSourceLocation());
		}

		aRuntime.scheduleWakeup(driver, computeSourceLocation());

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "SCHEDULE EVENT WAKEUP";
	}

}
