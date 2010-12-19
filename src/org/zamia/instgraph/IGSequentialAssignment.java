/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on May 23, 2009
 */
package org.zamia.instgraph;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGPopStmt;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class IGSequentialAssignment extends IGSequentialStatement {

	private IGOperation fValue;

	private IGOperation fTarget;

	private IGOperation fReject;
	
	private boolean fInertial;

	private IGOperation fDelay;

	public IGSequentialAssignment(IGOperation aValue, IGOperation aTarget, boolean aInertial, IGOperation aReject, String aId, SourceLocation aSrc, ZDB aZDB) {
		super(aId, aSrc, aZDB);
		fValue = aValue;
		fTarget = aTarget;
		fReject = aReject;
		fInertial = aInertial;
	}

	public void setDelay(IGOperation aDelay) {
		fDelay = aDelay;
	}

	@Override
	public void computeAccessedItems(IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {

		fValue.computeAccessedItems(false, aFilterItem, aFilterType, aDepth, aAccessedItems);
		fTarget.computeAccessedItems(true, aFilterItem, aFilterType, aDepth, aAccessedItems);

		if (fReject != null) {
			fReject.computeAccessedItems(false, aFilterItem, aFilterType, aDepth, aAccessedItems);
		}

		if (fDelay != null) {
			fDelay.computeAccessedItems(false, aFilterItem, aFilterType, aDepth, aAccessedItems);
		}
	}

	@Override
	public void generateCode(IGInterpreterCode aCode) throws ZamiaException {

		fTarget.generateCode(true, aCode);
		fValue.generateCode(true, aCode);

		if (fDelay != null) {
			fDelay.generateCode(true, aCode);
		}

		if (fReject != null) {
			fReject.generateCode(true, aCode);
		}

		aCode.add(new IGPopStmt(fInertial, fDelay != null, fReject != null, computeSourceLocation(), getZDB()));
	}

	@Override
	public IGItem getChild(int aIdx) {
		switch (aIdx) {
		case 0:
			return fDelay;
		case 1:
			return fReject;
		case 2:
			return fTarget;
		}
		return fValue;
	}

	@Override
	public int getNumChildren() {
		return 4;
	}

	@Override
	public String toString() {
		return fTarget.toString() + " := " + fValue;
	}

	public IGOperation getValue() {
		return fValue;
	}

	public IGOperation getTarget() {
		return fTarget;
	}

	public IGOperation getReject() {
		return fReject;
	}

	public boolean isInertial() {
		return fInertial;
	}

	public IGOperation getDelay() {
		return fDelay;
	}

	@Override
	public void dump(int aIndent) {
		logger.debug(aIndent, "%s := %s [delay=%s, inertial=%s, reject=%s]", fTarget, fValue, fDelay, fInertial, fReject);
	}
}
