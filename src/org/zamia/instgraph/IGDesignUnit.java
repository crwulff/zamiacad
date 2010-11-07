/* 
 * Copyright 2009-2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Mar 22, 2009
 */
package org.zamia.instgraph;

import org.zamia.SourceLocation;
import org.zamia.vhdl.ast.DMUID;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public abstract class IGDesignUnit extends IGItem {

	private DMUID fDUUID;

	public IGDesignUnit(DMUID aDUUID, SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);
		fDUUID = aDUUID;
	}

	public DMUID getDUUID() {
		return fDUUID;
	}
	
	public abstract IGContainer getContainer();
}
