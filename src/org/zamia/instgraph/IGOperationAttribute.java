/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 12, 2009
 */
package org.zamia.instgraph;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.instgraph.interpreter.IGAttributeStmt;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGPushStmt;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGOperationAttribute extends IGOperation {

	public enum AttrOp {
		LEFT, RIGHT, LOW, HIGH, ASCENDING, IMAGE, VALUE, POS, VAL, SUCC, PRED, LEFTOF, RIGHTOF, BASE, DELAYED, STABLE, QUIET, TRANSACTION, EVENT, ACTIVE, LAST_EVENT, LAST_ACTIVE, LAST_VALUE, DRIVING, DRIVING_VALUE, SIMPLE_NAME, PATH_NAME, INSTANCE_NAME, RANGE, REVERSE_RANGE, LENGTH
	}

	private AttrOp fAttrOp;

	private IGOperation fOp;

	private IGItem fItem;

	public IGOperationAttribute(AttrOp aAttrOp, IGItem aItem, IGOperation aOp, IGType aType, SourceLocation aSrc, ZDB aZDB) {
		super(aType, aSrc, aZDB);
		fItem = aItem;
		fOp = aOp;
		fAttrOp = aAttrOp;
	}

	@Override
	public void generateCode(boolean aFromInside, IGInterpreterCode aCode) throws ZamiaException {

		if (fOp != null) {
			fOp.generateCode(aFromInside, aCode);
		}

		if (fItem instanceof IGType) {
			aCode.add(new IGPushStmt((IGType) fItem, computeSourceLocation(), getZDB()));
		} else if (fItem instanceof IGOperation) {
			((IGOperation) fItem).generateCode(aFromInside, aCode);
		} else if (fItem instanceof IGObject) {
			aCode.add(new IGPushStmt((IGObject) fItem, computeSourceLocation(), getZDB()));
		}

		aCode.add(new IGAttributeStmt(getType(), fAttrOp, fOp != null, computeSourceLocation(), getZDB()));

	}

	@Override
	public int getNumOperands() {
		return fOp != null ? 1 : 0;
	}

	@Override
	public IGOperation getOperand(int aIdx) {
		return fOp;
	}

	public AttrOp getAttrOp() {
		return fAttrOp;
	}

	public void setAttrOp(AttrOp aAttrOp) {
		fAttrOp = aAttrOp;
	}

	public IGOperation getOp() {
		return fOp;
	}

	public void setOp(IGOperation aOp) {
		fOp = aOp;
	}

	@Override
	public String toString() {
		return fItem.toString() + "'" + fAttrOp;
	}

	@Override
	public void generateCodeRef(boolean aFromInside, boolean aCheckDirection, IGInterpreterCode aCode) throws ZamiaException {
		throw new ZamiaException("Cannot have attribute expression on the left side of an expression.", computeSourceLocation());
	}

	@Override
	public OIDir getDirection() throws ZamiaException {
		//		
		//		if (fItem instanceof IGOperation) {
		//			return ((IGOperation) fItem).getDirection();
		//		}
		return OIDir.NONE;
	}

	@Override
	public void computeAccessedItems(boolean aLeftSide, IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {
		if (fItem != null) {
			addItemAccess(fItem, AccessType.Read, aDepth, aFilterItem, aFilterType, aAccessedItems);
		}
		if (fOp != null) {
			fOp.computeAccessedItems(false, aFilterItem, aFilterType, aDepth, aAccessedItems);
		}
	}
}
