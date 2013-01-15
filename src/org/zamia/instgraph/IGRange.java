/* 
 * Copyright 2009,2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 11, 2009
 */
package org.zamia.instgraph;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGPushStmt;
import org.zamia.instgraph.interpreter.IGRangeOpStmt;
import org.zamia.instgraph.interpreter.IGRangeOpStmt.IGRangeOp;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class IGRange extends IGOperation {

	private IGOperation fRight;

	private IGOperation fLeft;

	private IGOperation fAscending;

	public IGRange(IGType aType, SourceLocation aSrc, ZDB aZDB) {
		super(aType, aSrc, aZDB);
		if (!aType.isRange()) {
			logger.error("IGRange: internal error: type is not a range type: %s, location: %s", aType, aSrc);
		}
	}

	public IGRange(IGOperation aLeft, IGOperation aRight, IGOperation aAscending, SourceLocation aSrc, ZDB aZDB) {
		super(aLeft.getType().getRange().getType(), aSrc, aZDB);

		fLeft = aLeft;
		fRight = aRight;
		fAscending = aAscending;
	}

	public IGOperation getLeft() {
		return fLeft;
	}

	public IGOperation getRight() {
		return fRight;
	}

	public IGOperation getAscending() {
		return fAscending;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {

		return "range (left=" + fLeft + ", right=" + fRight + ", ascending=" + fAscending + ")";
	}

	public void setLeft(IGOperation aOperation) {
		fLeft = aOperation;
	}

	public void setRight(IGOperation aOperation) {
		fRight = aOperation;
	}

	public void setAscending(IGOperation aAscending) {
		fAscending = aAscending;
	}

	public String toHRString() {
		return "IGRange(left=" + fLeft + ", right=" + fRight + ", ascending=" + fAscending + ")";
	}

	@Override
	public IGItem getChild(int aIdx) {
		switch (aIdx) {
		case 0:
			return fLeft;
		case 1:
			return fAscending;
		case 2:
			return fRight;
		}
		return null;
	}

	@Override
	public int getNumChildren() {
		return 3;
	}

	@Override
	public void computeAccessedItems(boolean aLeftSide, IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {
		fLeft.computeAccessedItems(aLeftSide, aFilterItem, aFilterType, aDepth, aAccessedItems);
		fAscending.computeAccessedItems(aLeftSide, aFilterItem, aFilterType, aDepth, aAccessedItems);
		fRight.computeAccessedItems(aLeftSide, aFilterItem, aFilterType, aDepth, aAccessedItems);
	}

	@Override
	public void generateCode(boolean aFromInside, IGInterpreterCode aCode) throws ZamiaException {

		fLeft.generateCode(aFromInside, aCode);
		if (fAscending != null) {
			fAscending.generateCode(aFromInside, aCode);
		} else {
			aCode.add(new IGPushStmt(true, computeSourceLocation(), getZDB()));
		}
		fRight.generateCode(aFromInside, aCode);

		aCode.add(new IGRangeOpStmt(IGRangeOp.CREATE, getType(), computeSourceLocation(), getZDB()));
	}

	@Override
	public OIDir getDirection() throws ZamiaException {
		// FIXME?
		throw new ZamiaException("Internal error, sorry.");
	}

	@Override
	public int getNumOperands() {
		return 3;
	}

	@Override
	public IGOperation getOperand(int aIdx) {

		switch (aIdx) {
		case 0:
			return fLeft;
		case 1:
			return fAscending;
		case 2:
			return fRight;
		}

		return null;
	}

	@Override
	public IGOperation getRangeLeft(SourceLocation aSrc) throws ZamiaException {
		return fLeft;
	}

	@Override
	public IGOperation getRangeRight(SourceLocation aSrc) throws ZamiaException {
		return fRight;
	}

	@Override
	public IGOperation getRangeAscending(IGContainer aContainer, SourceLocation aSrc) throws ZamiaException {
		return fAscending;
	}
}
