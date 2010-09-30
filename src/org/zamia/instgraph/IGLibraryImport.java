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
public class IGLibraryImport extends IGContainerItem {
	
	private String fRealId;

	public IGLibraryImport(String aId, String aRealId, SourceLocation aSrc, ZDB aZDB) {
		super (aId, aSrc, aZDB);
		fRealId = aRealId;
	}

	public String getRealId() {
		return fRealId;
	}

	public void setRealId(String aRealId) {
		fRealId = aRealId;
	}

	@Override
	public String toString() {
		return "IGLibraryImport(id="+getId()+", realId="+fRealId+")";
	}

	@Override
	public IGItem getChild(int aIdx) {
		return null;
	}

	@Override
	public int getNumChildren() {
		return 0;
	}
}
