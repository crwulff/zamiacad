package org.zamia.instgraph;

import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGLabel;
import org.zamia.instgraph.interpreter.IGJumpStmt;
import org.zamia.ZamiaException;
import org.zamia.SourceLocation;

/**
 * @author Anton Chepurov
 */
public class IGSequentialRestart extends IGSequentialStatement {

	public IGSequentialRestart(String aId, SourceLocation aSrc, ZDB aZDB) {
		super(aId, aSrc, aZDB);
	}

	@Override
	public void computeAccessedItems(IGItem aFilterItem, IGItemAccess.AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {
	}

	@Override
	public void generateCode(IGInterpreterCode aCode) throws ZamiaException {

		// Make process loop (run from WAIT stmt to WAIT stmt)
		IGLabel jumpStartLabel = new IGLabel();
		jumpStartLabel.setAdr(0);

		aCode.add(new IGJumpStmt(jumpStartLabel, computeSourceLocation(), getZDB()));

	}

	@Override
	public int getNumChildren() {
		return 0;
	}

	@Override
	public IGItem getChild(int aIdx) {
		return null;
	}
}