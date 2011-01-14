/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 17, 2010
 */
package org.zamia.rtlng;

import org.zamia.SourceLocation;
import org.zamia.util.HashMapArray;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class RTLType extends RTLItem {

	public enum TypeCat {
		BIT, ARRAY, RECORD
	}

	class RTLRecordField {
		private final String fId;

		private final RTLType fType;

		public RTLRecordField(String aId, RTLType aType) {
			fId = aId;
			fType = aType;
		}

		public String getId() {
			return fId;
		}

		public RTLType getType() {
			return fType;
		}

	}

	private final TypeCat fCat;

	private int fLeft, fRight;

	private boolean fAscending;

	private RTLType fArrayElementType;

	private HashMapArray<String, RTLRecordField> fRecordFields;

	public RTLType(TypeCat aCat, SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);

		fCat = aCat;
		if (fCat == TypeCat.RECORD) {
			fRecordFields = new HashMapArray<String, RTLRecordField>();
		}
	}

	public TypeCat getCat() {
		return fCat;
	}

	public void addField(String aId, RTLType aType) {
		fRecordFields.put(aId, new RTLRecordField(aId, aType));
	}

	public int getNumFields() {
		return fRecordFields.size();
	}

	public String getFieldId(int aIdx) {
		return fRecordFields.get(aIdx).getId();
	}

	public RTLType getFieldType(int aIdx) {
		return fRecordFields.get(aIdx).getType();
	}

	public void setArrayParams(RTLType aElementType, int aLeft, boolean aAscending, int aRight) {
		fArrayElementType = aElementType;
		fLeft = aLeft;
		fAscending = aAscending;
		fRight = aRight;
	}

	public RTLType getArrayElementType() {
		return fArrayElementType;
	}

	public int getArrayLeft() {
		return fLeft;
	}

	public int getArrayRight() {
		return fRight;
	}

	public boolean getArrayAscending() {
		return fAscending;
	}

	public boolean isCompatible(RTLType aType) {

		if (aType.getCat() != fCat)
			return false;

		switch (fCat) {
		case BIT:
			return true;
		case ARRAY:
		case RECORD:
		}

		// FIXME: implement

		throw new RuntimeException("Sorry, not implemented.");
	}

	public RTLType computeEnableType() {

		switch (fCat) {
		case BIT:
			return this;

		case ARRAY:
			RTLType res = new RTLType(TypeCat.ARRAY, computeSourceLocation(), getZDB());

			res.setArrayParams(fArrayElementType.computeEnableType(), fLeft, fAscending, fRight);

			return res;

		case RECORD:
		}

		// FIXME: implement

		throw new RuntimeException("Sorry, not implemented.");
	}

	public int computeCardinality() {
		return fAscending ? fRight - fLeft + 1 : fLeft - fRight + 1;
	}

	public int getArrayLow() {
		return fAscending ? fLeft : fRight;
	}

	public int getArrayHigh() {
		return fAscending ? fRight : fLeft;
	}

	public String toString() {
		switch (fCat) {
		case ARRAY:
			if (fAscending)
				return fLeft + " to " + fRight;
			else
				return fLeft + " downto " + fRight;
		case BIT:
			return "BIT";
		case RECORD:
			return "RECORD";
		}
		return "???";
	}

}
