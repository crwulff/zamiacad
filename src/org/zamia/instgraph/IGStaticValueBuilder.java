/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on May 14, 2009
 */
package org.zamia.instgraph;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.zdb.ZDB;


/**
 * Builder class to construct complex (array/record typed), immutable
 * IGActualConstant objects
 * 
 * @author Guenter Bartsch
 * 
 */

public class IGStaticValueBuilder {

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();
	
	class BuilderEntry {
		private IGStaticValue fConstant;

		private IGStaticValueBuilder fBuilder;

		public BuilderEntry(IGStaticValue aConstant) {
			fConstant = aConstant;
			fBuilder = null;
		}

		public BuilderEntry(IGStaticValueBuilder aBuilder) {
			fBuilder = aBuilder;
			fConstant = null;
		}

		public IGStaticValue getConstant() throws ZamiaException {
			if (fBuilder != null)
				return fBuilder.buildConstant();
			return fConstant;
		}

		public IGStaticValueBuilder getConstantBuilder() throws ZamiaException {
			if (fBuilder != null)
				return fBuilder;
			fBuilder = new IGStaticValueBuilder(fConstant, fSrc);
			fConstant = null;
			return fBuilder;
		}
	}

	private SourceLocation fSrc;

	private ArrayList<BuilderEntry> fArrayValues;

	private HashMap<String, IGStaticValueBuilder> fRecordValues;

	private IGTypeStatic fType;

	private int fArrayOffset;

	private String fId;

	private boolean fIsCharLiteral = false;

	private char fCharLiteral = ' ';

	private int fEnumOrd = 0;

	private BigInteger fNum;

	private BigDecimal fReal;

	private File fFile;

	private IGStaticValue fLeft, fRight, fAscending;
	
	private ZDB fZDB;

	public IGStaticValueBuilder(IGTypeStatic aType, String aId, SourceLocation aSrc) throws ZamiaException {
		fId = aId;
		fSrc = aSrc;
		fType = aType;
		fZDB = fType.getZDB();
		
		if (fZDB == null) {
			logger.error("IGStaticValueBuilder: ZDB == null");
		}

		switch (aType.getCat()) {
		case ARRAY:
			IGTypeStatic indexType = aType.getStaticIndexType(aSrc);

			if (!aType.isUnconstrained()) {
				int card = (int) indexType.computeCardinality(aSrc);

				if (card >= 0) {
					fArrayValues = new ArrayList<BuilderEntry>(card);

					for (int i = 0; i < card; i++) {
						fArrayValues.add(null);
					}
				}
				fArrayOffset = (int) indexType.getStaticLow(aSrc).getOrd();
			} else {
				//throw new ZamiaException ("Internal error: Tried to build constant of an unconstrained type!", aSrc);
			}
			break;
		case RECORD:
			fRecordValues = new HashMap<String, IGStaticValueBuilder>();
			break;
		}
	}

	public IGStaticValueBuilder(IGStaticValue aConstant, SourceLocation aSrc) throws ZamiaException {
		fSrc = aSrc;
		fZDB = aConstant.getZDB();
		
		if (fZDB == null) {
			logger.error("IGStaticValueBuilder: ZDB == null");
		}

		setConstant(aConstant);
	}

	public IGStaticValue buildConstant() throws ZamiaException {
		return new IGStaticValue(this);
	}

	public String getId() {
		return fId;
	}

	/************************************************
	 * 
	 * Arrays
	 * 
	 ************************************************/

	public void set(int aIdx, IGStaticValueBuilder aBuilder, SourceLocation aSrc) throws ZamiaException {
		if (!fType.isArray()) {
			throw new ZamiaException("IGActualConstantBuilder: set(): this is not an array.", aSrc);
		}

		fArrayValues.set(aIdx - fArrayOffset, new BuilderEntry(aBuilder));
	}

	public void set(int aIdx, IGStaticValue aConstant, SourceLocation aSrc) throws ZamiaException {
		if (!fType.isArray()) {
			throw new ZamiaException("IGActualConstantBuilder: set(): this is not an array.", aSrc);
		}

		if (fArrayValues == null) {
			throw new ZamiaException("IGActualConstantBuilder: getBuilder(): this is an unconstrained array.", aSrc);
		}
		
		fArrayValues.set(aIdx - fArrayOffset, new BuilderEntry(aConstant));
	}

