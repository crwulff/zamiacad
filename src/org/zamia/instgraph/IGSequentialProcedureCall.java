/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on May 30, 2009
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
public class IGSequentialProcedureCall extends IGSequentialStatement {

	private IGOperationInvokeSubprogram fInvocation;

	public IGSequentialProcedureCall(IGOperationInvokeSubprogram aInvocation, String aLabel, SourceLocation aSrc, ZDB aZDB) {
		super(aLabel, aSrc, aZDB);
		
		fInvocation = aInvocation;
		
	}

	@Override
	public void computeAccessedItems(IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {
		fInvocation.computeAccessedItems(false, aFilterItem, aFilterType, aDepth, aAccessedItems);
	}

	@Override
	public void generateCode(IGInterpreterCode aCode) throws ZamiaException {
		fInvocation.generateCode(true, aCode);
	}

	@Override
	public IGItem getChild(int aIdx) {
		return fInvocation;
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

}
