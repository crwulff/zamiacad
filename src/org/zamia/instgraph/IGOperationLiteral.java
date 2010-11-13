/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
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


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class IGOperationLiteral extends IGOperation {

	public enum IGOLCat {
		INTEGER, DECIMAL, STRING, CHAR, ACCESS
	};

	protected IGOLCat fCat;

	private BigInteger fNum;

	private BigDecimal fReal;

	protected String fStr;

	protected char fChar;

	public IGOperationLiteral(BigInteger aNum, IGType aType, SourceLocation aSrc) {
		super(aType, aSrc, aType.getZDB());
		fCat = IGOLCat.INTEGER;
		fNum = aNum;
	}

	public IGOperationLiteral(BigDecimal aReal, IGType aType, SourceLocation aSrc) {
		super(aType, aSrc, aType.getZDB());
		fCat = IGOLCat.DECIMAL;
		fReal = aReal;
	}

	public IGOperationLiteral(String aStr, IGType aType, SourceLocation aSrc) {
		super(aType, aSrc, aType.getZDB());
		fCat = IGOLCat.STRING;
		fStr = aStr;
	}

	public IGOperationLiteral(char aChar, IGType aType, SourceLocation aSrc) {
		super(aType, aSrc, aType.getZDB());
		fCat = IGOLCat.CHAR;
		fChar = aChar;
	}

	public IGOperationLiteral(IGType aType, SourceLocation aSrc) {
		super(aType, aSrc, aType.getZDB());
		fCat = IGOLCat.ACCESS;
	}

	// convenience:
	public IGOperationLiteral(int aNum, IGType aType, SourceLocation aSrc) {
		this(new BigInteger("" + aNum), aType, aSrc);
	}

	public IGOperationLiteral(long aNum, IGType aType, SourceLocation aSrc) {
		this(new BigInteger("" + aNum), aType, aSrc);
	}

	public IGOperationLiteral(double aDec, IGType aType, SourceLocation aSrc) {
		this(new BigDecimal("" + aDec), aType, aSrc);
	}

	@Override
	public void computeAccessedItems(boolean aLeftSide, IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {
	}

	public IGStaticValue computeStaticValue(IGInterpreterRuntimeEnv aRuntime) throws ZamiaException {

		IGStaticValue ac = null;
		
		if (aRuntime != null) {
			ac = aRuntime.getCachedLiteralActualConstant(this);
			if (ac != null) {
				return ac;
			}
		}

		IGTypeStatic t = getType().computeStaticType(aRuntime, ASTErrorMode.EXCEPTION, null);

		switch (fCat) {
		case DECIMAL:
			ac = new IGStaticValueBuilder(t, getId(), computeSourceLocation()).setReal(fReal).buildConstant();
			break;

		case INTEGER:
			ac = new IGStaticValueBuilder(t, getId(), computeSourceLocation()).setNum(fNum).buildConstant();
			break;

		case STRING:

			IGStaticValueBuilder builder = new IGStaticValueBuilder(t, getId(), computeSourceLocation());

			// FIXME: to / downto

			IGTypeStatic et = t.getStaticElementType(computeSourceLocation());

			int n = fStr.length();
			int offset = builder.getArrayOffset();
			for (int i = 0; i < n; i++) {
				char c = fStr.charAt(n-1-i);

				// IGActualConstantBuilder b = builder.getBuilder(offset+i,
				// computeSourceLocation());
				// b.setChar(fStr.charAt(i));

				IGStaticValue el = et.findEnumLiteral(c);

				if (el == null) {
					throw new ZamiaException("Couldn't find character literal " + c, computeSourceLocation());
				}

				builder.set(offset + i, el, computeSourceLocation());

			}

			ac = builder.buildConstant();
			break;

		case CHAR:
			ac = new IGStaticValueBuilder(t, getId(), computeSourceLocation()).setChar(fChar).buildConstant();
			break;

		}

		if (ac == null) {
			throw new ZamiaException("Internal error. sorry.", computeSourceLocation());
		}

		if (aRuntime != null) {
			aRuntime.putCachedLiteralActualConstant(this, ac);
		}
		return ac;
	}

	@Override
	public void generateCode(boolean aFromInside, IGInterpreterCode aCode) throws ZamiaException {
		aCode.add(new IGPushStmt(this, computeSourceLocation(), getZDB()));
	}

	@Override
	public IGObject generateCodeRef(boolean aFromInside, boolean aCheckDirection, IGInterpreterCode aCode) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Sorry, not implemented yet.");
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

	@Override
	public String getId() {
		switch (fCat) {
		case STRING:
			return fStr;
		case CHAR:
			return "" + fChar;
		}
		return super.getId();
	}

	@Override
	public String toString() {
		switch (fCat) {
		case DECIMAL:
			return fReal.toString();
		case INTEGER:
			return fNum.toString();
		case STRING:
			return "\"" + fStr + "\"";
		case CHAR:
			return "'" + fChar + "'";
		case ACCESS:
			return "NULL";
		}
		return "IGOperationLiteral(???)";
	}

	public boolean isCharLiteral() {
		return fCat == IGOLCat.CHAR;
	}

	public BigInteger getNum() {
		return fNum;
	}

	public char getChar() {
		return fChar;
	}

}
