/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jul 11, 2009
 */
package org.zamia.instgraph;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 *
 */
@SuppressWarnings("serial")
public class IGSequentialReport extends IGSequentialStatement {

	
	private IGOperation fMsg;
	private IGOperation fSeverity;

	public IGSequentialReport(IGOperation aMsg, IGOperation aSeverity, String aLabel, SourceLocation aSrc, ZDB aZDB) {
		super(aLabel, aSrc, aZDB);
		fMsg = aMsg;
		fSeverity = aSeverity;
	}


	@Override
	public void computeAccessedItems(IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {
		// TODO Auto-generated method stub

	}

	@Override
	public void generateCode(IGInterpreterCode aCode) throws ZamiaException {
		// TODO Auto-generated method stub

	}

	@Override
	public IGItem getChild(int aIdx) {
		if (aIdx == 0) {
			return fMsg;
		}
		return fSeverity;
	}

	@Override
	public int getNumChildren() {
		return 2;
	}
	
	@Override 
	public String toString() {
		return "IGSequentialReport (msg="+fMsg+", fSeverity="+fSeverity+")";
	}

}
