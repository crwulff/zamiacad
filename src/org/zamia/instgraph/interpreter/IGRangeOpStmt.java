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
import org.zamia.instgraph.IGStaticValueBuilder;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.vhdl.ast.ASTObject.ASTErrorMode;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGRangeOpStmt extends IGOpStmt {

	public enum IGRangeOp {
		LEFT, RIGHT, ASCENDING, CREATE, APPLY
	}

	private IGRangeOp fRangeOp;

	public IGRangeOpStmt(IGRangeOp aRangeOp, IGType aType, SourceLocation aLocation, ZDB aZDB) {
		super(aType, aLocation, aZDB);
		fRangeOp = aRangeOp;
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		IGTypeStatic rT;
		switch (fRangeOp) {
		case APPLY:

			IGStackFrame rangeSF = aRuntime.pop();
			IGStackFrame opSF = aRuntime.pop();

			IGStaticValue v = opSF.getValue();
			IGStaticValue r = rangeSF.getValue();

			rT = getType().computeStaticType(aRuntime, aErrorMode, aReport);
			if (rT == null) {
				return ReturnStatus.ERROR;
			}
			
			IGStaticValueBuilder builder = new IGStaticValueBuilder(rT, null, computeSourceLocation());

			int offset = builder.getArrayOffset();

			int left = (int) r.getLeft().getOrd();
			int right = (int) r.getRight().getOrd();
			boolean ascending = r.getAscending().isTrue();

			if (ascending) {
				for (int i = left; i <= right; i++) {
					builder.set(i - left + offset, v.getValue(i, computeSourceLocation()), computeSourceLocation());
				}
			} else {
				for (int i = left; i >= right; i--) {
					builder.set(i - right + offset, v.getValue(i, computeSourceLocation()), computeSourceLocation());
				}
			}

			aRuntime.push(builder.buildConstant());
			break;

		case CREATE:
			// we should find on the stack:
			// right, ascending, left

			IGStackFrame rightSF = aRuntime.pop();
			IGStackFrame ascendingSF = aRuntime.pop();
			IGStackFrame leftSF = aRuntime.pop();

			rT = getType().computeStaticType(aRuntime, aErrorMode, aReport);
			if (rT == null) {
				return ReturnStatus.ERROR;
			}
			
			builder = new IGStaticValueBuilder(rT, null, computeSourceLocation());

			builder.setLeft(leftSF.getValue());
			builder.setAscending(ascendingSF.getValue());
			builder.setRight(rightSF.getValue());

			aRuntime.push(builder.buildConstant());
			break;

		case ASCENDING:
			rangeSF = aRuntime.pop();
			IGStaticValue range = rangeSF.getValue();
			aRuntime.push(range.getAscending());
			break;

		case LEFT:
			rangeSF = aRuntime.pop();
			range = rangeSF.getValue();
			aRuntime.push(range.getLeft());
			break;

		case RIGHT:
			rangeSF = aRuntime.pop();
			range = rangeSF.getValue();
			aRuntime.push(range.getRight());
			break;

		}

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "IGRangeOp (op=" + fRangeOp + ")";
	}

}
