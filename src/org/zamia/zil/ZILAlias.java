/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 9, 2008
 */
package org.zamia.zil;

import org.zamia.vhdl.ast.ASTObject;


/**
 * 
 * @author Guenter Bartsch
 *
 */
public class ZILAlias extends ZILObject {

	private ZILIObject fObj;

	public ZILAlias(String aId, ZILIObject aObj, ZILType aType, ZILIContainer aContainer, ASTObject aSrc) {
		super (aType, aContainer, aSrc);
		fId = aId;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		return "Alias (id="+getId()+", type="+getType()+", obj="+fObj+")";
	}

}
