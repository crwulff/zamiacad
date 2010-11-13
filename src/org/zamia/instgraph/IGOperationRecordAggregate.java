/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 30, 2008
 */
package org.zamia.instgraph;

import java.io.Serializable;
import java.util.ArrayList;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGRecordAggregateStmt;
import org.zamia.util.HashMapArray;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGOperationRecordAggregate extends IGOperation {

	static class Entry implements Serializable {
		public IGRecordField fRF;

		public IGOperation fObj;

		public Entry(IGRecordField aRF, IGOperation aObj) {
			fRF = aRF;
			fObj = aObj;
		}

		@Override
		public String toString() {
			return "{" + fRF + "=>" + fObj + "}";
		}

	}

	private HashMapArray<IGRecordField, Entry> fEntries;

	public IGOperationRecordAggregate(IGType aType, SourceLocation aSrc, ZDB aZDB) {
		super(aType, aSrc, aZDB);

		fEntries = new HashMapArray<IGRecordField, Entry>();
	}

	public void set(IGRecordField aRF, IGOperation aObj) {
		fEntries.put(aRF, new Entry(aRF, aObj));
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("OperationRecordAggregate (");

		int n = fEntries.size();
		for (int i = 0; i < n; i++) {
			Entry entry = fEntries.get(i);
			buf.append(entry.fRF.getId());
			buf.append(":=");
			buf.append(entry.fObj);
			if (i < (n - 1)) {
				buf.append(", ");
			}
		}
		buf.append(")");
		return buf.toString();
	}

	@Override
	public void generateCode(boolean aFromInside, IGInterpreterCode aCode) throws ZamiaException {
		int nEntries = fEntries.size();

		ArrayList<String> fields = new ArrayList<String>(nEntries);

		for (int i = nEntries - 1; i >= 0; i--) {

			Entry entry = fEntries.get(i);

			entry.fObj.generateCode(aFromInside, aCode);
			fields.add(entry.fRF.getId());
		}

		aCode.add(new IGRecordAggregateStmt(fields, getType(), computeSourceLocation(), getZDB()));
	}

	@Override
	public IGObject generateCodeRef(boolean aFromInside, boolean aCheckDirection, IGInterpreterCode aCode) throws ZamiaException {
		// FIXME
		throw new ZamiaException("IGOperationRecordAggregate: generateCodeRef(): Sorry, not implemented yet.", computeSourceLocation());
	}

	@Override
	public OIDir getDirection() throws ZamiaException {
		return OIDir.NONE;
	}

	@Override
	public int getNumOperands() {
		return fEntries.size();
	}

	@Override
	public IGOperation getOperand(int aIdx) {
		return fEntries.get(aIdx).fObj;
	}

	@Override
	public void computeAccessedItems(boolean aLeftSide, IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {
		int n = fEntries.size();
		for (int i = 0; i < n; i++) {
			Entry entry = fEntries.get(i);
			entry.fObj.computeAccessedItems(aLeftSide, aFilterItem, aFilterType, aDepth, aAccessedItems);
		}
	}
}
