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
import org.zamia.instgraph.IGItem;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public abstract class IGStmt extends IGItem {

	public enum ReturnStatus {
		CONTINUE, WAIT, RETURN, ERROR
	};

	private int fExecCount = 0;

	public IGStmt(SourceLocation aLocation, ZDB aZDB) {
		super (aLocation, aZDB);

	}

	public ReturnStatus executeStmt(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
		fExecCount++;
		return execute(aRuntime, aErrorMode, aReport);
	}

	public abstract ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException;

	@Override
	public IGItem getChild(int aIdx) {
		return null;
	}

	@Override
	public int getNumChildren() {
		return 0;
	}

	public int getExecCount() {
		return fExecCount;
	}

	public String toStringStat() {
		return toString() + " [execCount = " + fExecCount + "]";
	}
}
