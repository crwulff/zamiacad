/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jul 24, 2009
 */
package org.zamia.instgraph.interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperationLiteral;
import org.zamia.instgraph.IGRange;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGStaticValueBuilder;
import org.zamia.instgraph.IGSubProgram;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.IGSubProgram.IGBuiltin;
import org.zamia.instgraph.interpreter.IGStmt.ReturnStatus;
import org.zamia.vhdl.ast.ASTObject.ASTErrorMode;


/**
 * Collection of builtin operation implementations
 * 
 * @author Guenter Bartsch
 * 
 */

public class IGBuiltinOperations {
	private static HashMap<File, LineNumberReader> readerByFile =  new HashMap<File, LineNumberReader>();
	private static HashMap<ZamiaProject, LinkedList<LineNumberReader>> readersByProject = new HashMap<ZamiaProject, LinkedList<LineNumberReader>>();
 
	private final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public static ReturnStatus execBuiltin(IGSubProgram aSub, IGInterpreterRuntimeEnv aRuntime, SourceLocation aLocation, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		IGBuiltin builtin = aSub.getBuiltin();

		switch (builtin) {
		case INT_ABS:
		case INT_NEG:
		case INT_POS:
			return execIntUnary(aSub, aRuntime, aLocation, aErrorMode, aReport);

		case INT_ADD:
		case INT_DIV:
		case INT_MINUS:
		case INT_MOD:
		case INT_MUL:
		case INT_POWER:
		case INT_REM:
			return execIntBinary(aSub, aRuntime, aLocation, aErrorMode, aReport);

		case REAL_ABS:
		case REAL_NEG:
		case REAL_POS:
			return execRealUnary(aSub, aRuntime, aLocation, aErrorMode, aReport);

		case REAL_ADD:
		case REAL_MINUS:
		case REAL_DIV:
		case REAL_MUL:
		case REAL_POWER:
			return execRealBinary(aSub, aRuntime, aLocation, aErrorMode, aReport);

		case SCALAR_EQUALS:
		case SCALAR_GREATER:
		case SCALAR_GREATEREQ:
		case SCALAR_LESS:
		case SCALAR_LESSEQ:
		case SCALAR_NEQUALS:
			return execScalarCompare(aSub, aRuntime, aLocation, aErrorMode, aReport);

		case BOOL_AND:
		case BOOL_NAND:
		case BOOL_NOR:
		case BOOL_OR:
		case BOOL_XNOR:
		case BOOL_XOR:
			return execBoolBinary(aSub, aRuntime, aLocation, aErrorMode, aReport);

		case BOOL_NOT:
			return execBoolNot(aSub, aRuntime, aLocation, aErrorMode, aReport);

		case BIT_NOT:
			return execBitNot(aSub, aRuntime, aLocation, aErrorMode, aReport);

		case BIT_AND:
		case BIT_NAND:
		case BIT_NOR:
		case BIT_OR:
		case BIT_XNOR:
		case BIT_XOR:
			return execBitBinary(aSub, aRuntime, aLocation, aErrorMode, aReport);

		case ARRAY_NOT:
			return execArrayNot(aSub, aRuntime, aLocation, aErrorMode, aReport);

		case ARRAY_EQUALS:
		case ARRAY_NEQUALS:
			return execArrayCompare(aSub, aRuntime, aLocation, aErrorMode, aReport);

		case ARRAY_AND:
		case ARRAY_NAND:
		case ARRAY_NOR:
		case ARRAY_OR:
		case ARRAY_XNOR:
		case ARRAY_XOR:
			return execArrayBinary(aSub, aRuntime, aLocation, aErrorMode, aReport);

		case ARRAY_CONCATAA:
		case ARRAY_CONCATAE:
		case ARRAY_CONCATEA:
		case ARRAY_CONCATEE:
			return execArrayConcat(aSub, aRuntime, aLocation, aErrorMode, aReport);

		case READ:
			return execRead(aSub, aRuntime, aLocation, aErrorMode, aReport);

		case WRITE:
			return execWrite(aSub, aRuntime, aLocation, aErrorMode, aReport);

		case READLINE:
			return execReadline(aSub, aRuntime, aLocation, aErrorMode, aReport);

		case WRITELINE:
			return execWriteline(aSub, aRuntime, aLocation, aErrorMode, aReport);

		case ENDFILE:
			return execEndfile(aSub, aRuntime, aLocation, aErrorMode, aReport);

		default:
			throw new ZamiaException("Sorry, unimplemented builtin: " + builtin, aLocation);
		}

	}

