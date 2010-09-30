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

	private IGAssertion fAssertion;

	public IGSequentialAssert(IGAssertion aAssertion, String aLabel, SourceLocation aSrc, ZDB aZDB) {
		super(aLabel, aSrc, aZDB);

		fAssertion = aAssertion;
	}

	@Override
	public void computeAccessedItems(IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {
		fAssertion.computeAccessedItems(aFilterItem, aFilterType, aDepth, aAccessedItems);
	}

	@Override
	public void generateCode(IGInterpreterCode aCode) throws ZamiaException {

		IGOperation op = fAssertion.getOp();
		op.generateCode(true, aCode);

		IGOperation report = fAssertion.getReport();
		if (report != null) {
			report.generateCode(true, aCode);
		}

		IGOperation severity = fAssertion.getSeverity();
		if (severity != null) {
			severity.generateCode(true, aCode);
		}

		aCode.add(new IGAssertStmt(report != null, severity != null, computeSourceLocation(), getZDB()));

	}

	@Override
	public IGItem getChild(int aIdx) {
		return fAssertion;
	}

	@Override
	public int getNumChildren() {
		return 1;
	}
	
	@Override
	public String toString() {
		return "ASSERT "+fAssertion;
	}

}
