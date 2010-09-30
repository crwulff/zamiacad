/* 
 * Copyright 2007-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by guenter on Feb 26, 2006
 */
package org.zamia.zil;

import org.zamia.vhdl.ast.ASTObject;

/**
 * A type attribute
 * 
 * @author Guenter Bartsch
 */

public class ZILAttribute extends ZILObject {

	private ZILValue fValue;
	
	public ZILAttribute (String aId, ZILValue aValue, ZILType aType, ZILIContainer aContainer, ASTObject aSrc) {
		super (aId, aType, aContainer, aSrc);
		fValue = aValue;
		fId = aId;
	}
	
	public ZILAttribute (String aId, ZILType aType, ZILIContainer aContainer, ASTObject aSrc) {
		this (aId, null, aType, aContainer, aSrc); 
	}
	
	public ZILValue getValue() {
		return fValue;
	}
	
	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		return "Attribute (id='"+getId()+"', type="+getType()+", value="+getValue()+")";
	}

}
