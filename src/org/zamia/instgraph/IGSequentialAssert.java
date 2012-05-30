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
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.instgraph.interpreter.IGAssertStmt;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class IGSequentialAssert extends IGSequentialStatement {

	private IGOperation fOp, fReport, fSeverity;

	public IGSequentialAssert(IGOperation aOp, IGOperation aReport, IGOperation aSeverity, String aLabel, SourceLocation aSrc, ZDB aZDB) {
		super(aLabel, aSrc, aZDB);
		fOp = aOp;
		fReport = aReport;
		fSeverity = aSeverity;
	}

	public void dump(int indent) {
		logger.debug(indent, "%s", toString());
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
	public void generateCode(IGInterpreterCode aCode) throws ZamiaException {

		fOp.generateCode(true, aCode);

		if (fReport != null) {
			fReport.generateCode(true, aCode);
		}

		if (fSeverity != null) {
			fSeverity.generateCode(true, aCode);
		}

		aCode.add(new IGAssertStmt(fReport != null, fSeverity != null, computeSourceLocation(), getZDB()));

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

	@Override
	public String toString() {
		return "Assertion (op=" + fOp + ", report=" + fReport + ", severity=" + fSeverity + ")";
	}

}
