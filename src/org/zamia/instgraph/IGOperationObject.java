/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 11, 2009
 */
package org.zamia.instgraph;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.instgraph.IGObject.IGObjectCat;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGPushRefStmt;
import org.zamia.instgraph.interpreter.IGPushStmt;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGOperationObject extends IGOperation {

	private long fObjectDBID;

	public IGOperationObject(IGObject aObject, SourceLocation aSrc, ZDB aZDB) {
		super(aObject.getType(), aSrc, aZDB);
		fObjectDBID = save(aObject);
	}

	public IGObject getObject() {
		return (IGObject) getZDB().load(fObjectDBID);
	}

	@Override
	public void generateCode(boolean aFromInside, IGInterpreterCode aCode) throws ZamiaException {
		aCode.add(new IGPushStmt(getObject(), computeSourceLocation(), getZDB()));
	}

	@Override
	public IGObject generateCodeRef(boolean aFromInside, boolean aCheckDirection, IGInterpreterCode aCode) throws ZamiaException {

		IGObject obj = getObject();

		if (aCheckDirection) {
			if (aFromInside) {
				switch (obj.getDirection()) {
				case IN:
					throw new ZamiaException("IN value cannot be driven.", computeSourceLocation());
				case NONE:
					if (obj.getCat() == IGObjectCat.CONSTANT) {
						throw new ZamiaException("Cannot drive constant value", computeSourceLocation());
					}
				}
			} else {
				switch (obj.getDirection()) {
				case OUT:
					throw new ZamiaException("OUT value cannot be driven.", computeSourceLocation());
				}
			}
		}

		aCode.add(new IGPushRefStmt(getObject(), computeSourceLocation(), getZDB()));
		
		return obj;
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
		return "IGOperationObject(" + getObject() + ")";
	}

	@Override
	public OIDir getDirection() throws ZamiaException {
		return getObject().getDirection();
	}

	@Override
	public void computeAccessedItems(boolean aLeftSide, IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {
		AccessType accessType = aLeftSide ? AccessType.Write : AccessType.Read;
		addItemAccess(getObject(), accessType, aDepth, aFilterItem, aFilterType, aAccessedItems);
	}
}
