/* 
 * Copyright 2009,2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jun 11, 2009
 */
package org.zamia.instgraph;


import java.math.BigDecimal;
import java.math.BigInteger;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.instgraph.interpreter.IGPushStmt;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * separated into subclasses (separating conditionals with polymorphism) by valentin tihhomirov
 * 
 */

@SuppressWarnings("serial")
public abstract class IGOperationLiteral extends IGOperation {

	public static IGStaticValueBuilder computeString(String aInput, IGStaticValueBuilder aBuilder, SourceLocation aSourceLocation) throws ZamiaException {

		IGTypeStatic t = aBuilder.getType();
		IGTypeStatic et = t.getStaticElementType(aSourceLocation);

		int n = aInput.length();
		int offset = aBuilder.getArrayOffset();
		for (int i = 0; i < n; i++) {
			char c = aInput.charAt(n-1-i);

			IGStaticValue el = et.findEnumLiteral(c);

			if (el == null) {
				el = et.getEnumLiteral(c, aSourceLocation, null, null);
				if (el == null)
					throw new ZamiaException("Couldn't find character literal " + c, aSourceLocation);
			}

			aBuilder.set(offset + i, el, aSourceLocation);

		}

		return aBuilder;
	}

	public IGOperationLiteral(IGType aType, SourceLocation aSrc, ZDB aZDB) {
		super(aType, aSrc, aZDB);
	}

	@Override
	public void computeAccessedItems(boolean aLeftSide, IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) { }
	
	@Override
	public void generateCode(boolean aFromInside, IGInterpreterCode aCode) throws ZamiaException {
		aCode.add(new IGPushStmt.LITERAL(this, computeSourceLocation(), getZDB()));
	}

	@Override
	public OIDir getDirection() throws ZamiaException {
		return OIDir.NONE;
	}

	@Override
	public int getNumOperands() {
		return 0;
	}

	@Override
	public IGOperation getOperand(int aIdx) {
		return null;
	}
	
	abstract protected IGStaticValueBuilder computeStaticValue(IGStaticValueBuilder builder) throws ZamiaException;
	public IGStaticValue computeStaticValue(IGInterpreterRuntimeEnv aRuntime) throws ZamiaException {

		IGStaticValue ac = null;
		
		if (aRuntime != null) {
			ac = aRuntime.getCachedLiteralActualConstant(this);
			if (ac != null) {
				return ac;
			}
		}

		IGTypeStatic t = getType().computeStaticType(aRuntime, ASTErrorMode.EXCEPTION, null);

		IGStaticValueBuilder builder = new IGStaticValueBuilder(t, getId(), computeSourceLocation());
		ac = computeStaticValue(builder).buildConstant();
		
		if (ac == null) {
			throw new ZamiaException("Internal error. sorry.", computeSourceLocation());
		}

		if (aRuntime != null) {
			aRuntime.putCachedLiteralActualConstant(this, ac);
		}
		return ac;
	}

	public String toString() {
		return "IGOperationLiteral(?????)";
	}
	
	
	
	
	
	
	
	
	public static class ACCESS extends IGOperationLiteral {
		public ACCESS(IGType aType, SourceLocation aSrc) {
			super(aType, aSrc, aType.getZDB());
		}

		public String toString() {
			return "NULL";
		}

		protected IGStaticValueBuilder computeStaticValue(IGStaticValueBuilder builder) throws ZamiaException {
			return builder;
		}

	}	
	
	public static class STR extends IGOperationLiteral {
		final protected String fStr;
		public STR(String aStr, IGType aType, SourceLocation aSrc) {
			super(aType, aSrc, aType.getZDB());
			fStr = aStr;
		}
		
		@Override
		public String getId() {
			return fStr;
		}
		
		@Override
		protected IGStaticValueBuilder computeStaticValue(IGStaticValueBuilder builder) throws ZamiaException {
			return IGOperationLiteral.computeString(fStr, builder, computeSourceLocation());
		}
		
		@Override
		public void generateCode(boolean aFromInside, IGInterpreterCode aCode) throws ZamiaException {
			aCode.add(new IGPushStmt.LITERAL(this, computeSourceLocation(), getZDB()));
		}
	
		@Override
		public String toString() {
			return "\"" + fStr + "\"";
		}
	}
	
	public static class INT extends IGOperationLiteral {
		final private BigInteger fNum;
		public INT(BigInteger aNum, IGType aType, SourceLocation aSrc) {
			super(aType, aSrc, aType.getZDB());
			fNum = aNum;
		}
//		// convenience:
		public INT(int aNum, IGType aType, SourceLocation aSrc) {
			this(new BigInteger("" + aNum), aType, aSrc);
		}

		public INT(long aNum, IGType aType, SourceLocation aSrc) {
			this(new BigInteger("" + aNum), aType, aSrc);
		}
		
		@Override
		protected IGStaticValueBuilder computeStaticValue(IGStaticValueBuilder builder) {
			return builder.setNum(fNum);
		}

		public BigInteger getNum() {
			return fNum;
		}

		@Override
		public String toString() {
			return fNum.toString();
		}
	}
	
	public static class DECIMAL extends IGOperationLiteral {
		private BigDecimal fReal;
		public DECIMAL(BigDecimal aReal, IGType aType, SourceLocation aSrc) {
			super(aType, aSrc, aType.getZDB());
			fReal = aReal;
		}
//		// convenience:
		public DECIMAL(double aDec, IGType aType, SourceLocation aSrc) {
			this(new BigDecimal("" + aDec), aType, aSrc);
		}
		@Override
		protected IGStaticValueBuilder computeStaticValue(IGStaticValueBuilder builder) {
			return builder.setReal(fReal);
		}
		
		@Override
		public String toString() {
			return fReal.toString();
		}
	}	
	public static class CHAR extends IGOperationLiteral {
		final protected char fChar;
		public CHAR(char aChar, IGType aType, SourceLocation aSrc) {
			super(aType, aSrc, aType.getZDB());
			fChar = aChar;
		}
		public String getId() {
			return "" + fChar;
		}
		@Override
		protected IGStaticValueBuilder computeStaticValue(IGStaticValueBuilder builder) {
			return builder.setChar(fChar);
		}
		public char getChar() {
			return fChar;
		}

		@Override
		public String toString() {
			return "'" + fChar + "'";
		}
	}
	
}
