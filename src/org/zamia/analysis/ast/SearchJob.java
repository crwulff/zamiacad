/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 18, 2009
 */
package org.zamia.analysis.ast;

import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.vhdl.ast.DUUID;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class SearchJob {

	public String fID;

	public DUUID fDUUID;

	public ReferenceSearchResult parent;

	int fDepth;

	public SearchJob(String aID, DUUID aDUUID, int aDepth, ReferenceSearchResult aParent) {
		fID = aID;
		fDUUID = aDUUID;
		parent = aParent;
		fDepth = aDepth;
	}

	@Override
	public boolean equals(Object aObject) {
		if (!(aObject instanceof SearchJob))
			return false;

		return toString().equals(((SearchJob) aObject).toString());
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return "Search Job " + fID + " in module " + fDUUID;
	}
}
