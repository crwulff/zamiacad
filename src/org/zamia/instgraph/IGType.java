/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 11, 2009
 */
package org.zamia.instgraph;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.util.HashMapArray;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.zdb.ZDB;


/**
 * Abstract base class for static and nonstatic types
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class IGType extends IGContainerItem {

	public static IGType createErrorType(ZDB aZDB) {
		return new IGType(TypeCat.ERROR, null, null, null, null, null, false, null, aZDB);
	}

	public enum TypeCat {
		INTEGER, ENUM, REAL, PHYSICAL, RECORD, ARRAY, ACCESS, FILE, ERROR, RANGE
	};

	protected TypeCat fCat;

	private transient IGType fBaseType;

	private long fBaseTypeDBID;

	// enum types:

	protected HashMapArray<String, IGStaticValue> fEnumLiterals;

	protected boolean fIsCharEnum = false;

	// scalar (sub-)types:

	protected IGOperation fRange;

	// array, range, file and access types:

	private transient IGType fElementType;

	private long fElementTypeDBID;

	// array types only:

	private transient IGType fIndexType;

	private long fIndexTypeDBID;

	protected boolean fUnconstrained;

	// physical types:

	protected HashMapArray<String, IGStaticValue> fUnits;

	// record types:

	private HashMapArray<String, IGRecordField> fFields;

	// resolution function:

	private long fRFDBID;

	// only true for the two universal types: universal_integer and universal_real

	private boolean fUniversal = false;

	// only true for STANDARD.BIT:
	private boolean fBit = false;

	public IGType(TypeCat aCat, IGSubProgram aRF, IGOperation aRange, IGType aIndexType, IGType aElementType, IGType aBaseType, boolean aUnconstrained, SourceLocation aSrc,
			ZDB aZDB) {
		super(null, aSrc, aZDB);
		fCat = aCat;
		fRange = aRange;
		fBaseType = aBaseType;
		fElementType = aElementType;
		fIndexType = aIndexType;
		fUnconstrained = aUnconstrained;
		fRFDBID = save(aRF);

		//		if (aRange != null) {
		//			IGType rType = aRange.getType();
		//			if (!rType.isRange()) {
		//				System.out.println ("IGType: foobar range's type is not a range: "+rType.getCat());
		//			}
		//		}

		if (aBaseType == null) {
			IGType rType;
			switch (fCat) {
			case PHYSICAL:
				fUnits = new HashMapArray<String, IGStaticValue>();
				if (fRange == null) {
					rType = new IGType(TypeCat.RANGE, null, null, null, this, null, false, aSrc, aZDB);
					IGRange range = new IGRange(rType, aSrc, aZDB);
					fRange = range;
				}
				break;
			case RECORD:
				fFields = new HashMapArray<String, IGRecordField>();
				break;
			case INTEGER:
			case REAL:
				if (fRange == null) {
					rType = new IGType(TypeCat.RANGE, null, null, null, this, null, false, aSrc, aZDB);
					fRange = new IGRange(rType, aSrc, aZDB);
				}
				break;
			case ENUM:
				fEnumLiterals = new HashMapArray<String, IGStaticValue>();
				break;
			}
		}
	}

	/*********************************************************
	 * 
	 * GENERAL STUFF THAT APPLIES TO ALL TYPE CATEGORIES
	 * 
	 *********************************************************/

	public TypeCat getCat() {
		return fCat;
	}

	public IGTypeStatic computeStaticType(IGInterpreterRuntimeEnv aEnv, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		long dbid = store();

		IGTypeStatic res = aEnv != null ? aEnv.getCachedActualType(dbid) : null;

		if (res != null) {
			return res;
		}

		IGTypeStatic sET = null;
		IGTypeStatic sIdxType = null;
		HashMapArray<String, IGTypeStatic> staticFields = null;

		switch (fCat) {
		case ENUM:
			break;

		case PHYSICAL:
			break;

		case ARRAY:
			sET = getElementType().computeStaticType(aEnv, aErrorMode, aReport);
			if (sET == null) {
				return null;
			}

			sIdxType = getIndexType().computeStaticType(aEnv, aErrorMode, aReport);
			if (sIdxType == null) {
				return null;
			}

			break;

		case RECORD:
			int n = getNumRecordFields(null);
			staticFields = new HashMapArray<String, IGTypeStatic>(n);
			for (int i = 0; i < n; i++) {
				IGRecordField field = getRecordField(i, null);

				String id = field.getId();
				IGTypeStatic sType = field.getType().computeStaticType(aEnv, aErrorMode, aReport);
				if (sType == null) {
					return null;
				}

				if (staticFields.containsKey(id)) {
					logger.error("IGStaticType: Record has field %s twice!", id);
				}

				staticFields.put(id, sType);

				//logger.info("IGStaticType: Record field %s => %s", id, sType);
			}

			break;

		case RANGE:
		case FILE:
		case ACCESS:
			sET = getElementType().computeStaticType(aEnv, aErrorMode, aReport);
			if (sET == null) {
				return null;
			}
			break;

		}

		IGStaticValue sRange = null;
		IGOperation range = getRange();
		if (range != null) {
			sRange = range.computeStaticValue(aEnv, aErrorMode, aReport);
			if (sRange == null) {
				return null;
			}
		}

		IGTypeStatic st = new IGTypeStatic(getCat(), sRange, sIdxType, sET, this, isUnconstrained(), computeSourceLocation(), getZDB());
		st.setId(getId());
		st.setStaticRecordFields(staticFields);
		if (aEnv != null) {
			aEnv.putCachedActualType(dbid, st);
		}

		return st;
	}

	public IGType getOriginalType() {
		IGType bt = getBaseType();
		if (bt != null)
			return bt.getOriginalType();
		return this;
	}

	public boolean isAssignmentCompatible(IGType aType, SourceLocation aSrc) throws ZamiaException {

		if (aType == this)
			return true;

		if (aType == null)
			return false;

		TypeCat cat2 = aType.getCat();

		if (cat2 == TypeCat.ERROR) {
			return true;
		}

		IGType o1 = getOriginalType();
		IGType o2 = aType.getOriginalType();

		if (o1.store() == o2.store())
			return true;

		if (o1.isUniversal()) {
			if (o1.isReal()) {
				return o2.isReal();
			} else if (o1.isInteger()) {
				return o2.isInteger() || o2.isPhysical();
			}
		}
		if (o2.isUniversal()) {
			if (o2.isReal()) {
				return o1.isReal();
			} else if (o2.isInteger()) {
				return o1.isInteger() || o2.isPhysical();
			}
		}

		if (fCat == TypeCat.RANGE) {
			IGType e1 = getElementType();
			IGType e2 = aType.getElementType();
			return e1.isAssignmentCompatible(e2, aSrc);
		} else if (fCat == TypeCat.ACCESS) {
			IGType e1 = getElementType();
			IGType e2 = aType.getElementType();
			return e1.isAssignmentCompatible(e2, aSrc);
		}

		return false;
	}

	/**
	 * Figure out how well this type matches the given one
	 * 
	 * 0 => not at all 1 => with implicit conversion 2 => perfect
	 * 
	 * @param aType
	 * @param aSrc
	 * @return
	 * @throws ZamiaException
	 */
	public int getAssignmentCompatibilityScore(IGType aType, SourceLocation aSrc) throws ZamiaException {

		if (aType == this)
			return 2;

		if (aType == null)
			return 2;

		TypeCat cat2 = aType.getCat();

		if (cat2 == TypeCat.ERROR) {
			return 2;
		}

		IGType o1 = getOriginalType();
		IGType o2 = aType.getOriginalType();

		if (o1.store() == o2.store())
			return 2;

		if (o1.isUniversal()) {
			if (o1.isReal()) {
				return o2.isReal() ? 1 : 0;
			} else if (o1.isInteger()) {
				return o2.isInteger() ? 1 : 0;
			}
		}
		if (o2.isUniversal()) {
			if (o2.isReal()) {
				return o1.isReal() ? 1 : 0;
			} else if (o2.isInteger()) {
				return o1.isInteger() ? 1 : 0;
			}
		}

		return 0;
	}

	public boolean conforms(IGType aType) throws ZamiaException {
		if (aType == this)
			return true;

		if (aType == null)
			return false;

		TypeCat cat2 = aType.getCat();

		if (cat2 == TypeCat.ERROR) {
			return true;
		}

		//logger.info("IGType: Matching %s against %s", this, aType);

		String id1 = aType.getId();
		if (id1 != null) {

			return id1.equals(getId());
		}

		switch (fCat) {
		case ERROR:
			return true;

		case ENUM:

			if (cat2 != TypeCat.ENUM)
				return false;

			int n1 = getNumEnumLiterals();
			int n2 = aType.getNumEnumLiterals();

			if (n1 != n2) {
				return false;
			}

			for (int i = 0; i < n1; i++) {
				IGStaticValue el1 = getEnumLiteral(i, null, ASTErrorMode.EXCEPTION, null);
				IGStaticValue el2 = aType.getEnumLiteral(i, null, ASTErrorMode.EXCEPTION, null);

				if (!el1.getId().equals(el2.getId()))
					return false;
			}

			return getOriginalType() == aType.getOriginalType();

		case INTEGER:
			return cat2 == fCat;

		case ARRAY:

			if (cat2 != TypeCat.ARRAY) {
				return false;
			}

			IGType idx1 = getIndexType();
			IGType idx2 = aType.getIndexType(null);

			if (isUnconstrained(null) || aType.isUnconstrained(null)) {
				return aType.getElementType(null).conforms(getElementType());
			}

			boolean res = aType.getElementType(null).conforms(getElementType()) && idx1.conforms(idx2);
			if (res) {
				return true;
			}
			return false;

		case ACCESS:
			if (cat2 != TypeCat.ACCESS) {
				return false;
			}

			return getElementType().conforms(aType.getElementType());

		case REAL:
			if (cat2 != TypeCat.REAL) {
				return false;
			}
			IGType bt = aType.getBaseType();

			IGType baseType = getBaseType();

			if (baseType == null && bt == aType)
				return this == bt;

			if (bt != null && !bt.conforms(baseType))
				return false;
			if (bt == null && baseType != null)
				return false;

			return true;

		case PHYSICAL:
			baseType = getBaseType();

			if (baseType != null)
				return baseType.conforms(aType);

			if (cat2 != TypeCat.PHYSICAL)
				return false;

			IGType bt1 = aType.getBaseType();

			if (bt1 != aType)
				return conforms(bt1);

			return true;

		case FILE:
			if (cat2 != TypeCat.FILE)
				return false;

			return aType.getElementType().conforms(getElementType());

		case RECORD:

			if (cat2 != TypeCat.RECORD)
				return false;

			int n = getNumRecordFields(null);

			if (aType.getNumRecordFields(null) != n)
				return false;

			for (int i = 0; i < n; i++) {

				IGRecordField rf = aType.getRecordField(i, null);
				IGRecordField rf2 = getRecordField(i, null);

				if (!rf.getId().equals(rf2.getId()))
					return false;
				if (!rf.getType().conforms(rf2.getType()))
					return false;

			}

			return true;

		}
		return false;
	}

	public String computeSignature() {

		StringBuilder buf = new StringBuilder();
		if (fRFDBID != 0) {
			buf.append("{RESOLVED " + fRFDBID + "}");
		}

		if (fCat == TypeCat.ACCESS) {
			buf.append("ACCESS ");
		} else if (fCat == TypeCat.FILE) {
			buf.append("FILE ");
		}

		IGType bt = getBaseType();
		if (bt != null) {
			buf.append(bt.computeSignature());
		} else {
			buf.append(getDBID());

			if (fCat == TypeCat.ARRAY) {
				buf.append("ARRAY ");

				if (fUnconstrained) {
					buf.append(" UNCONSTRAINED ");
				} else {
					buf.append(" CONSTRAINED ");
				}

				IGType idx1 = getIndexType();

				buf.append(idx1.computeSignature());

				IGType et = getElementType();
				buf.append(et.computeSignature());
			}
		}
		return buf.toString();
	}

	public IGType convertType(IGType aType, SourceLocation aSrc) throws ZamiaException {

		if (aType.getCat() == TypeCat.ERROR)
			return this;
		if (getCat() == TypeCat.ERROR)
			return aType;

		if (aType.isScalar() && isScalar()) {
			return aType;
		}

		if (fCat == TypeCat.ARRAY) {
			if (aType.getCat() != TypeCat.ARRAY) {
				throw new ZamiaException("Type conversion between array and non-array types is not allowed.", aSrc);
			}

			// just check - will throw an exception if it fails
			getElementType().convertType(aType.getElementType(aSrc), aSrc);

			if (isUnconstrained(aSrc)) {

				if (aType.isUnconstrained(aSrc))
					return this;

				return createSubtype(aType.getIndexType(aSrc).getRange(aSrc), null, aSrc);
			}

			return this;

		}

		throw new ZamiaException("Cannot convert from " + this + " to " + aType, aSrc);

	}

	public IGType createSubtype(IGOperation aRange, IGInterpreterRuntimeEnv aEnv, SourceLocation aSrc) throws ZamiaException {

		switch (fCat) {
		case ENUM:
		case INTEGER:
		case REAL:
		case PHYSICAL:
		case ACCESS:
		case FILE:
		case RECORD:
		case RANGE:
			return new IGType(fCat, null, aRange, null, null, this, false, aSrc, getZDB());

		case ARRAY:
			IGType idxType = aRange != null ? getIndexType().createSubtype(aRange, aEnv, aSrc) : getIndexType();

			return new IGType(fCat, null, null, idxType, getElementType(), this, aRange != null ? false : fUnconstrained, aSrc, getZDB());

		case ERROR:
			return this;
		}

		throw new ZamiaException("Cannot create subtypes from type " + this, aSrc);
	}

	public IGType createSubtype(IGSubProgram aRf, String aId, SourceLocation aSrc) throws ZamiaException {
		switch (fCat) {
		case ENUM:
		case INTEGER:
		case REAL:
		case PHYSICAL:
		case ACCESS:
		case FILE:
		case RECORD:
		case RANGE:
		case ARRAY:
			return new IGType(fCat, aRf, null, null, null, this, fUnconstrained, aSrc, getZDB());

		case ERROR:
			return this;
		}

		throw new ZamiaException("Cannot create subtypes from type " + this, aSrc);
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		if (fBaseTypeDBID == 0 && fBaseType != null) {
			fBaseTypeDBID = save(fBaseType);
			fBaseType = null; // we have to drop the reference so GC will be able to free the memory
		}
		if (fElementTypeDBID == 0 && fElementType != null) {
			fElementTypeDBID = save(fElementType);
			fElementType = null; // we have to drop the reference so GC will be able to free the memory
		}
		if (fIndexTypeDBID == 0 && fIndexType != null) {
			fIndexTypeDBID = save(fIndexType);
			fIndexType = null; // we have to drop the reference so GC will be able to free the memory
		}
		out.defaultWriteObject();
	}

	public IGType getBaseType() {
		if (fBaseType != null) {
			return fBaseType;
		}
		if (fBaseTypeDBID == 0)
			return null;
		return (IGType) getZDB().load(fBaseTypeDBID);
	}

	public boolean isAtomic() {
		switch (fCat) {
		case INTEGER:
		case PHYSICAL:
		case REAL:
		case ENUM:
		case FILE:
		case ACCESS:
		case ERROR:
			return true;
		}
		return false;
	}

	/*********************************************************
	 * 
	 * DISCRETE
	 * 
	 *********************************************************/

	public boolean isDiscrete() {
		return fCat == TypeCat.INTEGER || fCat == TypeCat.ENUM;
	}

	public IGOperation getDiscreteValue(long aOrd, SourceLocation aSrc, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
		IGType bt = getBaseType();
		if (bt != null)
			return bt.getDiscreteValue(aOrd, aSrc, aErrorMode, aReport);

		switch (fCat) {
		case ENUM:
			if (aOrd >= fEnumLiterals.size() || aOrd<0) {
				ZamiaException msg = new ZamiaException("Enum ord out of bounds: " + aOrd, aSrc);
				if (aErrorMode == ASTErrorMode.EXCEPTION) {
					throw msg;
				}
				aReport.append(msg);
				return null;
			}
			return fEnumLiterals.get((int) aOrd);

		case INTEGER:
			return new IGOperationLiteral(aOrd, this, aSrc);
		}

		ZamiaException msg = new ZamiaException("Discrete type expected here.", aSrc);
		if (aErrorMode == ASTErrorMode.EXCEPTION) {
			throw msg;
		}
		aReport.append(msg);
		return null;
	}

	public IGOperation getOne(SourceLocation aSrc) throws ZamiaException {
		switch (fCat) {
		case ENUM:
			return fEnumLiterals.get(0);

		case INTEGER:
			return new IGOperationLiteral(1, this, aSrc);
		}

		throw new ZamiaException("Discrete type expected here.", aSrc);
	}

	/*********************************************************
	 * 
	 * ENUM
	 * 
	 *********************************************************/

	public boolean isEnum() {
		return fCat == TypeCat.ENUM;
	}

	public boolean isCharEnum() {
		IGType bt = getBaseType();
		if (bt != null)
			return bt.isCharEnum();
		return fIsCharEnum;
	}

	public IGStaticValue findEnumLiteral(String aId) {

		IGType bt = getBaseType();
		if (bt != null) {
			return bt.findEnumLiteral(aId);
		}

		if (fEnumLiterals == null) {
			return null;
		}

		return fEnumLiterals.get(aId);
	}

	public IGStaticValue findEnumLiteral(char aChar) {
		return findEnumLiteral("" + aChar);
	}

	public int getNumEnumLiterals() {
		IGType bt = getBaseType();
		if (bt != null) {
			return bt.getNumEnumLiterals();
		}

		if (fEnumLiterals == null) {
			return 0;
		}

		return fEnumLiterals.size();
	}

	public IGStaticValue getEnumLiteral(int aIdx, SourceLocation aSrc, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
		IGType bt = getBaseType();
		if (bt != null) {
			return bt.getEnumLiteral(aIdx, aSrc, aErrorMode, aReport);
		}
		if (aIdx >= fEnumLiterals.size() || aIdx < 0) {
			ZamiaException msg = new ZamiaException("Enum ord out of bounds: " + aIdx, aSrc);
			if (aErrorMode == ASTErrorMode.EXCEPTION) {
				throw msg;
			}
			aReport.append(msg);
			return null;
		}

		return fEnumLiterals.get(aIdx);
	}

	public boolean isBool() {
		return fCat == TypeCat.ENUM && getNumEnumLiterals() == 2;
	}

	/*********************************************************
	 * 
	 * SCALAR
	 * 
	 *********************************************************/

	public boolean isScalar() {
		switch (fCat) {
		case INTEGER:
		case PHYSICAL:
		case REAL:
		case ENUM:
			return true;
		}
		return false;
	}

	public boolean isInteger() {
		return fCat == TypeCat.INTEGER;
	}

	public boolean isReal() {
		return fCat == TypeCat.REAL;
	}

	public IGOperation getRange() {
		if (fRange == null) {
			IGType baseType = getBaseType();
			if (baseType != null) {
				return baseType.getRange();
			}
		}
		return fRange;
	}

	public void setLeft(IGOperation aLeft, SourceLocation aSrc) throws ZamiaException {
		if (!isScalar()) {
			throw new ZamiaException("Scalar type expected here.", aSrc);
		}
		if (!(fRange instanceof IGRange)) {
			throw new ZamiaException("Internal error, sorry.", aSrc);
		}
		IGRange range = (IGRange) fRange;
		range.setLeft(aLeft);
	}

	public IGOperation getLeft(SourceLocation aSrc) throws ZamiaException {
		if (!isScalar()) {
			throw new ZamiaException("Scalar type expected here.", aSrc);
		}
		return fRange.getRangeLeft(aSrc);
	}

	public void setRight(IGOperation aRight, SourceLocation aSrc) throws ZamiaException {
		if (!isScalar()) {
			throw new ZamiaException("Scalar type expected here.", aSrc);
		}
		if (!(fRange instanceof IGRange)) {
			throw new ZamiaException("Internal error, sorry.", aSrc);
		}
		IGRange range = (IGRange) fRange;
		range.setRight(aRight);
	}

	public IGOperation getRight(SourceLocation aSrc) throws ZamiaException {
		if (!isScalar()) {
			throw new ZamiaException("Scalar type expected here.", aSrc);
		}
		return fRange.getRangeRight(aSrc);
	}

	public IGOperation getAscending(IGContainer aContainer, SourceLocation aSrc) throws ZamiaException {
		if (!isScalar()) {
			throw new ZamiaException("Scalar type expected here.", aSrc);
		}

		return fRange.getRangeAscending(aContainer, aSrc);
	}

	public void setAscending(IGOperation aAscending, SourceLocation aSrc) throws ZamiaException {
		if (!isScalar()) {
			throw new ZamiaException("Scalar type expected here.", aSrc);
		}
		if (!(fRange instanceof IGRange)) {
			throw new ZamiaException("Internal error, sorry.", aSrc);
		}
		IGRange range = (IGRange) fRange;
		range.setAscending(aAscending);
	}

	public IGOperation getRange(SourceLocation aSrc) throws ZamiaException {
		if (!isScalar()) {
			throw new ZamiaException("Scalar type expected here.", aSrc);
		}
		return getRange();
	}

	public IGOperation getHigh(IGContainer aContainer, SourceLocation aSrc) throws ZamiaException {

		IGOperation left = getLeft(aSrc);
		IGOperation right = getRight(aSrc);
		IGOperation ascending = getAscending(aContainer, aSrc);

		return new IGOperationPhi(ascending, right, left, this, aSrc, getZDB());
	}

	public IGOperation getLow(IGContainer aContainer, SourceLocation aSrc) throws ZamiaException {

		IGOperation left = getLeft(aSrc);
		IGOperation right = getRight(aSrc);
		IGOperation ascending = getAscending(aContainer, aSrc);

		return new IGOperationPhi(ascending, left, right, this, aSrc, getZDB());
	}

	/*********************************************************
	 * 
	 * ARRAY / FILE
	 * 
	 *********************************************************/

	public boolean isArray() {
		return fCat == TypeCat.ARRAY;
	}

	public boolean isFile() {
		return fCat == TypeCat.FILE;
	}

	public boolean isString() {

		if (fCat != TypeCat.ARRAY)
			return false;
		
		return getElementType().isCharEnum();
	}

	public IGType getElementType(SourceLocation aSrc) throws ZamiaException {
		if (!isArray() && !isFile()) {
			throw new ZamiaException("Array type expected here.", aSrc);
		}
		return getElementType();
	}

	public IGType getElementType() {
		if (fElementType != null) {
			return fElementType;
		}
		if (fElementTypeDBID != 0) {
			return (IGType) getZDB().load(fElementTypeDBID);
		}
		IGType bt = getBaseType();
		if (bt != null) {
			return bt.getElementType();
		}
		return null;
	}

	public IGType getIndexType() {
		if (fIndexType != null) {
			return fIndexType;
		}
		if (fIndexTypeDBID != 0) {
			return (IGType) getZDB().load(fIndexTypeDBID);
		}
		IGType bt = getBaseType();
		if (bt != null) {
			return bt.getIndexType();
		}
		return null;
	}

	public boolean isUnconstrained() {
		return fUnconstrained;
	}

	public boolean isUnconstrained(SourceLocation aSrc) throws ZamiaException {
		if (!isArray()) {
			throw new ZamiaException("Array type expected here.", aSrc);
		}
		return fUnconstrained;
	}

	public IGType getIndexType(SourceLocation aSrc) throws ZamiaException {
		if (!isArray()) {
			throw new ZamiaException("Array type expected here.", aSrc);
		}
		return getIndexType();
	}

	/*********************************************************
	 * 
	 * ACCESS
	 * 
	 *********************************************************/

	public boolean isAccess() {
		return fCat == TypeCat.ACCESS;
	}

	/*********************************************************
	 * 
	 * PHYSICAL
	 * 
	 *********************************************************/

	public boolean isPhysical() {
		return fCat == TypeCat.PHYSICAL;
	}

	public void addUnit(String aId, IGStaticValue aScale, SourceLocation aSrc) throws ZamiaException {

		if (!isPhysical()) {
			throw new ZamiaException("Physical type expected here.", aSrc);
		}

		fUnits.put(aId, aScale);
	}

	public IGStaticValue findScale(String aId, SourceLocation aSrc) throws ZamiaException {
		if (!isPhysical()) {
			throw new ZamiaException("Physical type expected here.", aSrc);
		}

		IGType bt = getBaseType();
		if (bt != null) {
			return bt.findScale(aId, aSrc);
		}

		return fUnits.get(aId);
	}

	public int getNumUnits(SourceLocation aSrc) throws ZamiaException {
		if (!isPhysical()) {
			throw new ZamiaException("Physical type expected here.", aSrc);
		}

		IGType bt = getBaseType();
		if (bt != null) {
			return bt.getNumUnits(aSrc);
		}

		if (fUnits == null) {
			return 0;
		}
		return fUnits.size();
	}

	public IGStaticValue getUnit(int aIdx, SourceLocation aSrc) throws ZamiaException {
		if (!isPhysical()) {
			throw new ZamiaException("Physical type expected here.", aSrc);
		}

		IGType bt = getBaseType();
		if (bt != null) {
			return bt.getUnit(aIdx, aSrc);
		}

		return fUnits.get(aIdx);
	}

	/*********************************************************
	 * 
	 * RECORD
	 * 
	 *********************************************************/

	public boolean isRecord() {
		return fCat == TypeCat.RECORD;
	}

	public void addRecordField(String aId, IGType aType, SourceLocation aSrc) throws ZamiaException {

		if (!isRecord()) {
			throw new ZamiaException("Record type expected here.", aSrc);
		}

		fFields.put(aId, new IGRecordField(aType, aId, aSrc, getZDB()));
	}

	public IGRecordField findRecordField(String aId, SourceLocation aSrc) throws ZamiaException {
		if (!isRecord()) {
			throw new ZamiaException("Record type expected here.", aSrc);
		}

		IGType bt = getBaseType();
		if (bt != null) {
			return bt.findRecordField(aId, aSrc);
		}

		return fFields.get(aId);
	}

	public int getNumRecordFields(SourceLocation aSrc) throws ZamiaException {
		if (!isRecord()) {
			throw new ZamiaException("Record type expected here.", aSrc);
		}

		IGType bt = getBaseType();
		if (bt != null) {
			return bt.getNumRecordFields(aSrc);
		}

		return fFields.size();
	}

	public IGRecordField getRecordField(int aIdx, SourceLocation aSrc) throws ZamiaException {
		if (!isRecord()) {
			throw new ZamiaException("Record type expected here.", aSrc);
		}

		IGType bt = getBaseType();
		if (bt != null) {
			return bt.getRecordField(aIdx, aSrc);
		}
		return fFields.get(aIdx);
	}

	public boolean isError() {
		return fCat == TypeCat.ERROR;
	}

	@Override
	public long storeOrUpdate() {
		super.storeOrUpdate();
		if (fRange != null) {
			fRange.storeOrUpdate();
		}
		return fDBID;
	}

	/*********************************************************
	 * 
	 * RANGE
	 * 
	 *********************************************************/

	public boolean isRange() {
		return fCat == TypeCat.RANGE;
	}

	@Override
	public String toString() {
		return toHRString();
	}

	/*********************************************************
	 * 
	 * LOGIC
	 * 
	 *********************************************************/

	/**
	 * returns true if this is either a single bit or a bit vector
	 * 
	 * @return
	 */
	public boolean isLogic() {

		if (fCat == TypeCat.ARRAY)
			return getElementType().isLogic();

		return isBit() || (fId != null && (fId.contains("STD_LOGIC") || fId.contains("STD_ULOGIC")));
	}

	// used to mark STANDARD.BIT
	public void setBit(boolean aBit) {
		fBit = aBit;
	}

	public boolean isBit() {
		return fBit;
		//return isEnum() && isCharEnum();
	}

	/*********************************************************
	 * 
	 * UTILS
	 * 
	 *********************************************************/

	/**
	 * a less detailed, more human readable version of toString()
	 * 
	 * @return
	 */

	public String toHRString() {

		try {

			StringBuilder buf = new StringBuilder();

			String id = getId();
			if (id != null) {

				buf.append(id);

			} else {

				if (fCat == TypeCat.ARRAY) {
					buf.append("ARRAY " + getIndexType().toHRString() + " OF " + getElementType().toHRString());
				} else {

					IGType bt = getBaseType();

					if (bt != null) {
						buf.append(bt.toHRString());
					} else {

						if (fCat != null) {
							switch (fCat) {
							case ENUM:
								buf.append("(");
								int n = fEnumLiterals.size();
								for (int i = 0; i < n; i++) {
									IGStaticValue el = fEnumLiterals.get(i);

									buf.append(el.toString());
									if (i < (n - 1))
										buf.append(", ");

									if (i > 5 && i < (n - 1)) {
										buf.append("...");
										break;
									}
								}

								buf.append(")");
								break;

							case ARRAY:
								buf.append("ARRAY " + getIndexType().toHRString() + " OF " + getElementType().toHRString());
								break;

							case RANGE:
								buf.append("RANGE OF " + getElementType().toHRString());
								break;

							case ACCESS:
								buf.append("ACCESS OF " + getElementType().toHRString());
								break;

							case FILE:
								buf.append("FILE OF " + getElementType().toHRString());
								break;

							case RECORD:
								buf.append("RECORD (");

								try {
									n = getNumRecordFields(null);

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
							}
						}
					}
				}
			}

			if (fRange != null && fId == null) {
				buf.append(" " + fRange);
			}

			return buf.toString();

		} catch (Throwable e) {
			el.logException(e);
			return "???";
		}
	}

	@Override
	public IGItem getChild(int aIdx) {
		return null;
	}

	@Override
	public int getNumChildren() {
		return 0;
	}

	public boolean isUniversal() {
		return fUniversal;
	}

	public void setUniversal(boolean aUniversal) {
		fUniversal = aUniversal;
	}
}
