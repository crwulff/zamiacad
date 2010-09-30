/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */

package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.vhdl.ast.ASTObject;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public abstract class ZILTypeDiscrete extends ZILTypeScalar {

	public ZILTypeDiscrete(ZILRange aRange, ZILType aBaseType, ZILTypeDeclaration aDeclaration, ZILIContainer aContainer, ASTObject src_) {
		super(aRange, aBaseType, aDeclaration, aContainer, src_);
	}

	/**
	 * used in array aggregates to determine internal array position of given
	 * value. especially useful for arrays declared over char enumes, e.g.
	 * 
	 * TYPE HiLoStrType IS ARRAY (std_ulogic RANGE 'X' TO '1') OF STRING(1 TO
	 * 4);
	 * 
	 * ord ('X') => 0 ord ('0') => 1 ...
	 * 
	 * @param v_
	 * @param src_
	 *            ref to who is guilty if this goes wrong (error reporting)
	 * @return
	 * @throws ZamiaException
	 */

	public abstract int getOrd(ZILValue v_, ASTObject src_) throws ZamiaException;

}
