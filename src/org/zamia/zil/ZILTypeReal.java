/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Aug 6, 2007
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
public class ZILTypeReal extends ZILTypeScalar {

	public ZILTypeReal(ZILRange aRange, ZILType aBaseType, ZILTypeDeclaration aDeclaration, ZILIContainer aContainer, ASTObject aSrc) {
		super(aRange, aBaseType, aDeclaration, aContainer, aSrc);
	}

	@Override
	protected boolean computeIsCompatible(ZILType aType) {

		if (aType == this)
			return true;

		if (aType == null)
			return false;

		if (!(aType instanceof ZILTypeReal))
			return false;

		ZILTypeReal rt = (ZILTypeReal) aType;
		ZILType bt = rt.getBaseType();

		ZILType baseType = getBaseType();
		
		if (baseType == null && bt == rt)
			return this == bt;

		if (bt != null && !bt.isCompatible(baseType))
			return false;
		if (bt == null && baseType != null)
			return false;

		return true;
	}

	@Override
	protected ZILType computeEnableType() {
		return ZILType.bit;
	}

	public void dump(int aIndent) {
		String dirStr = " ??? ";
		try {
			dirStr = isAscending() ? "to" : "downto";
		} catch (ZamiaException e) {
			e.printStackTrace();
		}
		logger.debug(aIndent, "%f %s %f", getLeft(), dirStr, getRight());
	}
	
	@Override
	public String toString() {
		String dirStr = " ??? ";
		try {
			dirStr = isAscending() ? " to " : " downto ";
		} catch (ZamiaException e) {
			e.printStackTrace();
		}
		return getLeft() + dirStr + getRight(); 
	}

	@Override
	public ZILType createSubtype(ZILRange aRange, ZILTypeDeclaration aDeclaration, ZILIContainer aContainer, ASTObject aSrc) throws ZamiaException {
		return new ZILTypeReal(aRange, this, aDeclaration, aContainer, aSrc);
	}
}
