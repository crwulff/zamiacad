/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on May 21, 2009
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
public class IGPushRefStmt extends IGStmt {

	private long fObjectDBID;

	/**
	 * push ref object
	 * 
	 * @param aObject
	 * @param aLocation
	 * @param aZDB
	 */
	public IGPushRefStmt(IGObject aObject, SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);

		fObjectDBID = save(aObject);
	}

	private IGObject getObject() {
		if (fObjectDBID == 0)
			return null;
		return (IGObject) getZDB().load(fObjectDBID);
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
		IGObjectWriter sw = new IGObjectWriter(getObject(), computeSourceLocation());

		aRuntime.push(sw);

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "PUSH REF " + getObject() + " DBID=" + getObject().getDBID();
	}

}
