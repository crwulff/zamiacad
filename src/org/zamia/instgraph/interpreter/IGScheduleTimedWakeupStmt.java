/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jun 19, 2009
 */
package org.zamia.instgraph.interpreter;

import java.math.BigInteger;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGStaticValueBuilder;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.vhdl.ast.ASTObject.ASTErrorMode;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGScheduleTimedWakeupStmt extends IGStmt {

	public IGScheduleTimedWakeupStmt(SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		IGStackFrame sf = aRuntime.pop();
		IGStaticValue staticValue = sf.getLiteral();

		BigInteger t = staticValue.getNum();
		BigInteger currentTime = aRuntime.getCurrentTime(computeSourceLocation());

		BigInteger wakeupTime = currentTime.add(t);

		aRuntime.scheduleWakeup(wakeupTime, computeSourceLocation());

		// Push the same wakeupTime to check for timeout in IGJumpTimeoutStmt
		IGTypeStatic staticType = staticValue.getType().computeStaticType(aRuntime, aErrorMode, aReport);
		IGStaticValueBuilder builder = new IGStaticValueBuilder(staticType, null, null);
		IGStaticValue staticWakeupTime = builder.setNum(wakeupTime).buildConstant();
		aRuntime.push(staticWakeupTime);
		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "SCHEDULE TIMED WAKEUP";
	}

}
