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
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public abstract class IGContainerItem extends IGItem {

	protected String fId;

	public IGContainerItem(String aId, SourceLocation aSrc, ZDB aZDB) {
		super(aSrc, aZDB);
		fId = aId;
	}


	public void setId(String aId) {
		if (fId != null && !fId.equals(aId)) {
			System.out.println ("About to rename "+fId+" to "+aId);
		}
		fId = aId;
	}

	public String getId() {
		return fId;
	}

}
