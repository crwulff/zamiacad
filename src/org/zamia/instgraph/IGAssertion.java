/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on May 24, 2009
 */
package org.zamia.instgraph;

import org.zamia.SourceLocation;
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGAssertion extends IGItem {

	private IGOperation fOp, fReport, fSeverity;

	public IGAssertion(IGOperation aOp, IGOperation aReport, IGOperation aSeverity, SourceLocation aSrc, ZDB aZDB) {
		super(aSrc, aZDB);
		fOp = aOp;
		fReport = aReport;
		fSeverity = aSeverity;
	}

	public void dump(int indent) {
		logger.debug(indent, "%s", toString());
	}

	@Override
	public String toString() {
		return "Assertion (op=" + fOp + ", report=" + fReport + ", severity=" + fSeverity + ")";
	}

	public IGOperation getOp() {
		return fOp;
	}

	public IGOperation getReport() {
		return fReport;
	}

	public IGOperation getSeverity() {
		return fSeverity;
	}

	public void computeAccessedItems(IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {
		if (fOp != null) {
			fOp.computeAccessedItems(false, aFilterItem, aFilterType, aDepth, aAccessedItems);
		}
		if (fReport != null) {
			fReport.computeAccessedItems(false, aFilterItem, aFilterType, aDepth, aAccessedItems);
		}
		if (fSeverity != null) {
			fSeverity.computeAccessedItems(false, aFilterItem, aFilterType, aDepth, aAccessedItems);
		}
	}

	@Override
	public int getNumChildren() {
		return 3;
	}

	@Override
	public IGItem getChild(int aIdx) {
		switch (aIdx) {
		case 0:
			return fOp;
		case 1:
			return fReport;
		case 2:
			return fSeverity;
		}
		return null;
	}

}
