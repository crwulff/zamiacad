/*
 * Copyright 2007,2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Aug 6, 2007
 */

package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.util.HashMapArray;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.vhdl.ast.Range;


/**
 * 
 * @author Guenter Bartsch
 *
 */
@SuppressWarnings("serial")
public class ZILTypeRecord extends ZILType {

	private HashMapArray<String, ZILRecordField> fields;

	public ZILTypeRecord(ZILTypeDeclaration aDeclaration, ZILIContainer aContainer, ASTObject aSrc) {
		super(aDeclaration, aContainer, aSrc);

		fields = new HashMapArray<String, ZILRecordField>();
	}

	public void addRecordField(String aId, ZILType aType) {
		fields.put(aId, new ZILRecordField(aId, aType));
	}

	public ZILRecordField findRecordField(String aId, ASTObject aSrc) {
		ZILRecordField rf = fields.get(aId);
		return rf;
	}

	public int getNumRecordFields() {
		return fields.size();
	}

	public ZILRecordField getRecordField(int aIdx) {
		return fields.get(aIdx);
	}

	@Override
	protected boolean computeIsCompatible(ZILType aType) {

		if (aType == this)
			return true;
		if (aType == null)
			return false;

		if (!(aType instanceof ZILTypeRecord))
			return false;

		ZILTypeRecord rt = (ZILTypeRecord) aType;

		int n = getNumRecordFields();

		if (rt.getNumRecordFields() != n)
			return false;

		for (int i = 0; i < n; i++) {

			ZILRecordField rf = rt.getRecordField(i);
			ZILRecordField rf2 = getRecordField(i);

			if (!rf.id.equals(rf2.id))
				return false;
			if (!rf.type.isCompatible(rf2.type))
				return false;

		}

		return true;
	}

	@Override
	public ZILType createSubtype(ZILRange aRange, ZILTypeDeclaration aDeclaration, ZILIContainer aContainer, ASTObject aSrc) throws ZamiaException {
		throw new ZamiaException("Cannot create subtypes from record type.", aSrc);
	}

	public ZILRecordField getRecordField(String aId) {
		return fields.get(aId);
	}

	@Override
	protected ZILType computeEnableType() {

		ZILTypeRecord et = new ZILTypeRecord(null, null, null);

		int n = getNumRecordFields();

		for (int i = 0; i < n; i++) {

			ZILRecordField rf = getRecordField(i);

			et.addRecordField(rf.id, rf.type.getEnableType());

		}

		return et;
	}

	public String toString() {
		StringBuilder buf = new StringBuilder("RecordType{");

		int n = getNumRecordFields();

		for (int i = 0; i < n; i++) {

			ZILRecordField rf = getRecordField(i);

			buf.append(rf.id + ": " + rf.type);

			if (i < (n - 1))
				buf.append(", ");
		}

		buf.append("}");

		return buf.toString();
	}

	@Override
	public ZILType convertType(ZILType aType, ZILIContainer aContainer, ASTObject aSrc) throws ZamiaException {
		if (aType == this)
			return this;
		throw new ZamiaException("Type conversion for record types is not allowed.", aSrc);
	}

	@Override
	public boolean isAtomic() {
		return false;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

}
