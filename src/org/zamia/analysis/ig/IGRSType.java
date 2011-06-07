/* 
 * Copyright 2010, 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 17, 2010
 */
package org.zamia.analysis.ig;

import org.zamia.SourceLocation;
import org.zamia.util.HashMapArray;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGRSType {

	public enum TypeCat {
		BIT, ARRAY, RECORD, INTEGER, REAL
	}

	class RTLRecordField {
		private final String fId;

		private final IGRSType fType;

		public RTLRecordField(String aId, IGRSType aType) {
			fId = aId;
			fType = aType;
		}

		public String getId() {
			return fId;
		}

		public IGRSType getType() {
			return fType;
		}

	}

	private final TypeCat fCat;

	private int fLeft, fRight;

	private boolean fAscending;

	private IGRSType fArrayElementType;

	private HashMapArray<String, RTLRecordField> fRecordFields;

	private final SourceLocation fLocation;

	public IGRSType(TypeCat aCat, SourceLocation aLocation) {
		fLocation = aLocation;

		fCat = aCat;
		if (fCat == TypeCat.RECORD) {
			fRecordFields = new HashMapArray<String, RTLRecordField>();
		}
	}

	public TypeCat getCat() {
		return fCat;
	}

	public void addField(String aId, IGRSType aType) {
		fRecordFields.put(aId, new RTLRecordField(aId, aType));
	}

	public int getNumFields() {
		return fRecordFields.size();
	}

	public String getFieldId(int aIdx) {
		return fRecordFields.get(aIdx).getId();
	}

	public IGRSType getFieldType(int aIdx) {
		return fRecordFields.get(aIdx).getType();
	}

	public void setArrayParams(IGRSType aElementType, int aLeft, boolean aAscending, int aRight) {
		fArrayElementType = aElementType;
		fLeft = aLeft;
		fAscending = aAscending;
		fRight = aRight;
	}

	public IGRSType getArrayElementType() {
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
			//if (fAscending)
			return "[" + fLeft + ":" + fRight + "]";
			//else
			//return "[" + fRight + ":" + fLeft + "]";
		case BIT:
			return "BIT";
		case RECORD:
			return "RECORD";
		}
		return "???";
	}

	public SourceLocation getLocation() {
		return fLocation;
	}

}
