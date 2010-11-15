/* 
 * Copyright 2008-2010 by the authors indicated in the @author tags. 
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
import org.zamia.instgraph.interpreter.IGArrayAggregateStmt;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGOperationArrayAggregate extends IGOperation {

	static class AggEntry implements Serializable {
		public IGOperation fPosOp;

		public IGOperation fObj;

		public AggEntry(IGOperation aPos, IGOperation aObj) {
			fPosOp = aPos;
			fObj = aObj;
		}
	}

	private ArrayList<AggEntry> fNamedEntries;

	private ArrayList<IGOperation> fPositionalEntries;

	private IGOperation fOthers;

	public IGOperationArrayAggregate(IGType aType, SourceLocation aSrc, ZDB aZDB) {
		super(aType, aSrc, aZDB);
		fNamedEntries = new ArrayList<AggEntry>();
		fPositionalEntries = new ArrayList<IGOperation>();
	}

	public void add(IGOperation aNamedPos, IGOperation aOp) {
		fNamedEntries.add(new AggEntry(aNamedPos, aOp));
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("OperationArrayAggregate(");

		int n = fNamedEntries.size();
		for (int i = 0; i < n; i++) {
			AggEntry assignment = fNamedEntries.get(i);
			buf.append(assignment.fPosOp);
			buf.append("=>");
			buf.append(assignment.fObj);
			if (i < (n - 1)) {
				buf.append(", ");
			}
		}
		buf.append(", others=" + fOthers);
		buf.append(")");
		return buf.toString();
	}

	@Override
	public int getNumOperands() {
		return fNamedEntries.size() + fPositionalEntries.size() + 1;
	}

	@Override
	public IGOperation getOperand(int aIdx) {
		if (aIdx == 0) {
			return fOthers;
		}
		int n = fNamedEntries.size();
		int idx = aIdx - 1;
		if (idx < n)
			return fNamedEntries.get(idx).fObj;
		return fPositionalEntries.get(idx - n);
	}

	public void setOthers(IGOperation aOthers) {
		fOthers = aOthers;
	}

	@Override
	public void generateCode(boolean aFromInside, IGInterpreterCode aCode) throws ZamiaException {

		int nNamedEntries = fNamedEntries.size();

		for (int i = nNamedEntries - 1; i >= 0; i--) {

			AggEntry entry = fNamedEntries.get(i);

			entry.fObj.generateCode(aFromInside, aCode);
			entry.fPosOp.generateCode(aFromInside, aCode);
		}

		int nPositionalEntries = fPositionalEntries.size();
		for (int i = nPositionalEntries - 1; i >= 0; i--) {
			fPositionalEntries.get(i).generateCode(aFromInside, aCode);
		}

		if (fOthers != null) {
			fOthers.generateCode(aFromInside, aCode);
		}

		aCode.add(new IGArrayAggregateStmt(nNamedEntries, nPositionalEntries, fOthers != null, getType(), computeSourceLocation(), getZDB()));
	}

	@Override
	public OIDir getDirection() throws ZamiaException {
		return OIDir.NONE;
	}

	public void add(IGOperation aObj) {
		fPositionalEntries.add(aObj);
	}

	@Override
	public void computeAccessedItems(boolean aLeftSide, IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {

		if (fOthers != null) {
			fOthers.computeAccessedItems(aLeftSide, aFilterItem, aFilterType, aDepth, aAccessedItems);
		}

		int n = fPositionalEntries.size();
		for (int i = 0; i < n; i++) {
			fPositionalEntries.get(i).computeAccessedItems(aLeftSide, aFilterItem, aFilterType, aDepth, aAccessedItems);
		}

		n = fNamedEntries.size();
		for (int i = 0; i < n; i++) {
			AggEntry entry = fNamedEntries.get(i);

			entry.fPosOp.computeAccessedItems(false, aFilterItem, aFilterType, aDepth, aAccessedItems);
			entry.fObj.computeAccessedItems(aLeftSide, aFilterItem, aFilterType, aDepth, aAccessedItems);
		}
	}
}
