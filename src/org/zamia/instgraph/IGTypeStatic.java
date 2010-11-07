/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 11, 2009
 */
package org.zamia.instgraph;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.util.HashMapArray;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.zdb.ZDB;


/**
 * This is always intended to be a subtype of a regular IGType so not to
 * duplicate all the information of the IGType this is built from.
 * 
 * A static type can do everything the regular type can plus it can provide
 * static information (i.e. IGStaticValue) about its range boundaries, enum
 * literals and so on.
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class IGTypeStatic extends IGType {

	// record types:

	private HashMapArray<String, IGTypeStatic> fStaticFields;

	/**
	 * For creating a static type
	 * 
	 * @throws ZamiaException
	 */

	public IGTypeStatic(TypeCat aCat, IGStaticValue aRange, IGTypeStatic aIndexType, IGTypeStatic aElementType, IGType aBaseType, boolean aUnconstrained, SourceLocation aSrc,
			ZDB aZDB) throws ZamiaException {
		super(aCat, null, aRange, aIndexType, aElementType, aBaseType, aUnconstrained, aSrc, aZDB);

		if (aCat == TypeCat.ENUM) {
			fEnumLiterals = new HashMapArray<String, IGStaticValue>();
		}

		//		if (aCat == TypeCat.ARRAY) {
		//			if (!aUnconstrained) {
		//				IGTypeStatic idxType = getStaticIndexType(null);
		//				long card = idxType.computeCardinality(null);
		//				if (card > 1024) {
		//					logger.error("IGTypeStatic: sanity check failed. foobar. card=%d (>1024)", card);
		//				}
		//			}
		//		}

	}

	@Override
	public IGTypeStatic computeStaticType(IGInterpreterRuntimeEnv aEnv, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
		return this;
	}

	/*
	 * 
	 * SCALAR types
	 * 
	 */

	public IGStaticValue getStaticRange(SourceLocation aSrc) throws ZamiaException {
		if (!isScalar()) {
			throw new ZamiaException("Scalar type expected here.", aSrc);
		}

		return (IGStaticValue) fRange;
	}

	public IGStaticValue getStaticLow(SourceLocation aSrc) throws ZamiaException {
		return getStaticRange().getAscending(aSrc).isTrue() ? getStaticRange().getLeft(aSrc) : getStaticRange().getRight(aSrc);
	}

	public IGStaticValue getStaticHigh(SourceLocation aSrc) throws ZamiaException {
		return getStaticRange().getAscending(aSrc).isTrue() ? getStaticRange().getRight(aSrc) : getStaticRange().getLeft(aSrc);
	}

	public IGStaticValue getStaticLeft(SourceLocation aSrc) throws ZamiaException {
		return getStaticRange().getLeft(aSrc);
	}

	public IGStaticValue getStaticRight(SourceLocation aSrc) throws ZamiaException {
		return getStaticRange().getRight(aSrc);
	}

	public IGStaticValue getStaticAscending(SourceLocation aSrc) throws ZamiaException {
		return getStaticRange().getAscending(aSrc);
	}

	@Override
	public IGOperation getHigh(IGContainer aContainer, SourceLocation aSrc) throws ZamiaException {
		return getStaticHigh(aSrc);
	}

	@Override
	public IGOperation getLow(IGContainer aContainer, SourceLocation aSrc) throws ZamiaException {
		return getStaticLow(aSrc);
	}

	/*
	 * 
	 * DISCRETE types
	 */

	public long computeCardinality(SourceLocation aSrc) throws ZamiaException {
		if (!isDiscrete()) {
			throw new ZamiaException("Discrete type expected here.", aSrc);
		}

		long min = getStaticLow(aSrc).getOrd();
		long max = getStaticHigh(aSrc).getOrd();

		return max - min + 1;
	}

	/*
	 * 
	 * ARRAY types
	 * 
	 */

	public IGTypeStatic getStaticIndexType(SourceLocation aSrc) throws ZamiaException {
		if (!isArray()) {
			throw new ZamiaException("Array type expected here.", aSrc);
		}
		return (IGTypeStatic) getIndexType();
	}

	public IGTypeStatic getStaticElementType(SourceLocation aSrc) throws ZamiaException {
		if (!isArray()) {
			throw new ZamiaException("Array type expected here.", aSrc);
		}
		return (IGTypeStatic) getElementType();
	}

	/*
	 * 
	 * RECORD types
	 * 
	 */

	public void setStaticRecordFields(HashMapArray<String, IGTypeStatic> aStaticFields) {
		fStaticFields = aStaticFields;
	}

	public IGTypeStatic getStaticRecordFieldType(int aIdx) throws ZamiaException {
		String id = getRecordField(aIdx, null).getId();
		return fStaticFields.get(id);
	}

	/*
	 * 
	 * ENUM types
	 * 
	 */

	public IGStaticValue addEnumLiteral(String aId, SourceLocation aSrc) throws ZamiaException {

		IGStaticValue el = new IGStaticValueBuilder(this, aId, aSrc).setOrd(fEnumLiterals.size()).buildConstant();

		addEnumLiteral(aId, el, aSrc);

		return el;
	}

	public void addEnumLiteral(char aLiteral, SourceLocation aSrc) throws ZamiaException {

		String id = "" + aLiteral;

		IGStaticValue el = new IGStaticValueBuilder(this, id, aSrc).setOrd(fEnumLiterals.size()).setChar(aLiteral).buildConstant();

		addEnumLiteral(id, el, aSrc);
	}

	private void addEnumLiteral(String aId, IGStaticValue aLiteral, SourceLocation aSrc) throws ZamiaException {

		if (!isEnum()) {
			throw new ZamiaException("Enum type expected here.", aSrc);
		}

		if (aLiteral.isCharLiteral()) {
			fIsCharEnum = true;
		}

		fEnumLiterals.put(aId, aLiteral);
	}

	public void finishEnum(SourceLocation aSrc) throws ZamiaException {

		int n = getNumEnumLiterals();
		IGStaticValue left = getEnumLiteral(0, aSrc, ASTErrorMode.EXCEPTION, null);
		IGStaticValue right = getEnumLiteral(n - 1, aSrc, ASTErrorMode.EXCEPTION, null);
		IGStaticValue asc = new IGStaticValue(true, getZDB());

		IGTypeStatic rType = new IGTypeStatic(TypeCat.RANGE, null, null, this, null, false, aSrc, getZDB());
		fRange = new IGStaticValueBuilder(rType, null, aSrc).setLeft(left).setRight(right).setAscending(asc).buildConstant();
	}

	/*
	 * 
	 * ACCESS types
	 * 
	 */

	/*
	 * 
	 * PHYSICAL types
	 * 
	 */

	/*
	 * 
	 * RANGE types
	 * 
	 */

	public IGStaticValue getStaticRange() {
		return (IGStaticValue) fRange;
	}

	public IGStaticValue getStaticAscending() {
		return getStaticRange().getAscending();
	}

	public boolean isAscending() {
		return getStaticAscending().isTrue();
	}

	/*
	 * 
	 * UTILS
	 * 
	 */

	/**
	 * This is used in call statements to determine whether actuals match
	 * formals in case of unconstrained arrays
	 * 
	 * => we only check array boundaries here, everything else has already been
	 * checked statically.
	 * 
	 * @param aValueType
	 * @return
	 * @throws ZamiaException
	 */
	public boolean isStaticAssignmentCompatible(IGTypeStatic aValueType) throws ZamiaException {

		if (getCat() == TypeCat.ARRAY) {

			if (isUnconstrained())
				return true;

			if (aValueType.isUnconstrained()) {
				throw new ZamiaException("Unconstrained actual parameter detected.", computeSourceLocation());
			}

			IGTypeStatic idxTarget = getStaticIndexType(null);
			IGTypeStatic idxValue = aValueType.getStaticIndexType(null);

			int lT = (int) idxTarget.getStaticLow(null).getOrd();
			int hT = (int) idxTarget.getStaticHigh(null).getOrd();
			boolean aT = idxTarget.getStaticAscending().isTrue();

			int lV = (int) idxValue.getStaticLow(null).getOrd();
			int hV = (int) idxValue.getStaticHigh(null).getOrd();
			boolean aV = idxValue.getStaticAscending().isTrue();

			return lT == lV && hT == hV && aT == aV;
		}

		return true;
	}

	public IGTypeStatic createSubtype(IGStaticValue aRange, SourceLocation aSrc) throws ZamiaException {

		switch (fCat) {
		case ENUM:
		case INTEGER:
		case REAL:
		case PHYSICAL:
		case ACCESS:
		case FILE:
		case RECORD:
		case RANGE:
			return new IGTypeStatic(fCat, aRange, null, null, this, false, aSrc, getZDB());

		case ARRAY:
			IGTypeStatic idxType = aRange != null ? getStaticIndexType(null).createSubtype(aRange, aSrc) : getStaticIndexType(null);

			return new IGTypeStatic(fCat, null, idxType, getStaticElementType(null), this, aRange != null ? false : fUnconstrained, aSrc, getZDB());

		case ERROR:
			return this;
		}

		throw new ZamiaException("IGTypeStatic: createSubtype(): Internal error - unknown type cat " + fCat);
	}

	@Override
	public IGType createSubtype(IGOperation aRange, IGInterpreterRuntimeEnv aEnv, SourceLocation aSrc) throws ZamiaException {

		if (aRange == null) {
			return new IGTypeStatic(fCat, getStaticRange(), null, null, this, fUnconstrained, aSrc, getZDB());
		}

		IGOperation range = aRange;
		if (aEnv != null && range != null) {
			IGStaticValue sRange = range.computeStaticValue(aEnv, ASTErrorMode.RETURN_NULL, new ErrorReport());
			if (sRange != null) {
				return createSubtype(sRange, aSrc);
			}
		}

		return super.createSubtype(aRange, aEnv, aSrc);
	}

	@Override
	public String toHRString() {

		try {

			StringBuilder buf = new StringBuilder();

			boolean haveId = false;
			String id = getId();
			if (id != null) {

				buf.append(id);
				haveId = true;

			} else {

				if (fCat == TypeCat.ARRAY) {
					buf.append("ARRAY " + getIndexType().toHRString() + " OF " + getElementType().toHRString());
				} else {

					if (fCat != null) {
						switch (fCat) {
						case ENUM:
							buf.append("ENUM");
							break;

						case ACCESS:
							buf.append("ACCESS " + getElementType().toHRString());
							break;

						case FILE:
							buf.append("FILE OF " + getElementType().toHRString());
							break;

						case RECORD:
							buf.append("RECORD (");

							try {
								int n = getNumRecordFields(null);

								for (int i = 0; i < n; i++) {

									IGRecordField rf = getRecordField(i, null);

									buf.append(rf.getId() + ": " + rf.getType().toHRString());

									if (i < (n - 1))
										buf.append(", ");

									if (i > 5 && i < (n - 1)) {
										buf.append("...");
										break;
									}
								}
							} catch (Throwable t) {
								el.logException(t);
							}
							buf.append(")");
							break;

						case ERROR:
							buf.append("***ERROR***");
							break;
							
						case RANGE:
							buf.append("RANGE OF " + getElementType().toHRString());
							break;
						}
					}
				}
			}

			if (isScalar()) {
				// decide whether we want to append the range information
				IGType bt = getBaseType();
				boolean haveRange = false;
				while (bt != null && bt.getId() == null) {
					haveRange = bt.getRange() != null;
					bt = bt.getBaseType();
				}

				if (haveRange || !haveId) {
					buf.append(" " + fRange);
				}
			}

			//			if (fRange != null) {
			//				buf.append(" " + fRange);
			//			} else if (isScalar() && id == null) {
			//				IGType bt = getBaseType();
			//				if (bt != null) {
			//					buf.append(bt.toHRString());
			//				}
			//			}

			return buf.toString();

		} catch (Throwable e) {
			el.logException(e);
			return "???";
		}
	}

}
