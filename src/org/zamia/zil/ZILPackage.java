/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 6, 2008
 */
package org.zamia.zil;

import org.zamia.vhdl.ast.VHDLPackage;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class ZILPackage extends ZILDesignUnit {

	public ZILPackage(String aId, VHDLPackage aSrc) {
		super (aId, aSrc);
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "package id=%s {", getId());
		int n = getNumItems();
		for (int i = 0; i<n; i++) {
			ZILIObject item = getItem(i);
			item.dump(aIndent+1);
		}
		logger.debug(aIndent, "}", getId());
	}

	@Override
	public String toString() {
		return "package (id="+getId()+")";
	}

}
