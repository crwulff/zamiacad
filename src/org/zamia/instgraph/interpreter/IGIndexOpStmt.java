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
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGIndexOpStmt extends IGStmt {

	public IGIndexOpStmt(SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);

	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		IGStackFrame sfIdx = aRuntime.pop();
		IGStaticValue opIdx = sfIdx.getValue();

		IGStackFrame sf = aRuntime.pop();

		int idx = (int) opIdx.getOrd();

		IGObjectDriver driver = sf.getObjectDriver();
		if (driver != null) {

			IGTypeStatic idxType = driver.getCurrentType().getStaticIndexType(computeSourceLocation());
			int n = (int) idxType.computeCardinality(computeSourceLocation());
			boolean asc = idxType.isAscending();
			IGStaticValue range = idxType.getStaticRange();
			int offset = (int) (asc ? range.getLeft().getOrd() : range.getRight().getOrd());

			idx = IGStaticValue.adjustIdx(idx, asc, n, offset);

			driver = driver.getArrayElementDriver(idx, computeSourceLocation());

			if (IGInterpreterRuntimeEnv.dump) {
				logger.debug ("Interpreter: result of driver idx op: %s", driver);
			}

			aRuntime.push(driver);

		} else {

			IGStaticValue op = sf.getValue();

			IGStaticValue resValue = op.getValue(idx, computeSourceLocation());

			aRuntime.push(resValue);
		}

		return ReturnStatus.CONTINUE;

	}

	@Override
	public String toString() {
		return "INDEX";
	}

}
