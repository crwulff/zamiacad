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
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
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
			IGStaticValue r = rangeSF.getValue();

			IGStackFrame opSF = aRuntime.pop();

			rT = getType().computeStaticType(aRuntime, aErrorMode, aReport);
			if (rT == null) {
				return ReturnStatus.ERROR;
			}

			IGObjectDriver driver = opSF.getObjectDriver();
			if (driver != null) {

				SourceLocation src = computeSourceLocation();

				IGTypeStatic idxType = driver.getCurrentType().getStaticIndexType(src);
				int n = (int) idxType.computeCardinality(src);
				boolean asc = idxType.isAscending();

				if (asc) {
					IGStaticValue range = idxType.getStaticRange();
					int offset = (int) (asc ? range.getLeft().getOrd() : range.getRight().getOrd());

					int ll = r.getLeft().getInt();
					int rr = r.getRight().getInt();
					ll = IGStaticValue.adjustIdx(ll, asc, n, offset);
					rr = IGStaticValue.adjustIdx(rr, asc, n, offset);

					int tempL = ll;
					ll = rr;
					rr = tempL;

					IGStaticValue nR = new IGStaticValueBuilder(r.getRight().getStaticType(), null, src).setNum(rr).buildConstant();
					IGStaticValue nL = new IGStaticValueBuilder(r.getLeft().getStaticType(), null, src).setNum(ll).buildConstant();
					r = new IGStaticValueBuilder(r, src).setLeft(nL).setRight(nR).buildConstant();
				}

				IGObjectDriver rangeDriver = driver.createRangeDriver(r, rT, src);

				aRuntime.push(rangeDriver);

			} else {

				IGStaticValue v = opSF.getValue();

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
			}
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

			SourceLocation location = computeSourceLocation();

			IGStaticValueBuilder builder = new IGStaticValueBuilder(rT, null, location);

			IGStaticValue l = leftSF.getValue();
			r = rightSF.getValue();
			IGStaticValue a = ascendingSF.getValue();

			if (!checkParam(l, aErrorMode, aReport, location)) {
				return ReturnStatus.ERROR;
			}
			if (!checkParam(a, aErrorMode, aReport, location)) {
				return ReturnStatus.ERROR;
			}
			if (!checkParam(r, aErrorMode, aReport, location)) {
				return ReturnStatus.ERROR;
			}

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

	private boolean checkParam(IGStaticValue aV, ASTErrorMode aErrorMode, ErrorReport aReport, SourceLocation aLocation) throws ZamiaException {

		if (aV != null)
			return true;

		ZamiaException e = new ZamiaException("Parameter null", aLocation);

		if (aErrorMode == ASTErrorMode.EXCEPTION) {
			throw e;
		}

		if (aReport != null) {
			aReport.append(e);
		}

		return false;
	}

	@Override
	public String toString() {
		return "IGRangeOp (op=" + fRangeOp + ")";
	}

}
