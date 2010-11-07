/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jun 25, 2009
 */
package org.zamia.instgraph.interpreter;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGObject;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class IGNewObjectStmt extends IGStmt {

	private long fObjectDBID;

	public IGNewObjectStmt(IGObject aObject, SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);
		
		fObjectDBID = aObject.store();
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
		
		IGObject obj = (IGObject) getZDB().load(fObjectDBID);
		
		aRuntime.newObject(obj, computeSourceLocation());
		
		return ReturnStatus.CONTINUE;
	}

	@Override 
	public String toString() {
		return "NEW OBJECT "+getZDB().load(fObjectDBID)+" DBID "+fObjectDBID;
	}
	
}
