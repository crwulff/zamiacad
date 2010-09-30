/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.vhdl.ast;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import org.zamia.ErrorReport;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationBinary;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGOperationLiteral;
import org.zamia.instgraph.IGOperationPhi;
import org.zamia.instgraph.IGRange;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGOperationBinary.BinOp;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.zdb.ZDB;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class OperationLiteral extends Operation {

	public enum LiteralCat {
		BIT_STRING /* includes base */, STRING, ENUM, NULL, DECIMAL, BASED, CHAR, PHYSICAL, RANGE
	}

	static class ParseResult {
		public BigInteger bigInt = null;

		public BigDecimal realValue = BigDecimal.ZERO;
	}

	private LiteralCat cat;

	private String image;

	private Name unitN;

	// private Range range;

	public OperationLiteral(String value_, LiteralCat category_, ASTObject parent_, long location_) throws ZamiaException {
		super(parent_, location_);
		setImage(value_, category_);
	}

	// convenience
	public OperationLiteral(int value_, ASTObject parent_, long location_) throws ZamiaException {
		super(parent_, location_);
		cat = LiteralCat.DECIMAL;
		image = "" + value_;
	}

	// convenience
	public OperationLiteral(double value_, ASTObject parent_, long location_) throws ZamiaException {
		super(parent_, location_);
		image = "" + value_;
		cat = LiteralCat.DECIMAL;
	}

	public OperationLiteral(OperationLiteral value_, Name name_, ASTObject parent_, long location_) throws ZamiaException {
		super(parent_, location_);
		if (value_ != null) {
			value_.setParent(this);
			image = value_.getImage();
		}
		cat = LiteralCat.PHYSICAL;
		setUnitName(name_);
	}

	public void setUnitName(Name name_) {
		unitN = name_;
		if (unitN != null)
			unitN.setParent(this);
	}

	public void setImage(String image_, LiteralCat category_) throws ZamiaException {
		cat = category_;
		if (image_ != null) {
			image = image_;
		} else
			image = null;
	}

	public String getImage() {
		return image;
	}

	public Name getUnit() {
		return unitN;
	}

	public LiteralCat getCategory() {
		return cat;
	}

	private ParseResult parseAbstractLiteral() throws ZamiaException {

		ParseResult pr = new ParseResult();

		// remove underscores, figure out type of literal
		int l = image.length();
		StringBuffer buf = new StringBuffer(l);

		int leftHash = -1;
		int rightHash = -1;
		boolean decimal = false;
		int j = 0;
		for (int i = 0; i < l; i++) {
			char c = image.charAt(i);
			switch (c) {
			case '_':
				break;
			case '#':
				if (leftHash < 0)
					leftHash = j;
				else
					rightHash = j;
				buf.append(c);
				j++;
				break;
			case '.':
				decimal = true;
				buf.append(c);
				j++;
				break;
			default:
				buf.append(c);
				j++;
			}
		}

		int base = 10;
		int exponent = 0;

		l = buf.length();
		// Mantisse:
		int mStart = 0;
		int mEnd = l - 1;

		if (leftHash >= 0) {

			base = Integer.parseInt(buf.substring(0, leftHash));

			if (l > (rightHash + 1)) {
				if (Character.toUpperCase(buf.charAt(rightHash + 1)) != 'E')
					throw new ZamiaException("Malformed literal. Base exponent expected follow right hash sign.", this);

				exponent = Integer.parseInt(buf.substring(rightHash + 2, l));
			}

			mStart = leftHash + 1;
			mEnd = rightHash - 1;

		} else {
			// extension?

			int exppos = -1;
			for (int i = 0; i < l; i++) {
				if (Character.toUpperCase(buf.charAt(i)) == 'E') {
					exppos = i;
				}
			}

			if (exppos >= 0) {
				String expStr = null;
				if (buf.charAt(exppos + 1) == '+') {
					expStr = buf.substring(exppos + 2, l);
				} else {
					expStr = buf.substring(exppos + 1, l);
				}
				try {
					exponent = Integer.parseInt(expStr);
				} catch (NumberFormatException e) {
					throw new ZamiaException("Trouble parsing exponent: " + expStr + e, this);
				}
				mEnd = exppos - 1;
			}
		}

		if (decimal) {

			pr.bigInt = null;
			String rvStr = buf.substring(mStart, mEnd + 1);
			try {
				pr.realValue = new BigDecimal(Double.parseDouble(rvStr)).multiply(new BigDecimal(Math.pow(base, exponent)));
			} catch (NumberFormatException e) {
				throw new ZamiaException("Trouble parsing real: " + rvStr + " : " + e, this);
			}

		} else {
			BigInteger b = new BigInteger(String.valueOf(base));
			BigInteger e = b.pow(exponent);

			pr.bigInt = (new BigInteger(buf.substring(mStart, mEnd + 1), base)).multiply(e);
			pr.realValue = new BigDecimal(pr.bigInt.doubleValue());
		}

		return pr;
	}

	private String parseBitStringLiteral() throws ZamiaException {

		int bitsPerChar = 0;
		switch (Character.toUpperCase(image.charAt(0))) {
		case 'O':
			bitsPerChar = 3;
			break;
		case 'X':
			bitsPerChar = 4;
			break;
		case 'B':
			bitsPerChar = 1;
			break;
		default:
			throw new ZamiaException("Illegal base in bit string literal: " + image.charAt(0), this);
		}

		// remove underscores and "
		int l = image.length();
		StringBuffer buf = new StringBuffer(l);

		for (int i = 2; i < l; i++) {
			char c = image.charAt(i);

			if ((c != '"') && (c != '_')) {
				int n = c - '0';
				buf.append(ZILValue.convert(n, bitsPerChar));
			}
		}

		return buf.toString();
	}

	public String toString() {
		return unitN != null ? image + " " + unitN : image;
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	@Override
	public ASTObject getChild(int idx_) {
		return unitN;
	}

	@Override
	public String toVHDL() {
		switch (cat) {
		case STRING:
			return '"' + image + '"';
		case CHAR:
			return "'" + image + "'";
		case PHYSICAL:
			return image + " " + unitN.toVHDL();
		}
		return image;
	}

	public void setValue(String value_) {
		image = value_;
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
	}

	@Override
	protected ArrayList<IGOperation> computeIGP(IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ASTErrorMode aErrorMode,
			ErrorReport aReport) throws ZamiaException {

		ArrayList<IGOperation> res = new ArrayList<IGOperation>(1);

		IGInterpreterRuntimeEnv env = aEE.getInterpreterEnv();
		ZDB zdb = aEE.getZDB();

		IGOperation value = null;

		switch (cat) {
		case BIT_STRING:

			if (aTypeHint == null) {
				reportError("Need type hint for bit string literal.", this, aErrorMode, aReport);
				return res;
			}
			if (!aTypeHint.isArray()) {
				reportError("Array type hint expected.", this, aErrorMode, aReport);
				return res;
			}

			int bitsPerDigit = 0;
			switch (Character.toUpperCase(image.charAt(0))) {
			case 'O':
				bitsPerDigit = 3;
				break;
			case 'X':
				bitsPerDigit = 4;
				break;
			case 'B':
				bitsPerDigit = 1;
				break;
			default:
				reportError("Illegal base in bit string literal: " + image.charAt(0), this, aErrorMode, aReport);
				return res;
			}

			// remove underscores and "
			int numBits = 0;
			int l = image.length();
			for (int i = 2; i < l; i++) {
				char c = image.charAt(i);

				if ((c != '"') && (c != '_'))
					numBits += bitsPerDigit;
			}

			IGType it = aTypeHint.getIndexType();

			IGOperation ascending = it.getAscending(aContainer, getLocation());
			IGOperation left = it.getLeft(getLocation());
			IGOperation length = it.getDiscreteValue(numBits - 1, getLocation(), aErrorMode, aReport);
			if (length == null) {
				return null;
			}

			IGOperation right;
			
			if (ascending instanceof IGStaticValue) {

				IGStaticValue sAscending = (IGStaticValue) ascending;
				if (sAscending.isTrue()) {
					right = new IGOperationBinary(left, length, BinOp.ADD, it, getLocation(), zdb).optimize(env);
				} else {
					right = new IGOperationBinary(left, length, BinOp.SUB, it, getLocation(), zdb).optimize(env);
				}
				
			} else {
				IGOperation rightAsc = new IGOperationBinary(left, length, BinOp.ADD, it, getLocation(), zdb).optimize(env);
				IGOperation rightDesc = new IGOperationBinary(left, length, BinOp.SUB, it, getLocation(), zdb).optimize(env);

				right = new IGOperationPhi(ascending, rightAsc, rightDesc, it, getLocation(), zdb).optimize(env);
			}

			IGRange range = new IGRange(left, right, ascending, getLocation(), zdb);

			IGType type = aTypeHint.createSubtype(range, aEE.getInterpreterEnv(), getLocation());

			String bitstring = parseBitStringLiteral();

			value = new IGOperationLiteral(bitstring, type, getLocation());
			break;

		case CHAR:
			if (image.length() != 1) {
				reportError("Bit literal expected.", this, aErrorMode, aReport);
				return res;
			}

			if (aTypeHint == null || !aTypeHint.isCharEnum()) {
				reportError("Cannot infer type information of char literal.", this, aErrorMode, aReport);
				return res;
			}

			value = aTypeHint.findEnumLiteral(image.charAt(0));
			break;

		case BASED:
		case DECIMAL:

			if (image.indexOf('.') < 0) {
				type = aContainer.findUniversalIntType();
			} else {
				type = aContainer.findUniversalRealType();
			}

			ParseResult pr = parseAbstractLiteral();
			if (pr.bigInt == null) {

				value = new IGOperationLiteral(pr.realValue, type, getLocation());

			} else {

				if (type.isInteger()) {
					value = new IGOperationLiteral(pr.bigInt, type, getLocation());
				} else if (type.isScalar()) {
					value = new IGOperationLiteral(pr.bigInt.doubleValue(), type, getLocation());
				} else if (type.isArray()) {

					if (!type.getElementType().isBit()) {
						reportError("Numeric type expected.", this, aErrorMode, aReport);
						return res;
					}

					value = new IGOperationLiteral(IGStaticValue.convert(pr.bigInt, 0), type, getLocation());

				} else {
					reportError("Numeric type expected, got " + type + " instead.", this, aErrorMode, aReport);
					return res;
				}
			}
			break;

		case ENUM:

			if (aTypeHint == null || !aTypeHint.isEnum()) {
				reportError("Cannot infer type information of enum literal.", this, aErrorMode, aReport);
				return res;
			}
			type = aTypeHint;

			value = type.findEnumLiteral(image);
			if (value == null) {
				reportError("Unknown enum literal.", this, aErrorMode, aReport);
				return res;
			}
			break;

		case NULL:

			if (aTypeHint == null || !aTypeHint.isAccess()) {
				reportError("Access type hint expected here.", this, aErrorMode, aReport);
				return res;
			}

			value = new IGOperationLiteral(aTypeHint, getLocation());
			break;

		case PHYSICAL:
			if (aTypeHint == null || !aTypeHint.isPhysical()) {
				reportError("Physical type hint expected here.", this, aErrorMode, aReport);
				return res;
			}
			type = aTypeHint;

			if (unitN.getNumExtensions() > 0) {
				reportError("Sorry, only simple unit names (identifiers) supported here.", this, aErrorMode, aReport);
				return res;
			}

			IGStaticValue scale = type.findScale(unitN.getId(), getLocation());
			if (scale == null) {
				reportError("Unknown scale " + unitN.getId(), unitN, aErrorMode, aReport);
				return res;
			}

			BigDecimal sr = scale.getReal();
			pr = parseAbstractLiteral();

			BigDecimal valr = pr.realValue.multiply(sr);
			BigInteger vali = valr.toBigInteger();

			value = new IGOperationLiteral(vali, type, getLocation());

			break;

		case RANGE:
			throw new ZamiaException("Internal error: don't know how to elaborate range literal.", this);

		case STRING:

			if (aTypeHint == null || !aTypeHint.isArray()) {
				reportError("Cannot infer type information of string literal.", this, aErrorMode, aReport);
				return res;
			}

			it = aTypeHint.getIndexType();

			ascending = it.getAscending(aContainer, getLocation());
			left = it.getLeft(getLocation());
			length = it.getDiscreteValue(image.length() - 1, getLocation(), aErrorMode, aReport);
			if (length == null) {
				return null;
			}

			if (ascending instanceof IGStaticValue) {

				IGStaticValue sAscending = (IGStaticValue) ascending;
				if (sAscending.isTrue()) {
					right = new IGOperationBinary(left, length, BinOp.ADD, it, getLocation(), zdb).optimize(env);
				} else {
					right = new IGOperationBinary(left, length, BinOp.SUB, it, getLocation(), zdb).optimize(env);
				}
				
			} else {
				IGOperation rightAsc = new IGOperationBinary(left, length, BinOp.ADD, it, getLocation(), zdb).optimize(env);
				IGOperation rightDesc = new IGOperationBinary(left, length, BinOp.SUB, it, getLocation(), zdb).optimize(env);

				right = new IGOperationPhi(ascending, rightAsc, rightDesc, it, getLocation(), zdb).optimize(env);
			}

			range = new IGRange(left, right, ascending, getLocation(), zdb);

			type = aTypeHint.createSubtype(range, aEE.getInterpreterEnv(), getLocation());

			value = new IGOperationLiteral(image, type, getLocation());

			break;
		}

		if (value != null) {
			res.add(value);
		}
		return res;
	}
}