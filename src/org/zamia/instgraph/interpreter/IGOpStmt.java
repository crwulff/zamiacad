/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jun 15, 2009
 */
package org.zamia.instgraph.interpreter;

import org.zamia.SourceLocation;
import org.zamia.instgraph.IGType;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public abstract class IGOpStmt extends IGStmt {

	private long fTypeDBID;

	public IGOpStmt(IGType aResultType, SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);
		
		fTypeDBID = save(aResultType);
	}

	protected IGType getType() {
		if (fTypeDBID == 0)
			return null;
		return (IGType) getZDB().load(fTypeDBID);
	}
	
}
