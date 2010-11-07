/* 
 * Copyright 2008 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 9, 2008
 */
package org.zamia.zil;

import org.zamia.vhdl.ast.VHDLNode;

/**
 * 
 * @author Guenter Bartsch
 *
 */
public class ZILSharedVariable extends ZILObject {

	private ZILValue fValue;
	
	public ZILSharedVariable (String aId, ZILType aType, ZILIContainer aContainer, VHDLNode aSrc) {
		super (aId, aType, aContainer, aSrc);
	}
	
	public void setInitialValue(ZILValue aValue) {
		fValue = aValue;
	}

	public ZILValue getInitialValue() {
		return fValue;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		return "shared variable (id="+getId()+", type="+getType()+")";
	}

}
