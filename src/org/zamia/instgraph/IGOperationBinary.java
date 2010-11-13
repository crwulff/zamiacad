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
import org.zamia.instgraph.interpreter.IGBinaryOpStmt;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGOperationBinary extends IGOperation {

	public enum BinOp {
		ADD, SUB, MUL, DIV, MOD, REM, POWER, EQUAL, LESSEQ, LESS, GREATER, GREATEREQ, NEQUAL, AND, NAND, OR, NOR, XOR, XNOR, MIN, MAX, SLL, SRL, SLA, SRA, ROL, ROR
	};

	private IGOperation fA;

	private IGOperation fB;

	private BinOp fBinOp;

	public IGOperationBinary(IGOperation aA, IGOperation aB, BinOp aBinOp, IGType aType, SourceLocation aSrc, ZDB aZDB) {
		super(aType, aSrc, aZDB);
		fA = aA;
		fB = aB;
		fBinOp = aBinOp;
	}

	@Override
	public void generateCode(boolean aFromInside, IGInterpreterCode aCode) throws ZamiaException {
		fA.generateCode(aFromInside, aCode);
		fB.generateCode(aFromInside, aCode);
		aCode.add(new IGBinaryOpStmt(fBinOp, getType(), computeSourceLocation(), getZDB()));
	}

	@Override
	public IGObject generateCodeRef(boolean aFromInside, boolean aCheckDirection, IGInterpreterCode aCode) throws ZamiaException {
		throw new ZamiaException ("Operation not permitted on left hand side");
	}

	@Override
	public int getNumOperands() {
		return 2;
	}

	@Override
	public IGOperation getOperand(int aIdx) {
		return aIdx == 0 ? fA : fB;
	}

	public IGOperation getA() {
		return fA;
	}

	public IGOperation getB() {
		return fB;
	}

	public BinOp getBinOp() {
		return fBinOp;
	}

	@Override
	public String toString() {
		return "(" + fA.toString() + " " + fBinOp + " " + fB.toString() +")";
	}

	@Override
	public OIDir getDirection() throws ZamiaException {
		return OIDir.NONE;
	}

	@Override
	public void computeAccessedItems(boolean aLeftSide, IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {
		if (aLeftSide) {
			// ?!?
			return;
		}
		fA.computeAccessedItems(aLeftSide, aFilterItem, aFilterType, aDepth, aAccessedItems);
		fB.computeAccessedItems(aLeftSide, aFilterItem, aFilterType, aDepth, aAccessedItems);
	}

}
