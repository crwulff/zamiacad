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
import org.zamia.instgraph.IGRecordField;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGRecordFieldRefOpStmt extends IGStmt {

	private IGRecordField fRF;

	public IGRecordFieldRefOpStmt(IGRecordField aRF, SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);

		fRF = aRF;
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
		// FIXME
		throw new ZamiaException("Sorry, not implemented yet.");
	}

	@Override
	public String toString() {
		return "RECORD FIELD REF " + fRF;
	}

}
