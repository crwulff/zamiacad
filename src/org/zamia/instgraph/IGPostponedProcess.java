package org.zamia.instgraph;

import org.zamia.SourceLocation;
import org.zamia.zdb.ZDB;

public class IGPostponedProcess extends IGProcess {
	
	public IGPostponedProcess(long aParentContainerDBID, String aLabel, SourceLocation aLocation, ZDB aZDB) {
		super(aParentContainerDBID, aLabel, aLocation, aZDB);
	}

	@Override
	public String toString() {
		return "postponed " + super.toString();
	}
}
