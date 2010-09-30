/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jun 18, 2009
 */
package org.zamia.instgraph;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.instgraph.interpreter.IGAllocateOpStmt;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGOperationAllocate extends IGOperation {

	private IGOperation fInitialValue;

	public IGOperationAllocate(IGOperation aInitialValue, IGType aType, SourceLocation aSrc, ZDB aZDB) {
		super(aType, aSrc, aZDB);
		
		fInitialValue = aInitialValue;
	}

	@Override
	public void computeAccessedItems(boolean aLeftSide, IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {
		if (fInitialValue != null) {
			fInitialValue.computeAccessedItems(false, aFilterItem, aFilterType, aDepth, aAccessedItems);
		}
	}

	@Override
	public void generateCode(boolean aFromInside, IGInterpreterCode aCode) throws ZamiaException {
		
		if (fInitialValue != null) {
			fInitialValue.generateCode(aFromInside, aCode);
		}
		
		aCode.add(new IGAllocateOpStmt(fInitialValue != null, getType(), computeSourceLocation(), getZDB()));
		
	}

	@Override
	public void generateCodeRef(boolean aFromInside, boolean aCheckDirection, IGInterpreterCode aCode) throws ZamiaException {
		// FIXME
		throw new ZamiaException("Sorry, not implemented yet.");
	}

	@Override
	public OIDir getDirection() throws ZamiaException {
		return null;
	}

	@Override
	public int getNumOperands() {
		return 0;
	}

	@Override
	public IGOperation getOperand(int aIdx) {
		return null;
	}

	@Override
	public String toString() {
		return "IGOperationAllocate(t=" + getType() + ")";
	}

}
