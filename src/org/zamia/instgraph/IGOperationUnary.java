/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
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
import org.zamia.instgraph.interpreter.IGUnaryOpStmt;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGOperationUnary extends IGOperation {

	public enum UnaryOp {
		NEG, NOT, ABS, BUF
	};

	private IGOperation fA;

	private UnaryOp fUnaryOp;

	public IGOperationUnary(IGOperation aA, UnaryOp aUnaryOp, IGType aType, SourceLocation aSrc, ZDB aZDB) {
		super(aType, aSrc, aZDB);
		fA = aA;
		fUnaryOp = aUnaryOp;
	}

	@Override
	public void generateCode(boolean aFromInside, IGInterpreterCode aCode) throws ZamiaException {
		fA.generateCode(aFromInside, aCode);
		aCode.add(new IGUnaryOpStmt(fUnaryOp, computeSourceLocation(), getZDB()));
	}

	@Override
	public int getNumOperands() {
		return 1;
	}

	@Override
	public IGOperation getOperand(int aIdx) {
		return fA;
	}

	public IGOperation getA() {
		return fA;
	}

	public UnaryOp getUnaryOp() {
		return fUnaryOp;
	}

	@Override
	public String toString() {
		return fUnaryOp + " " + fA.toString();
	}

	@Override
	public void generateCodeRef(boolean aFromInside, boolean aCheckDirection, IGInterpreterCode aCode) throws ZamiaException {
		throw new ZamiaException("Operation not permitted on left hand side");
	}

	@Override
	public OIDir getDirection() throws ZamiaException {
		return fA.getDirection();
	}

	@Override
	public void computeAccessedItems(boolean aLeftSide, IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {
		fA.computeAccessedItems(aLeftSide, aFilterItem, aFilterType, aDepth, aAccessedItems);
	}
}