	private static ReturnStatus execIntUnary(IGSubProgram aSub, IGInterpreterRuntimeEnv aRuntime, SourceLocation aLocation, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		IGContainer container = aSub.getContainer();
		IGObject intfA = container.resolveObject("a");

		IGStaticValue v = aRuntime.getObjectValue(intfA);

		BigInteger num = v.getNum();
		BigInteger res = null;

		switch (aSub.getBuiltin()) {
		case INT_ABS:
			res = num.abs();
			break;
		case INT_NEG:
			res = num.negate();
			break;
		case INT_POS:
			res = num;
			break;
		default:
			throw new ZamiaException("Sorry. Internal error. Unsupported operation: " + aSub, aLocation);
		}

		IGTypeStatic rt = aSub.getReturnType().computeStaticType(aRuntime, aErrorMode, aReport);
		if (rt == null) {
			return ReturnStatus.ERROR;
		}

		IGStaticValue resValue = new IGStaticValueBuilder(rt, null, aLocation).setNum(res).buildConstant();

		aRuntime.push(resValue);
		return ReturnStatus.CONTINUE;
	}

	private static ReturnStatus execIntBinary(IGSubProgram aSub, IGInterpreterRuntimeEnv aRuntime, SourceLocation aLocation, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		IGContainer container = aSub.getContainer();
		IGObject intfA = container.resolveObject("a");
		IGStaticValue vA = aRuntime.getObjectValue(intfA);
		BigInteger numA = vA.getNum();

		IGObject intfB = container.resolveObject("b");
		IGStaticValue vB = aRuntime.getObjectValue(intfB);
		BigInteger numB = vB.getNum();

		BigInteger res = null;

		switch (aSub.getBuiltin()) {
		case INT_ADD:
			res = numA.add(numB);
			break;
		case INT_MINUS:
			res = numA.subtract(numB);
			break;
		case INT_DIV:
			res = numA.divide(numB);
			break;
		case INT_MUL:
			res = numA.multiply(numB);
			break;
		case INT_POWER:
			res = numA.pow(numB.intValue());
			break;
		case INT_MOD:
			res = numA.mod(numB);
			if (numB.signum() < 0) {
				res = res.negate();
			}
			break;
		case INT_REM:
			res = numA.remainder(numB);
			break;
		default:
			throw new ZamiaException("Sorry. Internal error. Unsupported operation: " + aSub, aLocation);
		}

		IGTypeStatic rt = aSub.getReturnType().computeStaticType(aRuntime, aErrorMode, aReport);
		if (rt == null) {
			return ReturnStatus.ERROR;
		}

		IGStaticValue resValue = new IGStaticValueBuilder(rt, null, aLocation).setNum(res).buildConstant();

		aRuntime.push(resValue);

		return ReturnStatus.CONTINUE;
	}

	private static ReturnStatus execRealUnary(IGSubProgram aSub, IGInterpreterRuntimeEnv aRuntime, SourceLocation aLocation, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		IGContainer container = aSub.getContainer();
		IGObject intfA = container.resolveObject("a");

		IGStaticValue v = aRuntime.getObjectValue(intfA);

		BigDecimal num = v.getReal();
		BigDecimal res = null;

		switch (aSub.getBuiltin()) {
		case REAL_ABS:
			res = num.abs();
			break;
		case REAL_NEG:
			res = num.negate();
			break;
		case REAL_POS:
			res = num;
			break;
		default:
			throw new ZamiaException("Sorry. Internal error. Unsupported operation: " + aSub, aLocation);
		}

		IGTypeStatic rt = aSub.getReturnType().computeStaticType(aRuntime, aErrorMode, aReport);
		if (rt == null) {
			return ReturnStatus.ERROR;
		}

		IGStaticValue resValue = new IGStaticValueBuilder(rt, null, aLocation).setReal(res).buildConstant();

		aRuntime.push(resValue);

		return ReturnStatus.CONTINUE;
	}

