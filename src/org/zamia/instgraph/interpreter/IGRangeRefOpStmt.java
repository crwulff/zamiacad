/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
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
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGRangeRefOpStmt extends IGOpStmt {

	public IGRangeRefOpStmt(IGType aType, SourceLocation aLocation, ZDB aZDB) {
		super(aType, aLocation, aZDB);

	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		IGStackFrame sfRange = aRuntime.pop();
		IGStaticValue opRange = sfRange.getValue();

		int l = (int) opRange.getLeft().getOrd();
		int r = (int) opRange.getRight().getOrd();
		boolean a = opRange.getAscending().isTrue();
		
		int min = a ? l : r;
		int max = a ? r : l;
		
		IGStackFrame sf = aRuntime.pop();
		IGObjectWriter writer = sf.getObjectWriter();

		IGTypeStatic rT = getType().computeStaticType(aRuntime, aErrorMode, aReport);
		if (rT == null) {
			return ReturnStatus.ERROR;
		}
		
		writer = new IGObjectWriter(writer, rT, min, max, computeSourceLocation());
		
		aRuntime.push(writer);
		
		return ReturnStatus.CONTINUE;

	}

}
