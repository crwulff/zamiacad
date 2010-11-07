/* 
 * Copyright 2004-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created on Jan 22, 2004 by Guenter Bartsch
 * 
 */
package org.zamia.zil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.rtl.RTLGraph;
import org.zamia.rtl.RTLLiteral;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLSignal;
import org.zamia.util.HashMapArray;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.vhdl.ast.OperationCompare.CompareOp;
import org.zamia.vhdl.ast.OperationLogic.LogicOp;
import org.zamia.vhdl.ast.OperationMath.MathOp;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILPushStmt;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * This represents any constant value on the RTL level, such as the result of
 * elaborating a literal or values computed by constant propagation or the RTL
 * simulator
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class ZILValue extends ZILOperation implements ZILIReferable, Serializable {

	public final static char BIT_0 = '0';

	public final static char BIT_1 = '1';

	public final static char BIT_Z = 'Z';

	public final static char BIT_X = 'X';

	public final static char BIT_U = 'U';

	public final static char BIT_DC = '-';

	private BigInteger fNum;

	private BigDecimal fReal;

	private ArrayList<ZILValue> fArrayValue;

	private int fArrayOffset;

	private HashMapArray<ZILRecordField, ZILValue> fRecordValues;

	private int fLeft;

	private int fRight;

	private boolean fAscending;

	private ZILEnumLiteral fEnumLiteral;

	private ZILValue fPtr; // access types

	private static int counter=0;
	
	private int fCnt = counter++; // debug purposes

	public ZILValue getPtr() {
		return fPtr;
	}

	protected ZILValue(String aId, ZILType aType, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aType, aContainer, aSrc);
		fId = aId;
	}
	
	public ZILValue(BigInteger aNum, ZILTypeInteger aType, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {
		super(aType, aContainer, aSrc);
		fNum = aNum;
	}

	public ZILValue(int aNum, ZILTypeInteger aType, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {
		this(new BigInteger("" + aNum), aType, aContainer, aSrc);
	}

	public ZILValue(long aNum, ZILTypeInteger aType, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {
		this(new BigInteger("" + aNum), aType, aContainer, aSrc);
	}

	public ZILValue(char aChar, ZILTypeEnum aType, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {
		super(aType, aContainer, aSrc);
		fEnumLiteral = aType.findEnumLiteral(aChar);
		if (fEnumLiteral == null) {
			throw new ZamiaException("Internal error: enum literal not found ");
		}
	}

	public ZILValue(BigDecimal aBigDecimal, ZILTypeReal aType, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {
		super(aType, aContainer, aSrc);
		fReal = aBigDecimal;
	}

	public ZILValue(double aDouble, ZILTypeReal aType, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {
		super(aType, aContainer, aSrc);
		fReal = new BigDecimal(aDouble);
	}

	public ZILValue(BigDecimal aBigDecimal, ZILTypePhysical aType, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {
		super(aType, aContainer, aSrc);
		fReal = aBigDecimal;
	}

	public ZILValue(String aId, BigDecimal aBigDecimal, ZILTypePhysical aType, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {
		super(aType, aContainer, aSrc);
		fReal = aBigDecimal;
		fId = aId;
	}

	public ZILValue(double aDouble, ZILTypePhysical aType, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {
		super(aType, aContainer, aSrc);
		fReal = new BigDecimal(aDouble);
	}

	public ZILValue(ZILTypeArray aType, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {
		super(aType, aContainer, aSrc);

		ZILTypeDiscrete indexType = aType.getIndexType();

		if (indexType != null) {
			int card = (int) indexType.getCardinality();

			if (card > 0) {
				fArrayValue = new ArrayList<ZILValue>(card);

				for (int i = 0; i < card; i++) {
					fArrayValue.add(null);
				}
			}

			fArrayOffset = (int) indexType.getLow().getInt(aSrc);
		}
	}

	public ZILValue(ZILTypeRecord aType, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {
		super(aType, aContainer, aSrc);
		fRecordValues = new HashMapArray<ZILRecordField, ZILValue>();
	}

	public ZILValue(String v_, ZILTypeEnum aType, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {
		super(aType, aContainer, aSrc);
		fEnumLiteral = aType.findEnumLiteral(v_);
		if (fEnumLiteral == null) {
			throw new ZamiaException("Literal " + v_ + " not found in enum type " + aType, aType.getSrc());
		}
	}

	public ZILValue(String aString, ZILTypeArray aType, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {
		super(aType, aContainer, aSrc);

		ZILType et = aType.getElementType();

		if (!(et instanceof ZILTypeEnum))
			throw new ZamiaException("Internal error: string type expected.");

		ZILTypeEnum elementType = (ZILTypeEnum) et;

		ZILTypeDiscrete idxT = aType.getIndexType();
		boolean ascending = idxT != null ? idxT.isAscending() : true;
		
		int n = aString.length();
		fArrayValue = new ArrayList<ZILValue>(n);
		for (int i = 0; i < n; i++) {
			fArrayValue.add(new ZILValue(aString.charAt(ascending ? i : n - i - 1), elementType, getContainer(), getSrc()));
		}
	}

	public ZILValue(int aLeft, int aRight, boolean aAscending, ZILTypeInteger aType, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {
		super(aType, aContainer, aSrc);
		fLeft = aLeft;
		fRight = aRight;
		fAscending = aAscending;
	}

	public ZILValue(ZILEnumLiteral aLiteral, ZILTypeEnum aType, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aType, aContainer, aSrc);
		if (aLiteral == null) {
			throw new RuntimeException("Internal error.");
		}
		fEnumLiteral = aLiteral;
	}

	public ZILValue(ZILTypeAccess aType, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aType, aContainer, aSrc);
		fPtr = null;
	}

	public ZILValue cloneValue() throws ZamiaException {
		if (fType instanceof ZILTypeEnum) {
			return new ZILValue(fEnumLiteral, (ZILTypeEnum) fType, getContainer(), getSrc());
		} else if (fType instanceof ZILTypeArray) {

			ZILValue v = new ZILValue((ZILTypeArray) fType, getContainer(), getSrc());

			int n = fArrayValue.size();
			for (int i = 0; i < n; i++) {
				v.setValue(i + fArrayOffset, getValue(i + fArrayOffset).cloneValue());
			}
			return v;
		} else if (fType instanceof ZILTypeRecord) {
			ZILValue v = new ZILValue((ZILTypeRecord) fType, getContainer(), getSrc());

			for (ZILRecordField key : fRecordValues.keySet()) {
				v.setValue(key, fRecordValues.get(key));
			}
			return v;
		} else if (fType instanceof ZILTypeEnum) {
			return new ZILValue(getEnumLiteral(), (ZILTypeEnum) fType, getContainer(), getSrc());
			// } else if (type instanceof ZILTypeRange) {
			// return new Value (getLeft(), getRight(), isAscending(),
			// (ZILTypeRange) type);
		} else if (fType instanceof ZILTypeAccess) {
			// FIXME
			throw new ZamiaException("Sorry, not implemented.");
		} else if (fType instanceof ZILTypeFile) {
			// FIXME
			throw new ZamiaException("Sorry, not implemented.");
		} else if (fType instanceof ZILTypeReal) {
			return new ZILValue(getReal(null), (ZILTypeReal) fType, getContainer(), getSrc());
		} else if (fType instanceof ZILTypeInteger) {
			return new ZILValue(getInt((VHDLNode) null), (ZILTypeInteger) fType, getContainer(), getSrc());
		} else if (fType instanceof ZILTypePhysical) {
			return new ZILValue(getReal(null), (ZILTypePhysical) fType, getContainer(), getSrc());
		}
		throw new ZamiaException("Sorry, not implemented.");
	}

	// public ArrayList<ZILValue> getArrayValue() {
	// return fArrayValue;
	// }

	// public void addValue(ZILValue aType) throws ZamiaException {
	// if (!(fType instanceof ZILTypeArray))
	// throw new ZamiaException("Internal error: this is not an array type: " +
	// fType);
	// fArrayValue.add(aType);
	// }

	public int getInt(VHDLNode aSrc) throws ZamiaException {
		if (fType instanceof ZILTypeEnum) {
			return fEnumLiteral.getOrd();
		}

		if (!(fType instanceof ZILTypeInteger))
			throw new ZamiaException("Internal error: this is not an integer type: " + fType);
		return fNum.intValue();
	}

	public long getLong(VHDLNode aSrc) throws ZamiaException {

		if (fType instanceof ZILTypeEnum) {
			return fEnumLiteral.getOrd();
		}

		if (!(fType instanceof ZILTypeInteger))
			throw new ZamiaException("Internal error: this is not an integer type: " + fType, aSrc);

		if (fNum == null) {
			throw new ZamiaException("Internal error: this is an integer type, but num==null => probably a range type (left=" + fLeft + ", right=" + fRight + ")", aSrc);

		}

		return fNum.longValue();
	}

	public BigInteger getNum(VHDLNode aSrc) throws ZamiaException {
		if (!(fType instanceof ZILTypeInteger))
			throw new ZamiaException("Internal error: this is not an integer type: " + fType, aSrc);
		return fNum;
	}

	public BigDecimal getReal(VHDLNode aSrc) throws ZamiaException {

		if (fType instanceof ZILTypeInteger)
			return new BigDecimal(fNum.doubleValue());

		if (!(fType instanceof ZILTypeReal) && !(fType instanceof ZILTypePhysical))
			throw new ZamiaException("Internal error: this is not a real type: " + fType, aSrc);
		return fReal;
	}

	public char getBit() throws ZamiaException {
		// if (!(type instanceof ZILTypeEnum))
		// throw new
		// ZamiaException("Internal error: this is not a bit type: "+type);
		if (!(fEnumLiteral instanceof ZILEnumCharLiteral)) {

			if (fEnumLiteral == null)
				return BIT_U;

			throw new ZamiaException("Internal error: this is not a bit type: " + fType);
		}
		ZILEnumCharLiteral elc = (ZILEnumCharLiteral) fEnumLiteral;
		return elc.getChar();
	}

	public void setNum(int aNum) {
		fNum = new BigInteger("" + aNum);
	}

	@Override
	public String toString() {
		return toHRString() + "@"+fCnt;
	}
	
	public String toHRString() {

		if (fType instanceof ZILTypeArray) {
			StringBuilder buf = new StringBuilder();
			ZILTypeArray at = (ZILTypeArray) fType;
			
			ZILTypeDiscrete idxT = at.getIndexType();
			boolean ascending = true;
			try {
				ascending = idxT != null ? idxT.isAscending() : true;
			} catch (ZamiaException e1) {
				e1.printStackTrace();
			}
			
			int n = fArrayValue.size();
			if (at.isLogic()) {
				// buf.append("\"");
				for (int i = 0; i < n; i++) {
					buf.append(fArrayValue.get(ascending ? i : n-i-1).toHRString());
				}
				// buf.append("\"");

			} else {
				buf.append("(");
				for (int i = 0; i < n; i++) {
					buf.append(fArrayValue.get(i));
					if (i < n - 1)
						buf.append(", ");
				}
				buf.append(")");
			}
			return buf.toString();
		} else if (fType instanceof ZILTypeRecord) {
			StringBuilder buf = new StringBuilder();
			buf.append("(");

			ZILTypeRecord sigTypeRecord = (ZILTypeRecord) fType;

			int n = sigTypeRecord.getNumRecordFields();
			for (int i = 0; i < n; i++) {
				ZILRecordField rf = sigTypeRecord.getRecordField(i);
				buf.append(fRecordValues.get(rf));
				if (i < n - 1)
					buf.append(", ");
			}
			buf.append(")");
			return buf.toString();
		} else if (fType instanceof ZILTypeEnum) {
			return fEnumLiteral.getId();
		} else if (fType instanceof ZILTypeAccess) {
			// FIXME
			return "ACCESS";
		} else if (fType instanceof ZILTypeFile) {
			// FIXME
			return "FILE";
		} else if (fType instanceof ZILTypeReal) {
			return "" + fReal;
		} else if (fType instanceof ZILTypeInteger) {
			return "" + fNum;
		} else if (fType instanceof ZILTypePhysical) {
			// FIXME: unit missing
			return "" + fReal;
		}

		return "***ERR: UNKNOWN VALUE TYPE " + fType;
	}

	public void dump(int indent) {
		logger.debug("%s", toString());
	}

	// public boolean isAllOnes() {
	//
	// if (str == null)
	// return false;
	//		
	// int l = str.length();
	// for (int i = 0; i < l; i++) {
	// if (str.charAt(i) != '1')
	// return false;
	// }
	// return true;
	// }
	// public boolean isAllZeros() {
	//
	// if (str == null)
	// return false;
	//		
	// int l = str.length();
	// for (int i = 0; i < l; i++) {
	// if (str.charAt(i) != '0')
	// return false;
	// }
	// return true;
	// }

	public static int getInt(String aString) {
		return (int) getLong(aString);
	}

	public static long getLong(char aChar) {
		if (aChar == BIT_1)
			return 1l;
		return 0l;
	}

	public static long getLong(String aString) {
		int w = aString.length();
		long l = 0;
		for (int i = 0; i < w; i++) {
			char bit = aString.charAt(i);
			l = l * 2 + getLong(bit);
		}
		return l;
	}

	public static BigInteger getBigInt(String aString) {
		int w = aString.length();
		BigInteger res = new BigInteger("0");
		BigInteger two = new BigInteger("2");
		BigInteger one = new BigInteger("1");
		for (int i = 0; i < w; i++) {
			char bit = aString.charAt(i);

			if (getLong(bit) != 0)
				res = res.multiply(two).add(one);
			else
				res = res.multiply(two);
		}
		return res;
	}

	public static String convert(int aChar, int aWidth) {
		return convert(new BigInteger(String.valueOf(aChar)), aWidth);
	}

	public static String convert(long aNum, int aWidth) {
		return convert(new BigInteger(String.valueOf(aNum)), aWidth);
	}

	// width can be 0 means that we generate the minimum length
	// string that can hold the given constant

	public static String convert(BigInteger aNum, int aWidth) {

		int width = aWidth;
		if (width == 0) {
			width = aNum.bitCount();
		}

		StringBuffer ret = new StringBuffer(aWidth);
		for (int i = aWidth - 1; i >= 0; i--) {
			if (aNum.testBit(i))
				ret.append(BIT_1);
			else
				ret.append(BIT_0);
		}
		// System.out.println ("Converted "+v_+" to "+ret);
		return ret.toString();
	}

	public static String convert(BigInteger aNum) {
		return aNum.toString(2);
	}

	public static char getInverseBit(char aBit) {
		char inv;
		switch (aBit) {
		case BIT_1:
			inv = BIT_0;
			break;
		case BIT_0:
			inv = BIT_1;
			break;
		case BIT_U:
			inv = BIT_U;
			break;
		case BIT_Z:
			inv = BIT_Z;
			break;
		case BIT_X:
		default:
			inv = BIT_X;
			break;
		}
		return inv;
	}

	public ZILValue getInverse(SourceLocation aSrc) throws ZamiaException {

		if (!fType.isLogic()) {
			throw new ZamiaException("Bit / bit string expected.", aSrc);
		}

		if (fType instanceof ZILTypeEnum) {
			return new ZILValue(getInverseBit(getBit()), (ZILTypeEnum) fType, getContainer(), getSrc());
		}

		ZILTypeArray at = (ZILTypeArray) fType;

		ZILValue res = new ZILValue(at, getContainer(), getSrc());

		int len = fArrayValue.size();

		for (int i = 0; i < len; i++) {
			res.setValue(i + fArrayOffset, getValue(i + fArrayOffset).getInverse(aSrc));
		}

		return res;

	}

	public static String toHexString(String aString) {
		long v = 0;
		int l = aString.length();
		// boolean nan = false;
		for (int i = 0; i < l; i++) {
			char c = aString.charAt(i);
			if (c == ZILValue.BIT_1)
				v = v * 2l + 1l;
			else if (c == ZILValue.BIT_0)
				v = v * 2l;
			else {
				// nan = true;
				return aString;
			}
		}
		return "0x" + Long.toHexString(v);
	}

	// public static String convert(String str, ZILType type) {
	//
	// int destWidth = type.getWidth();
	//
	// if (type.isAscending()) {
	// while (str.length() < destWidth)
	// str = "0" + str;
	// } else {
	// while (str.length() < destWidth)
	// str = str + "0";
	// }
	//
	// return str;
	// }

	// public String getStr(SourceLocation location_) throws ZamiaException {
	//
	// if (type.getCategory() != SigCat.VECTOR)
	// throw new ZamiaException("String expected here.", location_);
	//
	// if (type.getElementType(location_).getCategory() != SigCat.BIT)
	// throw new ZamiaException("String expected here.", location_);
	//
	// int n = values.size();
	// StringBuilder buf = new StringBuilder(n);
	// for (int i = 0; i < n; i++) {
	// buf.append(values.get(i).getBit());
	// }
	//
	// return buf.toString();
	// }
	//
	// public boolean isAscending() throws ZamiaException {
	// if (type.getCategory() != SigCat.RANGE)
	// throw new ZamiaException("Internal error: range type expected.");
	// return ascending;
	// }
	//
	// public int getLeft() throws ZamiaException {
	// if (type.getCategory() != SigCat.RANGE)
	// throw new ZamiaException("Internal error: range type expected.");
	// return left;
	// }
	//
	// public int getRight() throws ZamiaException {
	// if (type.getCategory() != SigCat.RANGE)
	// throw new ZamiaException("Internal error: range type expected.");
	// return right;
	// }
	//
	// public Value getInverse() throws ZamiaException {
	// SigCat cat = type.getCategory();
	// if (cat == SigCat.BIT) {
	// return new Value(getInverseBit(bit), type);
	// } else if (cat == SigCat.VECTOR) {
	// ZILType et = type.getElementType(null);
	// if (et.getCategory() != SigCat.BIT)
	// throw new ZamiaException("Internal error: bit string expected.");
	//
	// Value res = new Value(type);
	// int n = values.size();
	// for (int i = 0; i < n; i++) {
	// res.addValue(new Value(getInverseBit(values.get(i).getBit()),
	// ZILType.bit));
	// }
	// return res;
	// }
	// throw new ZamiaException("Internal error: bit string expected.");
	// }

	public void setValue(int aIdx, ZILValue aValue) throws ZamiaException {
		if (!(fType instanceof ZILTypeArray))
			throw new ZamiaException("Internal error");

		fArrayValue.set(aIdx - fArrayOffset, aValue);
	}

	public ZILValue getValue(int aIdx) {
		if (fArrayValue == null)
			return null;
		return fArrayValue.get(aIdx - fArrayOffset);
	}

	public void setValue(ZILRecordField aField, ZILValue aValue) throws ZamiaException {
		if (!(fType instanceof ZILTypeRecord))
			throw new ZamiaException("Internal error");
		fRecordValues.put(aField, aValue);
	}

	public ZILValue getValue(ZILRecordField aField) {
		return fRecordValues.get(aField);
	}

	public static ZILValue getBit(char aBit, VHDLNode aSrc) {
		try {
			return new ZILValue(aBit, ZILType.bit, null, aSrc);
		} catch (ZamiaException e) {
		}

		return null;
	}

	public static ZILValue computeCompare(ZILValue aValueA, ZILValue aValueB, CompareOp aOp, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {

		ZILType t = aValueA.getType();

		if ((t instanceof ZILTypeArray) || (t instanceof ZILTypeEnum) || (t instanceof ZILTypeRecord)) {

			// ZILTypeArray at = (ZILTypeArray) t;

			boolean equal = aValueA.equals(aValueB);

			switch (aOp) {
			case EQUAL:
				if (equal)
					return getBit(BIT_1, aSrc);
				else
					return getBit(BIT_0, aSrc);
			case NEQUAL:
				if (equal)
					return getBit(BIT_0, aSrc);
				else
					return getBit(BIT_1, aSrc);
			}

			throw new ZamiaException("Operation " + aOp + "is not defined on operands of type " + t);

		} else if (t instanceof ZILTypeInteger) {

			BigInteger n1 = aValueA.getNum(aSrc);
			BigInteger n2 = aValueB.getNum(aSrc);

			switch (aOp) {
			case EQUAL:
				if (n1.compareTo(n2) == 0)
					return getBit(BIT_1, aSrc);
				else
					return getBit(BIT_0, aSrc);
			case NEQUAL:
				if (n1.compareTo(n2) != 0)
					return getBit(BIT_1, aSrc);
				else
					return getBit(BIT_0, aSrc);
			case GREATER:
				if (n1.compareTo(n2) > 0)
					return getBit(BIT_1, aSrc);
				else
					return getBit(BIT_0, aSrc);
			case GREATEREQ:
				if (n1.compareTo(n2) >= 0)
					return getBit(BIT_1, aSrc);
				else
					return getBit(BIT_0, aSrc);
			case LESS:
				if (n1.compareTo(n2) < 0)
					return getBit(BIT_1, aSrc);
				else
					return getBit(BIT_0, aSrc);
			case LESSEQ:
				if (n1.compareTo(n2) <= 0)
					return getBit(BIT_1, aSrc);
				else
					return getBit(BIT_0, aSrc);
			}

		} else if ((t instanceof ZILTypeReal) || (t instanceof ZILTypePhysical)) {
			BigDecimal n1 = aValueA.getReal(aSrc);
			BigDecimal n2 = aValueB.getReal(aSrc);

			switch (aOp) {
			case EQUAL:
				if (n1.compareTo(n2) == 0)
					return getBit(BIT_1, aSrc);
				else
					return getBit(BIT_0, aSrc);
			case NEQUAL:
				if (n1.compareTo(n2) != 0)
					return getBit(BIT_1, aSrc);
				else
					return getBit(BIT_0, aSrc);
			case GREATER:
				if (n1.compareTo(n2) > 0)
					return getBit(BIT_1, aSrc);
				else
					return getBit(BIT_0, aSrc);
			case GREATEREQ:
				if (n1.compareTo(n2) >= 0)
					return getBit(BIT_1, aSrc);
				else
					return getBit(BIT_0, aSrc);
			case LESS:
				if (n1.compareTo(n2) < 0)
					return getBit(BIT_1, aSrc);
				else
					return getBit(BIT_0, aSrc);
			case LESSEQ:
				if (n1.compareTo(n2) <= 0)
					return getBit(BIT_1, aSrc);
				else
					return getBit(BIT_0, aSrc);
			}

		} else
			throw new ZamiaException("Internal error: Don't know how to compute compare value for type " + t, aSrc);

		return null;
	}

	private static long getLong(ZILValue aValue, int aWidth) throws ZamiaException {

		long l = 0;
		for (int i = 0; i < aWidth; i++) {
			char bit = aValue.getValue(i).getBit();
			l = l * 2 + ZILValue.getLong(bit);
		}
		return l;
	}

	public static ZILValue computeMath(ZILValue va, ZILValue vb, MathOp op, VHDLNode aSrc) throws ZamiaException {

		ZILType t = va.getType();
		ZILValue resValue = null;
		if (t instanceof ZILTypeArray) {

			ZILTypeArray ta = (ZILTypeArray) t;

			int w = (int) ta.getIndexType().getCardinality();

			long la = getLong(va, w);
			long lb = vb != null ? getLong(vb, w) : 0;
			long res = 0;

			switch (op) {
			case ABS:
				res = Math.abs(la);
				break;
			case ADD:
				res = la + lb;
				break;
			case DIV:
				res = la / lb;
				break;
			case MOD:
				res = la % lb;
				break;
			case MUL:
				res = la * lb;
				break;
			case NEG:
				res = -la;
				break;
			case POWER:
				res = (long) Math.pow(la, lb);
				break;
			case REM:
				res = la % lb; // FIXME
				break;
			case SUB:
				res = la - lb;
				break;
			}

			String s = ZILValue.convert(res, (int) ta.getIndexType().getCardinality());
			resValue = new ZILValue(s, ta, null, null);
		} else if (t instanceof ZILTypeReal) {
			ZILTypeReal resType = (ZILTypeReal) t;

			// System.out.println("executing:   MATH OPS: " + op1 + ", " + op2);

			BigDecimal b = new BigDecimal(0), a = va.getReal(aSrc);

			if (vb != null)
				b = vb.getReal(aSrc);

			BigDecimal res = new BigDecimal(0);

			switch (op) {
			case NEG:
				res = a.negate();
				break;
			case ADD:
				res = a.add(b);
				break;
			case SUB:
				res = a.subtract(b);
				break;
			case MUL:
				res = a.multiply(b);
				break;
			case DIV:
				res = a.divide(b);
				break;
			case MOD:
				if (b.compareTo(new BigDecimal(0)) < 0)
					res = a.remainder(b).abs().negate();
				else
					res = a.remainder(b).abs();
				break;
			case REM:
				res = a.remainder(b);
				break;
			case POWER:
				res = a.pow(b.intValue()); // FIXME
				break;
			case ABS:
				res = a.abs();
				break;
			default:
				throw new ZamiaException("Unknown math op: " + op);
			}

			resValue = new ZILValue(res, resType, null, aSrc);

		} else if (t instanceof ZILTypeInteger) {

			ZILTypeInteger resType = (ZILTypeInteger) t;

			// System.out.println("executing:   MATH OPS: " + op1 + ", " + op2);

			long b = 0, a = va.getLong(aSrc);

			if (vb != null)
				b = vb.getLong(aSrc);

			long res = 0;

			switch (op) {
			case NEG:
				res = -a;
				break;
			case ADD:
				res = a + b;
				break;
			case SUB:
				res = a - b;
				break;
			case MUL:
				res = a * b;
				break;
			case DIV:
				res = a / b;
				break;
			case MOD:
				if (b < 0)
					res = -Math.abs(a % b);
				else
					res = Math.abs(a % b);
				break;
			case REM:
				res = a % b;
				break;
			case POWER:
				res = Math.round(Math.pow(a, b));
				break;
			case ABS:
				res = Math.abs(a);
				break;
			default:
				throw new ZamiaException("Unknown math op: " + op);
			}

			resValue = new ZILValue(res, resType, null, aSrc);

		} else {
			// FIXME
			throw new ZamiaException("Sorry, only bit-vector, real and integer math operations implemented yet.");

		}
		return resValue;
	}

	public static ZILValue computeLogic(ZILValue aValueA, ZILValue aValueB, LogicOp aOp, VHDLNode aSrc) throws ZamiaException {

		ZILType t = aValueA.getType();

		if (t.isBit()) {

			char b1 = aValueA.getBit();
			char b2 = BIT_X;
			if (aValueB != null)
				b2 = aValueB.getBit();

			char res = BIT_X;

			switch (aOp) {
			case NOT:
				res = getInverseBit(b1);
				break;
			case AND:
				switch (b1) {
				case BIT_1:
					res = b2;
					break;
				case BIT_0:
					res = BIT_0;
					break;
				default:
					res = b1;
				}
				break;
			case BUF:
				res = b1;
				break;
			case NAND:
				switch (b1) {
				case BIT_1:
					res = getInverseBit(b2);
					break;
				case BIT_0:
					res = BIT_1;
					break;
				default:
					res = b1;
				}
				break;
			case OR:
				switch (b1) {
				case BIT_1:
					res = BIT_1;
					break;
				case BIT_0:
					res = b2;
					break;
				default:
					res = b1;
				}
				break;
			case NOR:
				switch (b1) {
				case BIT_1:
					res = BIT_0;
					break;
				case BIT_0:
					res = getInverseBit(b2);
					break;
				default:
					res = b1;
				}
				break;
			case XOR:
				switch (b1) {
				case BIT_1:
					res = getInverseBit(b2);
					break;
				case BIT_0:
					res = b2;
					break;
				default:
					res = b1;
				}
				break;
			case XNOR:
				switch (b1) {
				case BIT_1:
					res = b2;
					break;
				case BIT_0:
					res = getInverseBit(b2);
					break;
				default:
					res = b1;
				}
				break;
			}

			return getBit(res, aSrc);
		} else if (t instanceof ZILTypeArray) {

			ZILTypeArray at = (ZILTypeArray) t;

			ZILValue res = new ZILValue(at, null, aValueA.getSrc());

			ZILTypeDiscrete idxType = at.getIndexType();

			int n = (int) idxType.getCardinality();
			int offset = idxType.getLow().getInt(aSrc);
			for (int i = 0; i < n; i++) {

				ZILValue va = aValueA.getValue(i);
				ZILValue vb = null;
				if (aValueB != null) {
					vb = aValueB.getValue(i);
				}

				res.setValue(i + offset, computeLogic(va, vb, aOp, aSrc));
			}

			return res;
		} else
			throw new ZamiaException("Internal error: Logic operation not defined for this type: " + t, aSrc.getLocation());

	}

	public static ZILValue generateUValue(ZILType aType, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {
		if (aType.isBit()) {

			ZILTypeEnum et = (ZILTypeEnum) aType;

			char firstLiteral = et.getEnumLiteral(0, aSrc).getChar(); // works for bit
			// (=>0) and
			// std_logic
			// (=>U)

			return getBit(firstLiteral, aSrc);
		} else if (aType instanceof ZILTypeArray) {

			ZILTypeArray at = (ZILTypeArray) aType;

			ZILValue res = new ZILValue(at, aContainer, aSrc);

			ZILType elementType = at.getElementType();

			ZILValue o = generateUValue(elementType, aContainer, aSrc);

			ZILTypeDiscrete idxType = at.getIndexType();

			int n = (int) idxType.getCardinality();
			int offset = idxType.getLow().getInt(aSrc);
			for (int i = 0; i < n; i++) {
				res.setValue(i + offset, o.cloneValue());
			}

			return res;
		} else if (aType instanceof ZILTypeInteger) {

			ZILTypeInteger intType = (ZILTypeInteger) aType;
			return new ZILValue(0, intType, aContainer, aSrc);

		} else if (aType instanceof ZILTypeReal) {

			ZILTypeReal realType = (ZILTypeReal) aType;
			return new ZILValue(0, realType, aContainer, aSrc);

			// } else if (t_ instanceof ZILTypePhysical) {
			// return getBit(bit_);
			// } else if (t_ instanceof ZILTypeEnum) {
			// return getBit(bit_);
		} else if (aType instanceof ZILTypeRecord) {

			ZILTypeRecord rt = (ZILTypeRecord) aType;

			ZILValue res = new ZILValue(rt, aContainer, aSrc);

			int n = rt.getNumRecordFields();
			for (int i = 0; i < n; i++) {
				ZILRecordField rf = rt.getRecordField(i);

				ZILType elementType = rf.type;

				ZILValue o = generateUValue(elementType, aContainer, aSrc);

				res.setValue(rf, o);
			}

			return res;

		} else
			throw new ZamiaException("Internal error: Don't know how to generate constant value for " + aType, aSrc);
	}

	public static ZILValue generateValue(char aBit, ZILType aType, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {

		if (aType.isBit()) {
			return getBit(aBit, aSrc);
		} else if (aType instanceof ZILTypeArray) {

			ZILTypeArray at = (ZILTypeArray) aType;

			ZILValue res = new ZILValue(at, aContainer, aSrc);

			ZILType elementType = at.getElementType();

			ZILValue o = generateValue(aBit, elementType, aContainer, aSrc);

			ZILTypeDiscrete idxType = at.getIndexType();

			int n = (int) idxType.getCardinality();
			int offset = idxType.getLow().getInt(aSrc);
			for (int i = 0; i < n; i++) {
				res.setValue(i + offset, o.cloneValue());
			}

			return res;
			// } else if (t_ instanceof ZILTypeInteger) {
			// return getBit(bit_);
			// } else if (t_ instanceof ZILTypeReal) {
			// return getBit(bit_);
			// } else if (t_ instanceof ZILTypePhysical) {
			// return getBit(bit_);
			// } else if (t_ instanceof ZILTypeEnum) {
			// return getBit(bit_);
		} else if (aType instanceof ZILTypeRecord) {

			ZILTypeRecord rt = (ZILTypeRecord) aType;

			ZILValue res = new ZILValue(rt, aContainer, aSrc);

			int n = rt.getNumRecordFields();
			for (int i = 0; i < n; i++) {
				ZILRecordField rf = rt.getRecordField(i);

				ZILType elementType = rf.type;

				ZILValue o = generateValue(aBit, elementType, aContainer, aSrc);

				res.setValue(rf, o);
			}

			return res;

		} else
			throw new ZamiaException("Internal error: Don't know how to generate constant value for " + aType, aSrc);
	}

	// used by the simulator
	public static ZILValue generateZ(ZILType aType, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {

		if (aType.isBit()) {
			
			ZILTypeEnum et = (ZILTypeEnum) aType;
			
			ZILEnumLiteral literal = et.findEnumLiteral(BIT_Z);
			if (literal == null) {
				return et.getLow();
			}
			
			return new ZILValue (literal, et, aContainer, aSrc);
			
		} else if (aType instanceof ZILTypeArray) {

			ZILTypeArray at = (ZILTypeArray) aType;

			ZILValue res = new ZILValue(at, aContainer, aSrc);

			ZILType elementType = at.getElementType();

			ZILValue o = generateZ(elementType, aContainer, aSrc);

			ZILTypeDiscrete idxType = at.getIndexType();
			if (idxType != null) {

				int offset = idxType.getLow().getInt(aSrc);

				int n = (int) idxType.getCardinality();

				for (int i = 0; i < n; i++) {
					res.setValue(i + offset, o.cloneValue());
				}
			}
			return res;
		} else if (aType instanceof ZILTypeInteger) {
			return new ZILValue(0, (ZILTypeInteger) aType, aContainer, aSrc);
		} else if (aType instanceof ZILTypeReal) {
			return new ZILValue(0.0, (ZILTypeReal) aType, aContainer, aSrc);
		} else if (aType instanceof ZILTypePhysical) {
			return new ZILValue(0.0, (ZILTypePhysical) aType, aContainer, aSrc);
		} else if (aType instanceof ZILTypeEnum) {
			ZILTypeEnum te = (ZILTypeEnum) aType;
			return new ZILValue(te.getEnumLiteral(0, aSrc), te, aContainer, aSrc);
		} else if (aType instanceof ZILTypeRecord) {

			ZILTypeRecord rt = (ZILTypeRecord) aType;

			ZILValue res = new ZILValue(rt, aContainer, aSrc);

			int n = rt.getNumRecordFields();
			for (int i = 0; i < n; i++) {
				ZILRecordField rf = rt.getRecordField(i);

				ZILType elementType = rf.type;

				ZILValue o = generateZ(elementType, aContainer, aSrc);

				res.setValue(rf, o);
			}

			return res;
		} else if (aType instanceof ZILTypeAccess) {

			ZILTypeAccess rt = (ZILTypeAccess) aType;

			ZILValue res = new ZILValue(rt, aContainer, aSrc);

			return res;

		} else
			throw new ZamiaException("Internal error: Don't know how to generate constant value for " + aType);
	}

	public String getString() throws ZamiaException {

		if (!fType.isLogic())
			throw new ZamiaException("Internal error: this is not a logic typed value.");

		return toString();
	}

	public int getNumValues() {
		return fArrayValue.size();
	}

	public boolean isAscending() throws ZamiaException {
		if (!(fType instanceof ZILTypeDiscrete))
			throw new ZamiaException("Internal error");
		return fAscending;
	}

	public int getLeft() throws ZamiaException {
		if (!(fType instanceof ZILTypeDiscrete))
			throw new ZamiaException("Internal error");

		return fLeft;
	}

	public int getRight() throws ZamiaException {
		if (!(fType instanceof ZILTypeDiscrete))
			throw new ZamiaException("Internal error");

		return fRight;
	}

	public ZILValue merge(ZILValue aValue) throws ZamiaException {
		ZILType zilType = getType();

		if (zilType.isBit()) {

			char b1 = getBit();
			char b2 = aValue.getBit();

			switch (b1) {

			case BIT_0:

				if (b2 == BIT_0 || b2 == BIT_Z)
					return this;

				return getBit(BIT_X, getSrc());

			case BIT_1:

				if (b2 == BIT_1 || b2 == BIT_Z)
					return this;

				return getBit(BIT_X, getSrc());

			case BIT_U:
				if (b2 == BIT_U)
					return this;
				return getBit(BIT_X, getSrc());

			case BIT_Z:
				return aValue;

			case BIT_X:
				return this;

			default:
				return getBit(BIT_X, getSrc());

			}

		} else if (zilType instanceof ZILTypeArray) {

			ZILTypeArray at = (ZILTypeArray) zilType;

			ZILValue res = new ZILValue(at, getContainer(), getSrc());

			ZILTypeDiscrete idxType = at.getIndexType();

			int n = (int) idxType.getCardinality();
			int offset = idxType.getLow().getInt(getSrc());
			for (int i = 0; i < n; i++) {
				res.setValue(i + offset, getValue(i + offset).merge(aValue.getValue(i + offset)));
			}

			return res;
		} else if (zilType instanceof ZILTypeInteger) {
			return this; // FIXME
		} else if (zilType instanceof ZILTypeReal) {
			return this; // FIXME
		} else if (zilType instanceof ZILTypePhysical) {
			return this; // FIXME
		} else if (zilType instanceof ZILTypeEnum) {
			return this; // FIXME
		} else if (zilType instanceof ZILTypeRecord) {

			ZILTypeRecord rt = (ZILTypeRecord) zilType;

			ZILValue res = new ZILValue(rt, getContainer(), getSrc());

			int n = rt.getNumRecordFields();
			for (int i = 0; i < n; i++) {
				ZILRecordField rf = rt.getRecordField(i);

				res.setValue(rf, getValue(rf).merge(aValue.getValue(rf)));
			}

			return res;

		} else
			throw new ZamiaException("Internal error: Don't know how to merge value for type " + zilType);
	}

	public ZILValue not() throws ZamiaException {
		ZILType type = getType();

		if (type.isBit()) {

			char b1 = getBit();

			switch (b1) {

			case BIT_0:

				return getBit(BIT_1, getSrc());

			case BIT_1:

				return getBit(BIT_0, getSrc());

			default:
				return getBit(BIT_X, getSrc());

			}

		} else if (type instanceof ZILTypeArray) {

			ZILTypeArray at = (ZILTypeArray) type;

			ZILValue res = new ZILValue(at, getContainer(), getSrc());

			ZILTypeDiscrete idxType = at.getIndexType();

			int n = (int) idxType.getCardinality();
			int offset = idxType.getLow().getInt(getSrc());
			for (int i = 0; i < n; i++) {
				res.setValue(i + offset, getValue(i + offset).not());
			}

			return res;
		} else
			throw new ZamiaException("Error: NOT operation not defined on type " + type);
	}

	@Override
	public int hashCode() {
		assert false : "hashCode not designed";
		return 42;
	}

	@Override
	public boolean equals(Object aObj) {

		try {
			if (!(aObj instanceof ZILValue))
				return false;

			if (this == aObj)
				return true;

			ZILValue v = (ZILValue) aObj;

			ZILType t = v.getType();
			if (!getType().isCompatible(t))
				return false;

			if (t instanceof ZILTypeArray) {

				ZILTypeArray at = (ZILTypeArray) t;
				
				ZILTypeDiscrete idxType = at.getIndexType();
				if (idxType == null)
					return false;

				int n = (int) idxType.getCardinality();
				for (int i = 0; i < n; i++) {

					ZILValue v1 = v.getValue(i);
					ZILValue v2 = getValue(i);

					if (v1 == null || v2 == null || !v1.equals(v2))
						return false;
				}

				return true;
			} else if (t instanceof ZILTypeInteger) {
				
				BigInteger n1 = v.getNum(null);
				BigInteger n2 = getNum(null);
				
				return n1 != null && n2 != null && n1.equals(n2);
			} else if (t instanceof ZILTypeReal) {
				
				BigDecimal b1 = v.getReal(null);
				BigDecimal b2 = getReal(null);
				
				return b1 != null && b2 != null && b1.equals(b2);
			} else if (t instanceof ZILTypePhysical) {
				
				BigDecimal b1 = v.getReal(null);
				BigDecimal b2 = getReal(null);
				
				return b1 != null && b2 != null && b1.equals(b2);
			} else if (t instanceof ZILTypeEnum) {
				ZILEnumLiteral el1 = getEnumLiteral();
				ZILEnumLiteral el2 = v.getEnumLiteral();
				return el1 != null && el2 != null && el1.equals(el2);
			} else if (t instanceof ZILTypeRecord) {

				ZILTypeRecord rt = (ZILTypeRecord) t;

				int n = rt.getNumRecordFields();
				for (int i = 0; i < n; i++) {
					ZILRecordField rf = rt.getRecordField(i);

					ZILValue v1 = v.getValue(rf);
					ZILValue v2 = getValue(rf);

					if (!v1.equals(v2))
						return false;

				}

				return true;

			} else
				throw new ZamiaException("Internal error: Don't know how to compare values of type " + t);
		} catch (ZamiaException e) {
			System.out.println("Exception caught: " + e);
			e.printStackTrace();
		}
		return false;
	}

	public ZILEnumLiteral getEnumLiteral() {
		return fEnumLiteral;
	}

	public void setType(ZILType aType) {
		fType = aType;
	}

	public void modifyValue(ZILValue aValue) throws ZamiaException {

		ZILType sType = aValue.getType();
		if (!sType.getClass().isAssignableFrom(fType.getClass())) {
			throw new ZamiaException("Type mismatch.");
		}

		if (fType instanceof ZILTypeArray) {

			int n = fArrayValue.size();
			for (int i = 0; i < n; i++) {
				setValue(i, aValue.getValue(i));
			}
		} else if (fType instanceof ZILTypeRecord) {

			ZILTypeRecord rt = (ZILTypeRecord) fType;

			int n = rt.getNumRecordFields();
			for (int i = 0; i < n; i++) {
				ZILRecordField rf = rt.getRecordField(i);
				setValue(rf, aValue.getValue(rf));
			}

		} else if (fType instanceof ZILTypeEnum) {

			fEnumLiteral = aValue.getEnumLiteral();

			// } else if (type instanceof ZILTypeRange) {
			// return new Value (getLeft(), getRight(), isAscending(),
			// (ZILTypeRange) type);
		} else if (fType instanceof ZILTypeAccess) {
			// FIXME
			throw new ZamiaException("Sorry, not implemented.");
		} else if (fType instanceof ZILTypeFile) {
			// FIXME
			throw new ZamiaException("Sorry, not implemented.");
		} else if (fType instanceof ZILTypeReal) {
			fReal = aValue.getReal(null);
		} else if (fType instanceof ZILTypeInteger) {
			fNum = aValue.getNum(null);
		} else if (fType instanceof ZILTypePhysical) {
			fReal = aValue.getReal(null);
		} else
			throw new ZamiaException("Sorry, not implemented.");
	}

	public boolean isRange() {
		return (fType instanceof ZILTypeDiscrete) && (fNum == null);
	}

	public boolean isLogicOne() {
		if (!(fEnumLiteral instanceof ZILEnumCharLiteral)) {
			return false;
		}
		ZILEnumCharLiteral elc = (ZILEnumCharLiteral) fEnumLiteral;
		char c = elc.getChar();
		return c == BIT_1;
	}

	public boolean isAllOnes() throws ZamiaException {
		if (fType instanceof ZILTypeArray) {

			ZILTypeArray at = (ZILTypeArray) fType;

			int n = (int) at.getIndexType().getCardinality();
			for (int i = 0; i < n; i++) {

				ZILValue v = getValue(i);

				if (!v.isAllOnes())
					return false;
			}

			return true;
		} else if (fType instanceof ZILTypeInteger) {
			return false;
		} else if (fType instanceof ZILTypeReal) {
			return false;
		} else if (fType instanceof ZILTypePhysical) {
			return false;
		} else if (fType instanceof ZILTypeEnum) {
			return isLogicOne();
		} else if (fType instanceof ZILTypeRecord) {

			ZILTypeRecord rt = (ZILTypeRecord) fType;

			int n = rt.getNumRecordFields();
			for (int i = 0; i < n; i++) {
				ZILRecordField rf = rt.getRecordField(i);

				ZILValue v = getValue(rf);

				if (!v.isAllOnes())
					return false;

			}
			return true;
		}
		return false;
	}

	// public Range getRange() throws ZamiaException {
	// // FIXME: this doesn't feel right
	// return new Range (new OperationLiteral(getLeft(),null,0), new
	// OperationLiteral(getRight(), null, 0), ascending, null, 0);
	// }

	@Override
	public void generateCode(ZILInterpreterCode aCode, RTLCache aCache) throws ZamiaException {
		aCode.add(new ZILPushStmt(this, getSrc()));

	}

	@Override
	public boolean isConstant() throws ZamiaException {
		return true;
	}

	@Override
	public ZILValue computeConstant() throws ZamiaException {
		return this;
	}

	@Override
	protected void doElaborate(RTLSignal aResult, Bindings aLastBindings, RTLCache aCache) throws ZamiaException {

		RTLGraph rtlg = aCache.getGraph();

		RTLLiteral module = new RTLLiteral(this, rtlg, null, getSrc());

		rtlg.add(module);

		RTLPort pz = module.getZ();
		pz.setSignal(aResult);
	}

	@Override
	public ZILClock getClock() throws ZamiaException {
		return null;
	}

	@Override
	public ZILOperation inlineSubprograms(VariableRemapping aVR, ZILSequenceOfStatements aSOS, RTLCache aCache) throws ZamiaException {
		return this;
	}

	@Override
	public boolean isSynthesizable() throws ZamiaException {
		return true;
	}

	@Override
	public ZILOperation resolveVariables(Bindings aVBS, ZILSequenceOfStatements aSOS, RTLCache aCache) throws ZamiaException {
		return this;
	}

	@Override
	public int getNumOperands() {
		return 0;
	}

	@Override
	public ZILOperation getOperand(int aIdx) {
		return null;
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
	}

	public boolean isValidTarget() {
		return false;
	}

	public void generateInterpreterCodeRef(boolean aIsInertial, boolean aHaveDelay, boolean aHaveReject, ZILInterpreterCode aCode, RTLCache aCache) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException ("Sorry, not implemented yet.");
	}

	public ZILValue getNext(VHDLNode aSrc) throws ZamiaException {
		
		if (fEnumLiteral != null) {
			
			int ord = fEnumLiteral.getOrd();
			
			ZILTypeEnum type = (ZILTypeEnum) fType;
			
			ZILEnumLiteral l = type.getEnumLiteral(ord+1, aSrc);
			
			return l.getValue();
		}
		
		if (fType instanceof ZILTypeInteger) {

			ZILTypeInteger iType = (ZILTypeInteger) fType;
			
			return new ZILValue (fNum.add(new BigInteger("1")), iType, getContainer(), aSrc);
		}

		throw new ZamiaException ("This is not a discrete type: "+this, aSrc);
	}

	public ZILValue getPrev(VHDLNode aSrc) throws ZamiaException {
		
		if (fEnumLiteral != null) {
			
			int ord = fEnumLiteral.getOrd();
			
			ZILTypeEnum type = (ZILTypeEnum) fType;
			
			ZILEnumLiteral l = type.getEnumLiteral(ord-1, aSrc);
			
			return l.getValue();
		}
		
		if (fType instanceof ZILTypeInteger) {

			ZILTypeInteger iType = (ZILTypeInteger) fType;
			
			return new ZILValue (fNum.subtract(new BigInteger("1")), iType, getContainer(), aSrc);
		}

		throw new ZamiaException ("This is not a discrete type: "+this, aSrc);
	}

	public long toLongNumber() throws ZamiaException {
		long v = 0;
		
		String str = toHRString();
		
		int l = str.length();
		// boolean nan = false;
		for (int i = 0; i < l; i++) {
			char c = str.charAt(i);
			if (c == ZILValue.BIT_1)
				v = v * 2l + 1l;
			else if (c == ZILValue.BIT_0)
				v = v * 2l;
			else {
				throw new ZamiaException ("Bit string literal expected here.", getSrc());
			}
		}
		
		return v;
	}
	
	public String toHexString() {

		if (!getType().isLogic()) {
			return toHRString();
		}
		
		try {
			long l = toLongNumber();
			return "X\"" + Long.toHexString(l)+"\"";
		} catch (ZamiaException e) {
			return toHRString();
		}
	}

	public String toDecString() {

		if (!getType().isLogic()) {
			return toHRString();
		}
		
		try {
			long l = toLongNumber();
			return "" + l;
		} catch (ZamiaException e) {
			return toHRString();
		}
	}
	
	public String toOctString() {

		if (!getType().isLogic()) {
			return toHRString();
		}
		
		try {
			long l = toLongNumber();
			return "O\"" + Long.toOctalString(l)+"\"";
		} catch (ZamiaException e) {
			return toHRString();
		}
	}

	public String toBinString(boolean aAscending) {

		if (!getType().isLogic()) {
			return toHRString();
		}

		if (fType instanceof ZILTypeArray) {
			StringBuilder buf = new StringBuilder("B\"");
			ZILTypeArray at = (ZILTypeArray) fType;

			// FIXME: use type information from DB
//			ZILTypeDiscrete idxT = at.getIndexType();
//			boolean ascending = true;
//			try {
//				ascending = idxT != null ? idxT.isAscending() : true;
//			} catch (ZamiaException e1) {
//				e1.printStackTrace();
//			}
			
			boolean ascending = aAscending;
			
			int n = fArrayValue.size();
			if (at.isLogic()) {
				
				for (int i = 0; i < n; i++) {
					buf.append(fArrayValue.get(ascending ? i : n-i-1).toHRString());
				}
				buf.append("\"");

			} else {
				buf.append("(");
				for (int i = 0; i < n; i++) {
					buf.append(fArrayValue.get(i));
					if (i < n - 1)
						buf.append(", ");
				}
				buf.append(")");
			}
			return buf.toString();
		} else if (fType instanceof ZILTypeEnum) {
			return fEnumLiteral.getId();
		}

		return "***ERR: UNKNOWN VALUE TYPE " + fType;

	}

}