	private static ReturnStatus execRealBinary(IGSubProgram aSub, IGInterpreterRuntimeEnv aRuntime, SourceLocation aLocation, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		IGContainer container = aSub.getContainer();
		IGObject intfA = container.resolveObject("a");
		IGStaticValue vA = aRuntime.getObjectValue(intfA);
		BigDecimal numA = vA.getReal();

		IGObject intfB = container.resolveObject("b");
		IGStaticValue vB = aRuntime.getObjectValue(intfB);
		BigDecimal numB = vB.getReal();

		BigDecimal res = null;

		switch (aSub.getBuiltin()) {
		case REAL_ADD:
			res = numA.add(numB);
			break;
		case REAL_MINUS:
			res = numA.subtract(numB);
			break;
		case REAL_DIV:
			res = numA.divide(numB, 7, BigDecimal.ROUND_HALF_EVEN);
			break;
		case REAL_MUL:
			res = numA.multiply(numB);
			break;
		case REAL_POWER:
			res = numA.pow(numB.intValue());
			break;
		default:
			throw new ZamiaException("Sorry. Internal error. Unsupported operation: " + aSub, aLocation);
		}

		IGTypeStatic rt = aSub.getReturnType().computeStaticType(aRuntime, aErrorMode, aReport);
		if (rt == null) {
			return ReturnStatus.ERROR;
		}

		IGStaticValue resValue = new IGStaticValueBuilder(rt, null, aLocation).setReal(res).buildConstant();

		aRuntime.push(resValue);

		return ReturnStatus.CONTINUE;
	}

	private static ReturnStatus execScalarCompare(IGSubProgram aSub, IGInterpreterRuntimeEnv aRuntime, SourceLocation aLocation, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		IGContainer container = aSub.getContainer();
		IGObject intfA = container.resolveObject("a");
		IGStaticValue vA = aRuntime.getObjectValue(intfA);
		long ordA = vA.getOrd();

		IGObject intfB = container.resolveObject("b");
		IGStaticValue vB = aRuntime.getObjectValue(intfB);
		long ordB = vB.getOrd();

		boolean res = false;

		switch (aSub.getBuiltin()) {
		case SCALAR_EQUALS:
			res = ordA == ordB;
			break;
		case SCALAR_GREATER:
			res = ordA > ordB;
			break;
		case SCALAR_GREATEREQ:
			res = ordA >= ordB;
			break;
		case SCALAR_LESS:
			res = ordA < ordB;
			break;
		case SCALAR_LESSEQ:
			res = ordA <= ordB;
			break;
		case SCALAR_NEQUALS:
			res = ordA != ordB;
			break;
		default:
			throw new ZamiaException("Sorry. Internal error. Unsupported operation: " + aSub, aLocation);
		}

		IGTypeStatic rt = aSub.getReturnType().computeStaticType(aRuntime, aErrorMode, aReport);
		if (rt == null) {
			return ReturnStatus.ERROR;
		}
		IGStaticValue resValue = rt.getEnumLiteral(res ? 1 : 0, aLocation, ASTErrorMode.EXCEPTION, null);

		aRuntime.push(resValue);

		return ReturnStatus.CONTINUE;
	}

	private static ReturnStatus execBoolNot(IGSubProgram aSub, IGInterpreterRuntimeEnv aRuntime, SourceLocation aLocation, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		IGContainer container = aSub.getContainer();
		IGObject intfA = container.resolveObject("a");

		IGStaticValue v = aRuntime.getObjectValue(intfA);

		boolean b = v.getOrd() == 1;
		boolean res = !b;

		IGTypeStatic rt = aSub.getReturnType().computeStaticType(aRuntime, aErrorMode, aReport);
		if (rt == null) {
			return ReturnStatus.ERROR;
		}
		IGStaticValue resValue = rt.getEnumLiteral(res ? 1 : 0, aLocation, ASTErrorMode.EXCEPTION, null);

		aRuntime.push(resValue);
		return ReturnStatus.CONTINUE;
	}

