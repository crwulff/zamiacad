/* 
 * Copyright 2005-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by guenter on 27.05.2005
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.vhdl.ast.VHDLNode;



/**
 * Just represents one string literal in an enum
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class ZILEnumIdLiteral extends ZILEnumLiteral {

	public ZILEnumIdLiteral(String aId, int aOrd, ZILType aType, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aOrd, aType, aContainer, aSrc);
		fId = aId;
	}
	
	@Override
	public char getChar() throws ZamiaException {
		throw new ZamiaException ("This is not a char literal.");
	}
	
	@Override
	public ZILEnumLiteral cloneEL() {
		return new ZILEnumIdLiteral(fId, getOrd(), getType(), getContainer(), getSrc());
	}
	
	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		return "EnumLiteral (id="+getId()+", ord="+getOrd()+")";
	}
	

}