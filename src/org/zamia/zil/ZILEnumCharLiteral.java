/* 
 * Copyright 2005-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by guenter on 27.05.2005
 */
package org.zamia.zil;

import org.zamia.vhdl.ast.VHDLNode;


/**
 * Just represents one char literal in an enum
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class ZILEnumCharLiteral extends ZILEnumLiteral {

	private char fChar;
	
	public ZILEnumCharLiteral(char aChar, int aOrd, ZILType aType, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aOrd, aType, aContainer, aSrc);
		fChar=aChar;
	}
	
	public char getChar() {
		return fChar;
	}

	@Override
	public String getId() {
		return ""+fChar;
	}
	
	@Override
	public ZILEnumLiteral cloneEL() {
		return new ZILEnumCharLiteral(fChar, getOrd(), getType(), getContainer(), getSrc());
	}
	
	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		return "EnumCharLiteral (c='"+fChar+"', ord="+getOrd()+")";
	}

}