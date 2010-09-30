/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 22, 2010
 */

package org.zamia.instgraph;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGRangeOpStmt;
import org.zamia.instgraph.interpreter.IGRangeRefOpStmt;
import org.zamia.instgraph.interpreter.IGRangeOpStmt.IGRangeOp;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGOperationRange extends IGOperation {

	private IGOperation fOp;

	private IGOperation fRange;

	public IGOperationRange(IGOperation aRange, IGOperation aOp, IGType aType, SourceLocation aSrc, ZDB aZDB) {
		super(aType, aSrc, aZDB);
		fOp = aOp;
		fRange = aRange;
	}

	@Override
	public void generateCode(boolean aFromInside, IGInterpreterCode aCode) throws ZamiaException {
		fOp.generateCode(aFromInside, aCode);
		fRange.generateCode(aFromInside, aCode);
		aCode.add(new IGRangeOpStmt(IGRangeOp.APPLY, getType(), computeSourceLocation(), getZDB()));
	}

	@Override
	public void generateCodeRef(boolean aFromInside, boolean aCheckDirection, IGInterpreterCode aCode) throws ZamiaException {
		fOp.generateCodeRef(aFromInside, true, aCode);
		fRange.generateCode(aFromInside, aCode);
		aCode.add(new IGRangeRefOpStmt(getType(), computeSourceLocation(), getZDB()));
	}

	@Override
	public OIDir getDirection() throws ZamiaException {
		return fOp.getDirection();
	}

	@Override
	public int getNumOperands() {
		return 2;
	}

	@Override
	public IGOperation getOperand(int aIdx) {
		switch (aIdx) {
		case 0:
			return fOp;
		case 1:
			return fRange;
		}
		return null;
	}

	@Override
	public void computeAccessedItems(boolean aLeftSide, IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {
		fOp.computeAccessedItems(aLeftSide, aFilterItem, aFilterType, aDepth, aAccessedItems);
		fRange.computeAccessedItems(false, aFilterItem, aFilterType, aDepth, aAccessedItems);
	}

	@Override
	public String toString() {
		return fOp.toString() + "(" + fRange + ")";
	}

	public IGOperation getRange() {
		return fRange;
	}

	public IGOperation getOperand() {
		return fOp;
	}
}
