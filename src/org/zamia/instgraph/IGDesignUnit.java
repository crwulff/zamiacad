/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Mar 22, 2009
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
public abstract class IGDesignUnit extends IGItem {

	private DUUID fDUUID;

	public IGDesignUnit(DUUID aDUUID, SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);
		fDUUID = aDUUID;
	}

	public DUUID getDUUID() {
		return fDUUID;
	}
	
	public abstract IGContainer getContainer();
}
