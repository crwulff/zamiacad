/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
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
import org.zamia.instgraph.IGObject;
import org.zamia.vhdl.ast.ASTObject.ASTErrorMode;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class IGJumpEventStmt extends IGJumpStmt {

	public IGJumpEventStmt(IGLabel aLabel, SourceLocation aLocation, ZDB aZDB) {
		super(aLabel, aLocation, aZDB);
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		IGStackFrame stackFrame = aRuntime.pop();
		IGObjectWriter ow = stackFrame.getObjectWriter();
		IGObject signal = ow.getObject();

		if (aRuntime.isChanged(signal, computeSourceLocation())) {
			aRuntime.setPC(fAdr);
		}
		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "JUMP EVENT "+fAdr;
	}

}
