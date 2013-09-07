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
public class IGArrayAggregateStmt extends IGOpStmt {

	private int fNamedEntries;

	private int fPositionalEntries;

	private boolean fHaveOthers;

	public IGArrayAggregateStmt(int aNamedEntries, int aPositionalEntries, boolean aHaveOthers, IGType aResType, SourceLocation aLocation, ZDB aZDB) {
		super(aResType, aLocation, aZDB);
		fNamedEntries = aNamedEntries;
		fPositionalEntries = aPositionalEntries;
		fHaveOthers = aHaveOthers;
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		IGTypeStatic rt = getType().computeStaticType(aRuntime, aErrorMode, aReport);
		if (rt == null) {
			return ReturnStatus.ERROR;
		}

		if (rt.isUnconstrained()) {

			// compute constraints at runtime

			IGTypeStatic idxType = rt.getStaticIndexType(computeSourceLocation());

			boolean bAsc = idxType.isAscending();
			int nIdxTLeft = idxType.getStaticLeft(null).getInt();

			int offset = 0;

			if (fHaveOthers) {

				logger.error("IGArrayAggregateStmt: Internal error, array is unconstrained and others value is given!");

				offset++;
			}

			offset += fPositionalEntries;

			// compute maximum and minimum indices being set from named entries

			int minNamedIdx = Integer.MAX_VALUE, maxNamedIdx = Integer.MIN_VALUE;

			for (int i = 0; i < fNamedEntries; i++) {

				IGStaticValue pos = aRuntime.peek(offset).getValue();
				offset++;
				offset++; // skip value

				int min, max;

				IGType t = pos.getType();
				if (t.isRange()) {

					IGStaticValue vAscending = pos.getAscending();

					IGStaticValue vLeft = pos.getLeft();
					IGStaticValue vRight = pos.getRight();

					boolean a = vAscending.isTrue();
					int l = vLeft.getInt(computeSourceLocation());
					int r = vRight.getInt(computeSourceLocation());

					min = a ? l : r;
					max = a ? r : l;

				} else {
					min = (int) pos.getOrd();
					max = min;
				}

				if (min < minNamedIdx) {
					minNamedIdx = min;
				}

				if (max > maxNamedIdx) {
					maxNamedIdx = max;
				}
			}

			// at this point we have the number of implicit associations as well as
			// the minimum and maximum indices of the explicit associations
			// with that we can compute the limits of the whole aggregate:
			//
			// ascending:  right := max (left+fPositionalEntries-1, rightMax)
			// descending: right := min (left-fPositionalEntries+1, rightMin)

			int nRight, nLeft;
			
			if (bAsc) {
				
				if (fNamedEntries > 0) {
					
					nLeft = minNamedIdx - fPositionalEntries;
					nRight = maxNamedIdx;
					
				} else {
					nLeft = nIdxTLeft;
					nRight = nLeft + fPositionalEntries -1;
				}
				
			} else {

				if (fNamedEntries > 0) {
					
					nLeft = maxNamedIdx + fPositionalEntries;
					nRight = minNamedIdx;
					
				} else {
					nLeft = nIdxTLeft;
					nRight = nLeft - fPositionalEntries + 1;
				}
			}
			
			
			//int nRight = bAsc ? Math.max(nLeft+fPositionalEntries-1, rightMax) : Math.min(nLeft-fPositionalEntries+1, rightMin);

			IGStaticValue left = new IGStaticValueBuilder(idxType, null, computeSourceLocation()).setOrd(nLeft).buildConstant();
			IGStaticValue right = new IGStaticValueBuilder(idxType, null, computeSourceLocation()).setOrd(nRight).buildConstant();
			IGStaticValue ascending = new IGStaticValue.INNER_BOOLEAN_DUPLICATE(bAsc, getZDB());

			IGTypeStatic rType = idxType.getStaticRange().getStaticType();//new IGTypeStatic(TypeCat.RANGE, null, null, idxType, null, false, computeSourceLocation(), getZDB());
			IGStaticValue range = new IGStaticValueBuilder(rType, null, computeSourceLocation()).setLeft(left).setRight(right).setAscending(ascending).buildConstant();

			rt = rt.createSubtype(range, computeSourceLocation());
		}

		IGStaticValueBuilder builder = new IGStaticValueBuilder(rt, null, computeSourceLocation());

		int nArrayOffset = builder.getArrayOffset();
		int nArrayElements = builder.getNumArrayElements();
		boolean ascending = rt.getStaticIndexType(computeSourceLocation()).isAscending();

		if (fHaveOthers) {

			IGStaticValue others = aRuntime.pop().getValue();

			for (int i = nArrayOffset; i < nArrayOffset + nArrayElements; i++) {
				builder.set(i, others, computeSourceLocation());
			}
		}

		for (int i = 0; i < fPositionalEntries; i++) {
			IGStaticValue v = aRuntime.pop().getValue();

			int idx = ascending ? nArrayElements - 1 - i + nArrayOffset : i + nArrayOffset;

			builder.set(idx, v, computeSourceLocation());
		}

		for (int i = 0; i < fNamedEntries; i++) {

			IGStaticValue pos = aRuntime.pop().getValue();
			IGStaticValue v = aRuntime.pop().getValue();

			IGType t = pos.getType();
			if (t.isRange()) {

				IGStaticValue vAscending = pos.getAscending();
				IGStaticValue vLeft = pos.getLeft();
				IGStaticValue vRight = pos.getRight();

				boolean asc = vAscending.isTrue();
				int left = vLeft.getInt(computeSourceLocation());
				int right = vRight.getInt(computeSourceLocation());

				if (asc) {
					for (int idx = left; idx <= right; idx++) {
						builder.set(IGStaticValue.adjustIdx(idx, ascending, nArrayElements, nArrayOffset), v, computeSourceLocation());
					}
				} else {
					for (int idx = left; idx >= right; idx--) {
						builder.set(IGStaticValue.adjustIdx(idx, ascending, nArrayElements, nArrayOffset), v, computeSourceLocation());
					}
				}
			} else {
				int idx = (int) pos.getOrd();
				builder.set(IGStaticValue.adjustIdx(idx, ascending, nArrayElements, nArrayOffset), v, computeSourceLocation());
			}
		}

		IGStaticValue c = builder.buildConstant();

		aRuntime.push(c);

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "AGGREGATE (#namedEntries=" + fNamedEntries + ", #positionalEntries=" + fPositionalEntries + ", haveOthers=" + fHaveOthers + ", resType=" + getType() + ")";
	}

}
