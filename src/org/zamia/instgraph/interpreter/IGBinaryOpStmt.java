/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 11, 2009
 */
package org.zamia.instgraph.interpreter;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGOperationBinary.BinOp;
import org.zamia.instgraph.IGStaticValue;
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
public class IGBinaryOpStmt extends IGOpStmt {

	private BinOp fOp;

	public IGBinaryOpStmt(BinOp aOp, IGType aResType, SourceLocation aLocation, ZDB aZDB) {
		super(aResType, aLocation, aZDB);
		fOp = aOp;
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		SourceLocation location = computeSourceLocation();
		
		IGStackFrame sf2 = aRuntime.pop();
		IGStaticValue op2 = sf2.getValue();

		if (op2 == null) {
			if (aErrorMode == ASTErrorMode.RETURN_NULL) {
				return ReturnStatus.ERROR;
			}
			throw new ZamiaException ("IGBinaryOpStmt: op2==null", location);
		}

		IGStackFrame sf1 = aRuntime.pop();
		IGStaticValue op1 = sf1.getValue();

		if (op1 == null) {
			if (aErrorMode == ASTErrorMode.RETURN_NULL) {
				return ReturnStatus.ERROR;
			}
			throw new ZamiaException ("IGBinaryOpStmt: op1==null", location);
		}

		IGTypeStatic rt = getType().computeStaticType(aRuntime, aErrorMode, aReport);
		if (rt == null) {
			return ReturnStatus.ERROR;
		}

		IGStaticValue resValue = IGStaticValue.computeBinary(op1, op2, fOp, rt, location, aErrorMode, aReport);
		if (resValue == null) {
			return ReturnStatus.ERROR;
		}
		aRuntime.push(resValue);

		logLogicalValue(resValue);

		return ReturnStatus.CONTINUE;
	}

	private boolean fHasTrueOccurred, fHasFalseOccurred, fIsRelational;

	void logLogicalValue(IGStaticValue logicalValue) throws ZamiaException {

		fIsRelational = isRelational(fOp);
		if (!fIsRelational) {
			return;
		}

		if (logicalValue.isTrue())
			fHasTrueOccurred = fHasTrueOccurred || true;
		else
			fHasFalseOccurred = fHasFalseOccurred || true;
	}

	private static boolean isRelational(BinOp binOp) {
		switch (binOp) {
			case EQUAL:
			case LESSEQ:
			case LESS:
			case GREATER:
			case GREATEREQ:
			case NEQUAL:
			case AND:
			case NAND:
			case OR:
			case NOR:
			case XOR:
			case XNOR:
				return true;
			default:
				return false;
		}
	}

	public boolean isRelational() {
		return fIsRelational;
	}

	public boolean hasTrueOccurred() {
		return fHasTrueOccurred;
	}

	public boolean hasFalseOccurred() {
		return fHasFalseOccurred;
	}

	@Override
	public String toString() {
		return "BINARY OP " + fOp;
	}
}
