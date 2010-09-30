/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.instgraph;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGReturnStmt;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGSequentialReturn extends IGSequentialStatement {

	private IGOperation fReturnValue;

	public IGSequentialReturn(IGOperation aReturnValue, String aLabel, SourceLocation aSrc, ZDB aZDB) {
		super(aLabel, aSrc, aZDB);
		fReturnValue = aReturnValue;

	}

	@Override
	public void computeAccessedItems(IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {
		if (fReturnValue != null) {
			fReturnValue.computeAccessedItems(false, aFilterItem, aFilterType, aDepth, aAccessedItems);
		}
	}
	
	@Override
	public void generateCode(IGInterpreterCode aCode) throws ZamiaException {
		
		if (fReturnValue != null) {
			fReturnValue.generateCode(true, aCode);
		}
		
		aCode.add(new IGReturnStmt(computeSourceLocation(), getZDB()));
	}

	@Override
	public IGItem getChild(int aIdx) {
		return fReturnValue;
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

}
