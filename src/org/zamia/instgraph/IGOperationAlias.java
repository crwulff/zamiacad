/* 
 * Copyright 2009,2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jun 7, 2009
 */
package org.zamia.instgraph;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.instgraph.IGType.TypeCat;
import org.zamia.instgraph.interpreter.IGAliasOpStmt;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class IGOperationAlias extends IGOperation {

	private IGOperation fOp;

	public IGOperationAlias(IGOperation aOp, IGType aType, String aId, SourceLocation aLocation, ZDB aZDB) {
		super(aType != null ? aType : aOp.getType(), aLocation, aZDB);
		setId(aId);
		fOp = aOp;
	}

	@Override
	public String toString() {
		return "IGAlias(id=" + getId() + ", op=" + fOp + ")";
	}

	@Override
	public void computeAccessedItems(boolean aLeftSide, IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {
		fOp.computeAccessedItems(aLeftSide, aFilterItem, aFilterType, aDepth, aAccessedItems);
	}

	@Override
	public void generateCode(boolean aFromInside, IGInterpreterCode aCode) throws ZamiaException {
		fOp.generateCode(aFromInside, aCode);

		IGType t = getType();
		if (t.getCat() == TypeCat.ARRAY) {
			aCode.add(new IGAliasOpStmt(t, computeSourceLocation(), getZDB()));
		}
	}

	@Override
	public OIDir getDirection() throws ZamiaException {
		return fOp.getDirection();
	}

	@Override
	public int getNumOperands() {
		return 1;
	}

	@Override
	public IGOperation getOperand(int aIdx) {
		return fOp;
	}

}
