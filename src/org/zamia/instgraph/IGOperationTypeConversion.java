/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.instgraph;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGTypeConversionStmt;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGOperationTypeConversion extends IGOperation {

	private IGOperation fOp;

	public IGOperationTypeConversion(IGOperation aOp, IGType aType, SourceLocation aSrc, ZDB aZDB) {
		super(aType, aSrc, aZDB);
		fOp = aOp;
	}

	@Override
	public void computeAccessedItems(boolean aLeftSide, IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {
		fOp.computeAccessedItems(aLeftSide, aFilterItem, aFilterType, aDepth, aAccessedItems);
	}

	@Override
	public void generateCode(boolean aFromInside, IGInterpreterCode aCode) throws ZamiaException {
		fOp.generateCode(aFromInside, aCode);
		aCode.add(new IGTypeConversionStmt(getType(), computeSourceLocation(), getZDB()));
	}

	@Override
	public IGObject generateCodeRef(boolean aFromInside, boolean aCheckDirection, IGInterpreterCode aCode) throws ZamiaException {
		// FIXME
		throw new ZamiaException("Sorry, not implemented yet.");
	}

	@Override
	public OIDir getDirection() throws ZamiaException {
		return fOp.getDirection();
	}

	@Override
	public int getNumOperands() {
		return 1;
	}

	@Override
	public IGOperation getOperand(int aIdx) {
		return fOp;
	}

}
