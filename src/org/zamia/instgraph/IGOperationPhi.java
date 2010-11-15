/* 
 * Copyright 2009,2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on May 25, 2009
 */
package org.zamia.instgraph;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGJumpNCStmt;
import org.zamia.instgraph.interpreter.IGJumpStmt;
import org.zamia.instgraph.interpreter.IGLabel;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;


/**
 * A multiplexer in IGOperation world
 * 
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class IGOperationPhi extends IGOperation {

	private IGOperation fCond;

	private IGOperation fTrueOp;

	private IGOperation fFalseOp;

	public IGOperationPhi(IGOperation aCond, IGOperation aTrueOp, IGOperation aFalseOp, IGType aType, SourceLocation aSrc, ZDB aZDB) {
		super(aType, aSrc, aZDB);

		fCond = aCond;
		fTrueOp = aTrueOp;
		fFalseOp = aFalseOp;
	}

	@Override
	public void computeAccessedItems(boolean aLeftSide, IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {
		fCond.computeAccessedItems(false, aFilterItem, aFilterType, aDepth, aAccessedItems);
		fTrueOp.computeAccessedItems(false, aFilterItem, aFilterType, aDepth, aAccessedItems);
		fFalseOp.computeAccessedItems(false, aFilterItem, aFilterType, aDepth, aAccessedItems);
	}

	@Override
	public void generateCode(boolean aFromInside, IGInterpreterCode aCode) throws ZamiaException {
		fCond.generateCode(aFromInside, aCode);

		IGLabel falseLabel = new IGLabel();

		aCode.add(new IGJumpNCStmt(falseLabel, computeSourceLocation(), getZDB()));

		fTrueOp.generateCode(aFromInside, aCode);

		IGLabel endLabel = new IGLabel();

		aCode.add(new IGJumpStmt(endLabel, computeSourceLocation(), getZDB()));

		aCode.defineLabel(falseLabel);

		fFalseOp.generateCode(aFromInside, aCode);

		aCode.defineLabel(endLabel);
	}

	@Override
	public OIDir getDirection() throws ZamiaException {
		throw new ZamiaException("Sorry, not implemented yet.");
	}

	@Override
	public int getNumOperands() {
		return 3;
	}

	@Override
	public IGOperation getOperand(int aIdx) {
		switch (aIdx) {
		case 0:
			return fCond;
		case 1:
			return fTrueOp;
		}
		return fFalseOp;
	}

	@Override
	public String toString() {
		return "IGOperationPhi(cond=" + fCond + ", trueOp=" + fTrueOp + ", falseOp=" + fFalseOp + ")";
	}

}
