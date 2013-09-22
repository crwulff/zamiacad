/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jun 21, 2009
 */
package org.zamia.instgraph.interpreter;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationObject;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.interpreter.IGStmt.ReturnStatus;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class IGDerefOpStmt extends IGOpStmt {
	
	private long fObjectDBID;
	
	public IGDerefOpStmt(IGOperation fOp, IGType aResultType, SourceLocation aLocation, ZDB aZDB) {
		super(aResultType, aLocation, aZDB);
		if (fOp instanceof IGOperationObject)
			fObjectDBID = ((IGOperationObject)fOp).getObject().getDBID();
		
		//TODO: check what are we doing here in case of non-operationobject 
		// (non-access type), I got here with arrayTest/vital_timing_body.vhdl
		else;  
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
		IGObject obj = (IGObject) getZDB().load(fObjectDBID);
		IGObjectDriver drv = aRuntime.getDriver(obj);
		aRuntime.push(drv);
		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "DEREF";
	}
}
