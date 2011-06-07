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
import org.zamia.instgraph.interpreter.IGIndexOpStmt;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGOperationIndex extends IGOperation {

	private IGOperation fOp;

	private IGOperation fIdx;

	public IGOperationIndex(IGOperation aIdx, IGOperation aOp, IGType aType, SourceLocation aSrc, ZDB aZDB) {
		super(aType, aSrc, aZDB);

		fIdx = aIdx;
		fOp = aOp;
	}

	@Override
	public void generateCode(boolean aFromInside, IGInterpreterCode aCode) throws ZamiaException {

		fOp.generateCode(aFromInside, aCode);
		fIdx.generateCode(aFromInside, aCode);
		aCode.add(new IGIndexOpStmt(computeSourceLocation(), getZDB()));
	}

	@Override
	public int getNumOperands() {
		return 2;
	}

	@Override
	public IGOperation getOperand(int aIdx) {

		switch (aIdx) {
		case 0:
			return fIdx;
		case 1:
			return fOp;
		}
		return fOp;
	}

	@Override
	public OIDir getDirection() throws ZamiaException {
		return fOp.getDirection();
	}

	@Override
	public void computeAccessedItems(boolean aLeftSide, IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {
		fOp.computeAccessedItems(aLeftSide, aFilterItem, aFilterType, aDepth, aAccessedItems);
		fIdx.computeAccessedItems(false, aFilterItem, aFilterType, aDepth, aAccessedItems);
	}

	@Override
	public String toString() {
		return "IGOperationIndex(obj=" + fOp + ", idx=" + fIdx + ")";
	}

	@Override
	public String toHRString() {
		return fOp.toHRString() + "[" + fIdx.toHRString() + "]";
	}

	public IGOperation getIndex() {
		return fIdx;
	}

	public IGOperation getOperand() {
		return fOp;
	}
}
