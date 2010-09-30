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
public class IGPackageImport extends IGContainerItem {
	
	private String fLibId;
	private String fItemId;
	private boolean fAll;

	public IGPackageImport(String aLibId, String aPkgId, String aItemId, boolean aAll, SourceLocation aSrc, ZDB aZDB) {
		super (aPkgId, aSrc, aZDB);
		fLibId = aLibId;
		fItemId = aItemId;
		fAll = aAll;
	}

	public String getLibId() {
		return fLibId;
	}

	public String getItemId() {
		return fItemId;
	}

	public boolean isAll() {
		return fAll;
	}

	@Override
	public IGItem getChild(int aIdx) {
		return null;
	}

	@Override
	public int getNumChildren() {
		return 0;
	}

	@Override
	public String toString() {
		if (fAll) {
			return "USE "+fLibId+"."+fId+".ALL";
		}
		return "USE "+fLibId+"."+fId+"."+fItemId;
	}
}
