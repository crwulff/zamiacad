/*
 * Copyright 2009 by the authors indicated in the @author tags.
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
import org.zamia.instgraph.interpreter.IGJumpCStmt;
import org.zamia.instgraph.interpreter.IGJumpStmt;
import org.zamia.instgraph.interpreter.IGLabel;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGSequentialExit extends IGSequentialStatement {

	private IGOperation fCond;
	private String fExitLabel;

	public IGSequentialExit(String aExitLabel, IGOperation aCond, String aLabel, SourceLocation aSrc, ZDB aZDB) {
		super(aLabel, aSrc, aZDB);
		fCond = aCond;
		fExitLabel = aExitLabel;
	}

	@Override
	public void computeAccessedItems(IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {
		if (fCond != null) {
			fCond.computeAccessedItems(false, aFilterItem, aFilterType, aDepth, aAccessedItems);
		}
	}

	@Override
	public void generateCode(IGInterpreterCode aCode) throws ZamiaException {
		
		IGLabel label=null;
		
		if (fExitLabel != null) {
			label = aCode.getLoopExitLabel(fExitLabel);
		} else {
			label = aCode.getLoopExitLabel();
		}
		if (label == null) {
			throw new ZamiaException ("Loop exit label not found.");
		}
		
		if (fCond != null) {
			fCond.generateCode(true, aCode);
			aCode.add(new IGJumpCStmt(label, computeSourceLocation(), getZDB()));
		} else {
			aCode.add(new IGJumpStmt(label, computeSourceLocation(), getZDB()));
		}
	}

	@Override
	public IGItem getChild(int aIdx) {
		return fCond;
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

}
