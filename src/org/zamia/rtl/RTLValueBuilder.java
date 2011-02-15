/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 20, 2010
 */
package org.zamia.rtl;

import java.util.ArrayList;
import java.util.HashMap;

import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.rtl.RTLType.TypeCat;
import org.zamia.rtl.RTLValue.BitValue;
import org.zamia.zdb.ZDB;

/**
 * Builder class to construct complex (array/record typed), immutable
 * RTLValue objects
 * 
 * @author Guenter Bartsch
 * 
 */

public class RTLValueBuilder {

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	private final RTLType fType;

	private final SourceLocation fLocation;

	private final ZDB fZDB;

	private ArrayList<BuilderEntry> fArrayValues;

	private int fArrayOffset;

	private BitValue fBit = BitValue.BV_U;

	private HashMap<String, RTLValueBuilder> fRecordValues;

	class BuilderEntry {
		private RTLValue fValue;

		private RTLValueBuilder fBuilder;

		public BuilderEntry(RTLValue aConstant) {
			fValue = aConstant;
			fBuilder = null;
		}

		public BuilderEntry(RTLValueBuilder aBuilder) {
			fBuilder = aBuilder;
			fValue = null;
		}

		public RTLValue getConstant() throws ZamiaException {
			if (fBuilder != null)
				return fBuilder.buildValue();
			return fValue;
		}

		public RTLValueBuilder getConstantBuilder() throws ZamiaException {
			if (fBuilder != null)
				return fBuilder;
			fBuilder = new RTLValueBuilder(fValue, fLocation, fZDB);
			fValue = null;
			return fBuilder;
		}
	}

	public RTLValueBuilder(RTLValue aValue, SourceLocation aLocation, ZDB aZDB) {
		this(aValue.getType(), aLocation, aZDB);

		setValue(aValue);
	}

	public RTLValueBuilder(RTLType aType, SourceLocation aLocation, ZDB aZDB) {
		fType = aType;
		fLocation = aLocation;
		fZDB = aZDB;

		switch (fType.getCat()) {
		case ARRAY:
			int card = fType.computeCardinality();

			if (card >= 0) {
				fArrayValues = new ArrayList<BuilderEntry>(card);

				for (int i = 0; i < card; i++) {
					fArrayValues.add(null);
				}
			}
			fArrayOffset = fType.getArrayLow();
			break;
		case RECORD:
			fRecordValues = new HashMap<String, RTLValueBuilder>();
			break;
		}

	}

	public RTLValue buildValue() throws ZamiaException {
		return new RTLValue(this);
	}

	public void setValue(RTLValue aValue) {

		switch (fType.getCat()) {
		case BIT:
			fBit = aValue.getBit();
			break;

		case ARRAY:
			int card = fType.computeCardinality();
			fArrayOffset = fType.getArrayLow();

			if (card > 0) {
				fArrayValues = new ArrayList<BuilderEntry>(card);

				for (int i = 0; i < card; i++) {
					fArrayValues.add(new BuilderEntry(aValue.getValue(i + fArrayOffset)));
				}

			} else {
				fArrayValues = null;
			}
			break;

		case RECORD:
			int n = fType.getNumFields();
			fRecordValues = new HashMap<String, RTLValueBuilder>();
			for (int i = 0; i < n; i++) {
				String id = fType.getFieldId(i);
				fRecordValues.put(id, new RTLValueBuilder(aValue.getRecordFieldValue(id), fLocation, fZDB));
			}
			break;

		}
	}

	public RTLValueBuilder setBit(BitValue aBit) {
		fBit = aBit;
		return this;
	}

	public BitValue getBit() {
		return fBit;
	}

	public RTLType getType() {
		return fType;
	}

	public ZDB getZDB() {
		return fZDB;
	}

	public SourceLocation getLocation() {
		return fLocation;
	}

	public static RTLValue generateUValue(RTLType aType, SourceLocation aLocation, ZDB aZDB) throws ZamiaException {
		RTLValueBuilder b = new RTLValueBuilder(aType, aLocation, aZDB);

		switch (aType.getCat()) {

		case ARRAY:
			
			for (int i = aType.getArrayLow(); i<=aType.getArrayHigh(); i++) {
				b.set(i, generateUValue(aType.getArrayElementType(), aLocation, aZDB), aLocation);
			}
			
			break;
		case BIT:
			b.setBit(BitValue.BV_U);
			break;
		case RECORD:
			// FIXME: implement
			throw new ZamiaException("Sorry, not implemented yet.");
		}

		return b.buildValue();
	}

	public static RTLValue generateValue(RTLType aType, String aValue, SourceLocation aLocation, ZDB aZDB) throws ZamiaException {

		RTLValueBuilder b = new RTLValueBuilder(aType, aLocation, aZDB);

		deSerialize(b, aValue, 0);

		return b.buildValue();
	}

	private static int deSerialize(RTLValueBuilder aB, String aValue, int aI) throws ZamiaException {

		RTLType t = aB.getType();

		switch (t.getCat()) {

		case BIT:
			char c = aValue.charAt(aI);

			switch (c) {
			case '0':
				aB.setBit(BitValue.BV_0);
				return 1;
			case '1':
				aB.setBit(BitValue.BV_1);
				return 1;
			case 'X':
				aB.setBit(BitValue.BV_X);
				return 1;
			case 'U':
				aB.setBit(BitValue.BV_U);
				return 1;
			case 'Z':
				aB.setBit(BitValue.BV_Z);
				return 1;
			default:
				throw new ZamiaException("RTLValueBuilder: Illegal bit literal: " + c);
			}
		default:
			throw new ZamiaException("RTLValueBuilder: sorry, not implemented yet.");
		}

	}

	public static RTLValue generateBit(RTLType aType, BitValue aBit, SourceLocation aLocation, ZDB aZDB) throws ZamiaException {
		RTLValueBuilder b = new RTLValueBuilder(aType, aLocation, aZDB);

		b.setBit(aBit);

		return b.buildValue();
	}

	/************************************************
	 * 
	 * Arrays
	 * 
	 ************************************************/

	public void set(int aIdx, RTLValueBuilder aBuilder, SourceLocation aLocation) throws ZamiaException {
		if (fType.getCat() != TypeCat.ARRAY) {
			throw new ZamiaException("RTLValueBuilder: set(): this is not an array.", aLocation);
		}

		fArrayValues.set(aIdx - fArrayOffset, new BuilderEntry(aBuilder));
	}

	public void set(int aIdx, RTLValue aConstant, SourceLocation aLocation) throws ZamiaException {
		if (fType.getCat() != TypeCat.ARRAY) {
			throw new ZamiaException("RTLValueBuilder: set(): this is not an array.", aLocation);
		}

		if (fArrayValues == null) {
			throw new ZamiaException("RTLValueBuilder: getBuilder(): this is an unconstrained array.", aLocation);
		}

		fArrayValues.set(aIdx - fArrayOffset, new BuilderEntry(aConstant));
	}

	public RTLValue get(int aIdx, SourceLocation aLocation) throws ZamiaException {
		if (fType.getCat() != TypeCat.ARRAY) {
			throw new ZamiaException("RTLValueBuilder: get(): this is not an array.", aLocation);
		}

		BuilderEntry entry = fArrayValues.get(aIdx - fArrayOffset);

		if (entry == null) {
			throw new ZamiaException("RTLValueBuilder: get(): Element " + aIdx + " not set.", aLocation);
		}

		return entry.getConstant();
	}

}