	private static ReturnStatus execBoolBinary(IGSubProgram aSub, IGInterpreterRuntimeEnv aRuntime, SourceLocation aLocation, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		IGContainer container = aSub.getContainer();
		IGObject intfA = container.resolveObject("a");
		IGStaticValue vA = aRuntime.getObjectValue(intfA);
		boolean bA = vA.getOrd() == 1;

		IGObject intfB = container.resolveObject("b");
		IGStaticValue vB = aRuntime.getObjectValue(intfB);
		boolean bB = vB.getOrd() == 1;

		boolean res = false;

		switch (aSub.getBuiltin()) {
		case BOOL_AND:
			res = bA & bB;
			break;

		case BOOL_NAND:
			res = !(bA & bB);
			break;

		case BOOL_NOR:
			res = !(bA | bB);
			break;

		case BOOL_OR:
			res = bA | bB;
			break;

		case BOOL_XNOR:
			res = !(bA ^ bB);
			break;

		case BOOL_XOR:
			res = bA ^ bB;

			break;
		default:
			throw new ZamiaException("Sorry. Internal error. Unsupported operation: " + aSub, aLocation);
		}

		IGTypeStatic rt = aSub.getReturnType().computeStaticType(aRuntime, aErrorMode, aReport);
		if (rt == null) {
			return ReturnStatus.ERROR;
		}
		IGStaticValue resValue = rt.getEnumLiteral(res ? 1 : 0, aLocation, ASTErrorMode.EXCEPTION, null);

		aRuntime.push(resValue);

		return ReturnStatus.CONTINUE;
	}

	private static ReturnStatus execBitNot(IGSubProgram aSub, IGInterpreterRuntimeEnv aRuntime, SourceLocation aLocation, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		IGContainer container = aSub.getContainer();
		IGObject intfA = container.resolveObject("a");

		IGStaticValue v = aRuntime.getObjectValue(intfA);

		boolean b = v.getOrd() == 1;
		boolean res = !b;

		IGTypeStatic rt = aSub.getReturnType().computeStaticType(aRuntime, aErrorMode, aReport);
		if (rt == null) {
			return ReturnStatus.ERROR;
		}
		IGStaticValue resValue = rt.getEnumLiteral(res ? 1 : 0, aLocation, ASTErrorMode.EXCEPTION, null);

		aRuntime.push(resValue);
		return ReturnStatus.CONTINUE;
	}

	private static ReturnStatus execBitBinary(IGSubProgram aSub, IGInterpreterRuntimeEnv aRuntime, SourceLocation aLocation, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		IGBuiltin originalBuiltin = aSub.getBuiltin();
		IGBuiltin boolBuiltin;
		switch (originalBuiltin) {
			case BIT_AND:
				boolBuiltin = IGBuiltin.BOOL_AND; break;
			case BIT_NAND:
				boolBuiltin = IGBuiltin.BOOL_NAND; break;
			case BIT_NOR:
				boolBuiltin = IGBuiltin.BOOL_NOR; break;
			case BIT_OR:
				boolBuiltin = IGBuiltin.BOOL_OR; break;
			case BIT_XNOR:
				boolBuiltin = IGBuiltin.BOOL_XNOR; break;
			case BIT_XOR:
				boolBuiltin = IGBuiltin.BOOL_XOR; break;
			default:
				throw new ZamiaException("Sorry, unimplemented builtin: " + originalBuiltin, aLocation);
		}
		aSub.setBuiltin(boolBuiltin);
		ReturnStatus returnStatus = execBoolBinary(aSub, aRuntime, aLocation, aErrorMode, aReport);
		aSub.setBuiltin(originalBuiltin);

		return returnStatus;
	}

	private static ReturnStatus execArrayNot(IGSubProgram aSub, IGInterpreterRuntimeEnv aRuntime, SourceLocation aLocation, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		IGContainer container = aSub.getContainer();
		IGObject intfA = container.resolveObject("a");

		IGStaticValue value = aRuntime.getObjectValue(intfA);

		int offset = value.getArrayOffset();
		int n = value.getNumArrayEntries(aLocation);
		IGTypeStatic rt = aSub.getReturnType().computeStaticType(aRuntime, aErrorMode, aReport);
		if (rt == null) {
			return ReturnStatus.ERROR;
		}
		IGType et = rt.getElementType();

		IGStaticValueBuilder builder = new IGStaticValueBuilder(rt, null, aLocation);

		for (int i = 0; i < n; i++) {

			IGStaticValue v = value.getValue(i + offset, aLocation);
			boolean b = v.getOrd() == 1;

			v = et.getEnumLiteral(!b ? 1 : 0, aLocation, ASTErrorMode.EXCEPTION, null);

			builder.set(i + offset, v, aLocation);
		}

		IGStaticValue resValue = builder.buildConstant();

		aRuntime.push(resValue);
		return ReturnStatus.CONTINUE;
	}

