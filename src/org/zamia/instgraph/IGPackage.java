/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 10, 2009
 */
package org.zamia.instgraph;

import org.zamia.SourceLocation;
import org.zamia.vhdl.ast.DUUID;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class IGPackage extends IGDesignUnit {

	private long fContainerDBID;

	private transient IGContainer fContainer = null;

	public IGPackage(DUUID aDUUID, SourceLocation aLocation, ZDB aZDB) {
		super(aDUUID, aLocation, aZDB);

		fContainer = new IGContainer(0, aLocation, aZDB);
		fContainerDBID = aZDB.store(fContainer);
	}

	@Override
	public String toString() {
		return "IGPackage(duuid=" + getDUUID() + ")";
	}

	public IGContainer getContainer() {
		if (fContainer == null) {
			fContainer = (IGContainer) getZDB().load(fContainerDBID);
		}

		return fContainer;
	}

	@Override
	public IGItem getChild(int aIdx) {
		return getContainer();
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

}