	public IGStaticValue get(int aIdx, SourceLocation aSrc) throws ZamiaException {
		if (!fType.isArray()) {
			throw new ZamiaException("IGActualConstantBuilder: get(): this is not an array.", aSrc);
		}
		
		if (fArrayValues == null) {
			throw new ZamiaException("IGActualConstantBuilder: getBuilder(): this is an unconstrained array.", aSrc);
		}
		
		// range check
		
		if (aIdx < fArrayOffset || (aIdx-fArrayOffset) > fArrayValues.size()) {
			throw new ZamiaException ("IGActualConstantBuilder: get(): index out of range. Requested element index is "+aIdx+", valid range is "+fArrayOffset+" to "+(fArrayOffset + fArrayValues.size()-1), aSrc);
		}
		
		BuilderEntry entry = fArrayValues.get(aIdx - fArrayOffset);
		
		if (entry == null) {
			throw new ZamiaException ("IGActualConstantBuilder: get(): Element "+aIdx+" not set.", aSrc);
		}

		return entry.getConstant();
	}

	public IGStaticValueBuilder getBuilder(int aIdx, SourceLocation aSrc) throws ZamiaException {
		if (!fType.isArray()) {
			throw new ZamiaException("IGActualConstantBuilder: getBuilder(): this is not an array.", aSrc);
		}
		if (fArrayValues == null) {
			throw new ZamiaException("IGActualConstantBuilder: getBuilder(): this is an unconstrained array.", aSrc);
		}

		// range check
		
		if (aIdx < fArrayOffset || (aIdx-fArrayOffset) > fArrayValues.size()) {
			throw new ZamiaException ("IGActualConstantBuilder: getBuilder(): index out of range. Requested element index is "+aIdx+", valid range is "+fArrayOffset+" to "+(fArrayOffset + fArrayValues.size()-1), aSrc);
		}

		BuilderEntry entry = fArrayValues.get(aIdx - fArrayOffset);
		
		if (entry == null) {
			throw new ZamiaException ("IGActualConstantBuilder: getBuilder(): Element "+aIdx+" not set.", aSrc);
		}

		return entry.getConstantBuilder();
	}

	public IGStaticValueBuilder getBuilder(IGTypeStatic aType, int aMin, int aMax, SourceLocation aSrc) throws ZamiaException {
		if (!fType.isArray()) {
			throw new ZamiaException("IGActualConstantBuilder: getBuilder(): this is not an array.", aSrc);
		}

		IGStaticValueBuilder b = new IGStaticValueBuilder(aType, null, aSrc);
		
		for (int i = aMin; i<= aMax; i++) {
			b.set(i, get(i, aSrc), aSrc);
		}
		return b;
	}

	/************************************************
	 * 
	 * Records
	 * 
	 ************************************************/

	public void set(IGRecordField aField, IGStaticValue aConstant, SourceLocation aSrc) throws ZamiaException {
		if (!fType.isRecord()) {
			throw new ZamiaException("IGActualConstantBuilder: set(): this is not a record.", aSrc);
		}
		String id = aField.getId();
		fRecordValues.put(id, new IGStaticValueBuilder(aConstant, aSrc));
	}

	public void set(IGRecordField aField, IGStaticValueBuilder aBuilder, SourceLocation aSrc) throws ZamiaException {
		if (!fType.isRecord()) {
			throw new ZamiaException("IGActualConstantBuilder: set(): this is not a record.", aSrc);
		}
		String id = aField.getId();
		fRecordValues.put(id, aBuilder);
	}

	public IGStaticValueBuilder getBuilder(IGRecordField aField, SourceLocation aSrc) throws ZamiaException {
		if (!fType.isRecord()) {
			throw new ZamiaException("IGActualConstantBuilder: get(): this is not a record.", aSrc);
		}
		String rfID = aField.getId();
		return fRecordValues.get(rfID);
	}

	public IGStaticValue get(IGRecordField aField, SourceLocation aSrc) throws ZamiaException {
		if (!fType.isRecord()) {
			throw new ZamiaException("IGActualConstantBuilder: get(): this is not a record.", aSrc);
		}
		
		// FIXME: room for optimization here.
		
		String rfID = aField.getId();
		IGStaticValueBuilder fieldBuilder = fRecordValues.get(rfID);
		if (fieldBuilder == null) {
			
			logger.error("IGActualConstantBuilder: no value for field %s (hash=%d)", aField, aField.hashCode());
			
			throw new ZamiaException ("IGActualConstantBuilder: no value for field "+aField, aSrc);
		}
		
		return fieldBuilder.buildConstant();
	}