	private static ReturnStatus execArrayCompare(IGSubProgram aSub, IGInterpreterRuntimeEnv aRuntime, SourceLocation aLocation, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		IGContainer container = aSub.getContainer();
		IGObject intfA = container.resolveObject("a");
		IGStaticValue valueA = aRuntime.getObjectValue(intfA);

		IGObject intfB = container.resolveObject("b");
		IGStaticValue valueB = aRuntime.getObjectValue(intfB);

		int offsetA = valueA.getArrayOffset();
		int offsetB = valueB.getArrayOffset();
		int nA = valueA.getNumArrayEntries(aLocation);
		int nB = valueB.getNumArrayEntries(aLocation);

		boolean doEquals = true;
		switch (aSub.getBuiltin()) {
		case ARRAY_EQUALS:
			break;
		case ARRAY_NEQUALS:
			doEquals = false;
			break;
		default:
			throw new ZamiaException("Internal interpreter error: execArrayCompare() called on non-compare op " + aSub.getBuiltin(), aLocation);
		}

		boolean bRes = false;

		if (nA != nB) {
			bRes = !doEquals;
		} else {

			// compute equals, invert it if necessary

			bRes = true;
			for (int i = 0; i < nA; i++) {

				IGStaticValue vA = valueA.getValue(i + offsetA, aLocation);
				IGStaticValue vB = valueB.getValue(i + offsetB, aLocation);

				if (!vA.equalsValue(vB)) {
					bRes = false;
					break;
				}
			}

			if (!doEquals) {
				bRes = !bRes;
			}
		}

		IGTypeStatic rt = aSub.getReturnType().computeStaticType(aRuntime, aErrorMode, aReport);
		if (rt == null) {
			return ReturnStatus.ERROR;
		}
		IGStaticValue resValue = rt.getEnumLiteral(bRes ? 1 : 0, aLocation, ASTErrorMode.EXCEPTION, null);

		aRuntime.push(resValue);
		return ReturnStatus.CONTINUE;
	}

	private static ReturnStatus execArrayBinary(IGSubProgram aSub, IGInterpreterRuntimeEnv aRuntime, SourceLocation aLocation, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		IGContainer container = aSub.getContainer();
		IGObject intfA = container.resolveObject("a");
		IGStaticValue valueA = aRuntime.getObjectValue(intfA);

		IGObject intfB = container.resolveObject("b");
		IGStaticValue valueB = aRuntime.getObjectValue(intfB);

		int offsetA = valueA.getArrayOffset();
		int offsetB = valueB.getArrayOffset();
		int nA = valueA.getNumArrayEntries(aLocation);
		int nB = valueB.getNumArrayEntries(aLocation);

		IGTypeStatic rt = aSub.getReturnType().computeStaticType(aRuntime, aErrorMode, aReport);
		if (rt == null) {
			return ReturnStatus.ERROR;
		}

		IGTypeStatic et = rt.getStaticElementType(aLocation);

		if (rt.isUnconstrained()) {
			// compute constraints at runtime
			rt = valueA.getStaticType();
			if (rt.isUnconstrained()) {
				rt = valueB.getStaticType();
				if (rt.isUnconstrained()) {
					throw new ZamiaException("Interpreter error: cannot determine resulting array boundaries, all types involved are unconstrained :-/", aLocation);
				}
			}
		}

		IGStaticValueBuilder builder = new IGStaticValueBuilder(rt, null, aLocation);

		int offsetR = builder.getArrayOffset();

		if (nA != nB) {
			throw new ZamiaException("Interpreter error: execArrayBinary() called on non-equal-lengths operands: " + valueA + ", " + valueB, aLocation);
		} else {

			for (int i = 0; i < nA; i++) {

				IGStaticValue vA = valueA.getValue(i + offsetA, aLocation);
				IGStaticValue vB = valueB.getValue(i + offsetB, aLocation);

				boolean bA = vA.getOrd() == 1;
				boolean bB = vB.getOrd() == 1;
				boolean res = false;

				switch (aSub.getBuiltin()) {
				case ARRAY_AND:
					res = bA & bB;
					break;

				case ARRAY_NAND:
					res = !(bA & bB);
					break;

				case ARRAY_NOR:
					res = !(bA | bB);
					break;

				case ARRAY_OR:
					res = bA | bB;
					break;

				case ARRAY_XNOR:
					res = !(bA ^ bB);
					break;

				case ARRAY_XOR:
					res = bA ^ bB;

					break;

				default:
					throw new ZamiaException("Internal interpreter error: execArrayBinary() called on non-implemented op " + aSub.getBuiltin(), aLocation);
				}

				IGStaticValue resValue = et.getEnumLiteral(res ? 1 : 0, aLocation, ASTErrorMode.EXCEPTION, null);

				builder.set(i + offsetR, resValue, aLocation);
			}
		}

		IGStaticValue resValue = builder.buildConstant();

		aRuntime.push(resValue);
		return ReturnStatus.CONTINUE;
	}