	public void setId(String aId) {
		fId = aId;
	}

	public int getArrayOffset() {
		return fArrayOffset;
	}

	public int getNumArrayElements() {
		if (fArrayValues == null)
			return 0;
		return fArrayValues.size();
	}

	public IGTypeStatic getType() {
		return fType;
	}

	public boolean isCharLiteral() {
		return fIsCharLiteral;
	}

	public char getCharLiteral() {
		return fCharLiteral;
	}

	public int getEnumOrd() {
		return fEnumOrd;
	}

	public BigInteger getNum() {
		return fNum;
	}

	public BigDecimal getReal() {
		return fReal;
	}

	public File getFile() {
		return fFile;
	}

	public SourceLocation getSrc() {
		return fSrc;
	}

	public IGStaticValue getLeft() {
		return fLeft;
	}

	public IGStaticValue getRight() {
		return fRight;
	}

	public IGStaticValue getAscending() {
		return fAscending;
	}

	public void setConstant(IGStaticValue aConstant) throws ZamiaException {
		fId = aConstant.getId();
		fType = aConstant.getStaticType();
		fIsCharLiteral = aConstant.isCharLiteral();
		fCharLiteral = aConstant.getCharLiteral();
		fEnumOrd = aConstant.getEnumOrd();
		fNum = aConstant.getNum();
		fReal = aConstant.getReal();
		fFile = aConstant.getFile();
		fLeft = aConstant.getLeft();
		fRight = aConstant.getRight();
		fAscending = aConstant.getAscending();

		switch (fType.getCat()) {
		case ARRAY:
			IGTypeStatic indexType = fType.getStaticIndexType(fSrc);

			if (!fType.isUnconstrained()) {
				int card = (int) indexType.computeCardinality(fSrc);
				fArrayOffset = (int) indexType.getStaticLow(fSrc).getOrd();

				if (card > 0) {
					fArrayValues = new ArrayList<BuilderEntry>(card);

					for (int i = 0; i < card; i++) {
						fArrayValues.add(new BuilderEntry(aConstant.getValue(i + fArrayOffset, fSrc)));
					}
				}
			}
			break;

		case RECORD:

			int n = fType.getNumRecordFields(fSrc);
			fRecordValues = new HashMap<String, IGStaticValueBuilder>(n);
			for (int i = 0; i < n; i++) {
				IGRecordField rf = fType.getRecordField(i, fSrc);
				String id = rf.getId();
				fRecordValues.put(id, new IGStaticValueBuilder(aConstant.getRecordFieldValue(id, fSrc), fSrc));
			}
			break;

		}

	}

	public IGStaticValueBuilder setOrd(long aOrd) {
		if (getType().isEnum()) {
			fEnumOrd = (int) aOrd;
		} else {
			fNum = new BigInteger(""+aOrd);
		}
		return this;
	}
	
	public IGStaticValueBuilder setNum(long aNum) {
		fNum = new BigInteger(""+aNum);
		return this;
	}

	public IGStaticValueBuilder setNum(BigInteger aNum) {
		fNum = aNum;
		return this;
	}

	public IGStaticValueBuilder setReal(BigDecimal aReal) {
		fReal = aReal;
		return this;
	}

	public IGStaticValueBuilder setChar(char aC) {
		fCharLiteral = aC;
		fIsCharLiteral = true;
		return this;
	}

	public ZDB getZDB() {
		return fZDB;
	}

	public void setEnumOrd(int aEnumOrd) {
		fEnumOrd = aEnumOrd;
	}

	public IGStaticValueBuilder setLeft(IGStaticValue aValue) {
		fLeft = aValue;
		return this;
	}
	public IGStaticValueBuilder setRight(IGStaticValue aValue) {
		fRight = aValue;
		return this;
	}
	public IGStaticValueBuilder setAscending(IGStaticValue aValue) {
		fAscending = aValue;
		return this;
	}

	public IGStaticValueBuilder setReal(double aD) {
		setReal(new BigDecimal(aD));
		return this;
	}
}