	private static ReturnStatus execArrayConcat(IGSubProgram aSub, IGInterpreterRuntimeEnv aRuntime, SourceLocation aLocation, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		IGContainer container = aSub.getContainer();
		IGObject intfA = container.resolveObject("a");
		IGStaticValue valueA = aRuntime.getObjectValue(intfA);

		IGObject intfB = container.resolveObject("b");
		IGStaticValue valueB = aRuntime.getObjectValue(intfB);

		IGTypeStatic rt = aSub.getReturnType().computeStaticType(aRuntime, aErrorMode, aReport);
		if (rt == null) {
			return ReturnStatus.ERROR;
		}

		IGTypeStatic tA = valueA.getStaticType();
		IGTypeStatic tB = valueB.getStaticType();

		if (rt.isUnconstrained()) {

			// compute constraints at runtime

			int l = 0, r = 0;
			boolean a = true;

			IGTypeStatic idxType = rt.getStaticIndexType(aLocation);

			switch (aSub.getBuiltin()) {
			case ARRAY_CONCATAA:

				IGStaticValue aRange = idxType.getStaticRange();
				l = (int) aRange.getLeft().getOrd();
				a = aRange.getAscending().isTrue();

				int cardA = (int) tA.getStaticIndexType(aLocation).computeCardinality(aLocation);
				int cardB = (int) tB.getStaticIndexType(aLocation).computeCardinality(aLocation);

				r = a ? l + cardA + cardB - 1 : l - cardA - cardB - 1;

				//				IGStaticValue aRange = tA.getStaticIndexType(aLocation).getStaticRange();
				//				l = (int) aRange.getLeft().getOrd();
				//				r = (int) aRange.getRight().getOrd();
				//				a = aRange.getAscending().isTrue();
				//				int card = (int) tB.getStaticIndexType(aLocation).computeCardinality(aLocation);
				//				r = a ? r + card : r - card;
				break;

			case ARRAY_CONCATAE:

				aRange = tA.getStaticIndexType(aLocation).getStaticRange();
				l = (int) aRange.getLeft().getOrd();
				r = (int) aRange.getRight().getOrd();
				a = aRange.getAscending().isTrue();

				r = a ? r + 1 : r - 1;
				break;

			case ARRAY_CONCATEA:

				IGStaticValue bRange = tB.getStaticIndexType(aLocation).getStaticRange();
				l = (int) bRange.getLeft().getOrd();
				r = (int) bRange.getRight().getOrd();
				a = bRange.getAscending().isTrue();

				l = a ? l - 1 : l + 1;
				break;

			case ARRAY_CONCATEE:

				l = idxType.getStaticLow(aLocation).getInt();
				a = idxType.isAscending();
				r = l + 1;

				break;

			default:
				throw new ZamiaException("Internal interpreter error: execArrayConcat() called on non-implemented op " + aSub.getBuiltin(), aLocation);
			}

			IGStaticValue left = new IGStaticValueBuilder(idxType, null, aLocation).setOrd(l).buildConstant();
			IGStaticValue right = new IGStaticValueBuilder(idxType, null, aLocation).setOrd(r).buildConstant();

			IGType boolT = container.findBoolType();

			IGStaticValue ascending = boolT.getEnumLiteral(a ? 1 : 0, aLocation, ASTErrorMode.EXCEPTION, null);

			IGTypeStatic rType = idxType.getStaticRange().getStaticType();
			IGStaticValue range = new IGStaticValueBuilder(rType, null, aLocation).setLeft(left).setRight(right).setAscending(ascending).buildConstant();

			rt = rt.createSubtype(range, aLocation);
		}

		IGStaticValueBuilder b = new IGStaticValueBuilder(rt, null, aLocation);

		int off = b.getArrayOffset();

		if (aSub.getBuiltin() == IGBuiltin.ARRAY_CONCATAA || aSub.getBuiltin() == IGBuiltin.ARRAY_CONCATAE) {
			int n = (int) tA.getStaticIndexType(aLocation).computeCardinality(aLocation);
			int offA = valueA.getArrayOffset();
			for (int i = 0; i < n; i++) {
				IGStaticValue op = valueA.getValue(i + offA, aLocation);
				b.set(i + off, op, aLocation);
			}
			off += n;
		} else {
			b.set(off, valueA, aLocation);
			off += 1;
		}

		if (aSub.getBuiltin() == IGBuiltin.ARRAY_CONCATAA || aSub.getBuiltin() == IGBuiltin.ARRAY_CONCATEA) {
			int n = (int) tB.getStaticIndexType(aLocation).computeCardinality(aLocation);
			int offB = valueB.getArrayOffset();
			for (int i = 0; i < n; i++) {
				IGStaticValue op = valueB.getValue(i + offB, aLocation);
				b.set(i + off, op, aLocation);
			}
		} else {
			b.set(off, valueB, aLocation);
		}

		aRuntime.push(b.buildConstant());
		return ReturnStatus.CONTINUE;
	}

	private static ReturnStatus execRead(IGSubProgram aSub, IGInterpreterRuntimeEnv aRuntime, SourceLocation aLocation, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		IGContainer container = aSub.getContainer();
		IGObject intfL = container.resolveObject("L");
		IGStaticValue valueF = aRuntime.getObjectValue(intfL);

		IGObject intfV = container.resolveObject("VALUE");
		IGObject intfG = container.resolveObject("GOOD");

		aRuntime.setObjectValue(intfV, valueF, aLocation);
		aRuntime.setObjectValue(intfG, valueF == null ? container.findFalseValue() : container.findTrueValue(), aLocation);

		return ReturnStatus.CONTINUE;
	}

	private static ReturnStatus execWrite(IGSubProgram aSub, IGInterpreterRuntimeEnv aRuntime, SourceLocation aLocation, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {
		//todo: also process JUSTIFIED and FIELD
		IGContainer container = aSub.getContainer();
		IGObject intfL = container.resolveObject("L");

		IGObject intfV = container.resolveObject("VALUE");
		IGStaticValue valueV = aRuntime.getObjectValue(intfV);
		//todo: aRuntime.getObjectValue(intfL) and append to this value before setting object value to intfL.
		aRuntime.setObjectValue(intfL, valueV, aLocation);

		return ReturnStatus.CONTINUE;
	}

	private static ReturnStatus execReadline(IGSubProgram aSub, IGInterpreterRuntimeEnv aRuntime, SourceLocation aLocation, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		IGContainer container = aSub.getContainer();
		IGObject intfF = container.resolveObject("F");
		IGStaticValue valueF = aRuntime.getObjectValue(intfF);

		IGObject intfL = container.resolveObject("L");

		String filePath = valueF.getId();
		ZamiaProject zprj = aSub.getZPrj();
		File file = new File(zprj.getBasePath() + File.separator + filePath);

		String line = null;
		LineNumberReader reader = null;
		try {

			reader = getReader(file, zprj);

			line = reader.readLine();

			logger.debug("Line # %d read from %s", reader.getLineNumber(), file.getAbsolutePath());

		} catch (FileNotFoundException e) {
			throw new ZamiaException("File " + file.getAbsolutePath() + " not found while executing " + aSub, aLocation);
		} catch (IOException e) {
			throw new ZamiaException("Error while reading file " + file.getAbsolutePath() + ":\n" + e.getMessage(), aLocation);
		} finally {
			if (line == null) { // close reader only when tried to read after EOF was reached and ...
				close(reader);
			}
		}

		if (line == null) { // ... and throw exception
			throw new ZamiaException("IGBuiltinOperations: execReadline(): trying to read " + file.getName() + " file after EOF was reached", aLocation);
		}

		IGType stringType = container.findStringType();

		IGTypeStatic intTypeS = container.findUniversalIntType().computeStaticType(aRuntime, ASTErrorMode.EXCEPTION, aReport);
		IGStaticValue left = new IGStaticValueBuilder(intTypeS, null, null).setNum(0).buildConstant();
		IGStaticValue right = new IGStaticValueBuilder(intTypeS, null, null).setNum(line.length() - 1).buildConstant();
		IGStaticValue ascending = container.findTrueValue();

		IGRange range = new IGRange(left, right, ascending, aLocation, aSub.getZDB());
		stringType = stringType.createSubtype(range, aRuntime, aLocation);

		IGOperationLiteral lineOL = new IGOperationLiteral(line, stringType, aLocation);
		IGStaticValue ret = lineOL.computeStaticValue(aRuntime, aErrorMode, aReport);

		aRuntime.setObjectValue(intfL, ret, aLocation);

		return ReturnStatus.CONTINUE;
	}

	private static ReturnStatus execWriteline(IGSubProgram aSub, IGInterpreterRuntimeEnv aRuntime, SourceLocation aLocation, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		IGContainer container = aSub.getContainer();
		IGObject intfF = container.resolveObject("F");

		IGObject intfL = container.resolveObject("L");
		IGStaticValue valueL = aRuntime.getObjectValue(intfL);

		aRuntime.setObjectValue(intfF, valueL, aLocation);

		return ReturnStatus.CONTINUE;
	}

	private static ReturnStatus execEndfile(IGSubProgram aSub, IGInterpreterRuntimeEnv aRuntime, SourceLocation aLocation, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		IGContainer container = aSub.getContainer();
		IGObject intfF = container.resolveObject("F");
		IGStaticValue valueF = aRuntime.getObjectValue(intfF);

		String filePath = valueF.getId();
		ZamiaProject zprj = aSub.getZPrj();
		File file = new File(zprj.getBasePath() + File.separator + filePath);

		boolean ready;
		try {

			BufferedReader reader = getReader(file, zprj);

			ready = reader.ready();

		} catch (FileNotFoundException e) {
			throw new ZamiaException("File " + file.getAbsolutePath() + " not found while executing " + aSub, aLocation);
		} catch (IOException e) {
			throw new ZamiaException("Error while checking file " + file.getAbsolutePath() + " for readiness:\n" + e.getMessage(), aLocation);
		}

		IGStaticValue ret = ready ? container.findFalseValue() : container.findTrueValue();

		aRuntime.push(ret);

		return ReturnStatus.CONTINUE;
	}

	private static LineNumberReader getReader(File file, ZamiaProject zprj) throws FileNotFoundException {

		LineNumberReader reader = readerByFile.get(file);

		if (reader != null) {
			return reader;
		}

		reader = new LineNumberReader(new BufferedReader(new FileReader(file)));

		readerByFile.put(file, reader);
		getReadersFrom(zprj).add(reader);

		return reader;
	}

	private static LinkedList<LineNumberReader> getReadersFrom(ZamiaProject zprj) {
		LinkedList<LineNumberReader> readers = readersByProject.get(zprj);
		if (readers == null) {
			readers = new LinkedList<LineNumberReader>();
			readersByProject.put(zprj, readers);
		}
		return readers;
	}

	public static void remove(ZamiaProject aZprj) {
		//FOXME: todo: when deleting project from Navigator, call this method to release the potentially occupied resources
		LinkedList<LineNumberReader> readers = getReadersFrom(aZprj);
		for (LineNumberReader reader : readers) {
			close(reader);
		}
		readersByProject.remove(aZprj);
	}

	private static void close(LineNumberReader reader) {
		try {
			if (reader != null) {
				reader.close();
			}
		} catch (IOException e) {}
	}

}
