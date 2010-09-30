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
public class ZILTypeInteger extends ZILTypeDiscrete {

	public ZILTypeInteger(ZILRange aRange, ZILTypeInteger aBaseType, ZILTypeDeclaration aDeclaration, ZILIContainer aContainer, ASTObject aSrc) {
		super(aRange, aBaseType, aDeclaration, aContainer, aSrc);
	}

	public ZILTypeInteger getRootType() {
		ZILTypeInteger res = this;

		while (res.getBaseType() != null)
			res = (ZILTypeInteger) res.getBaseType();

		return res;
	}

	@Override
	protected boolean computeIsCompatible(ZILType aType) {

		if (aType == null)
			return false;

		if (!(aType instanceof ZILTypeInteger))
			return false;

		// ZILTypeInteger it = (ZILTypeInteger) t2;
		//		
		// ZILTypeInteger rt1 = getRootType();
		// ZILTypeInteger rt2 = it.getRootType();
		//		
		// if (rt1 != rt2)
		// return false;

		return true;
	}

	@Override
	public String toString() {
		try {
			return isAscending() ? "[" + getLeft() + " TO " + getRight() + "]" : "[" + getLeft() + " DOWNTO " + getRight() + "]";
		} catch (ZamiaException e) {
			e.printStackTrace();
		}
		return "[" + getLeft() + " ??? " + getRight() + "]";
	}

	@Override
	protected ZILType computeEnableType() {
		return ZILType.bit;
	}

	@Override
	public int getOrd(ZILValue aValue, ASTObject aSrc) throws ZamiaException {
		return aValue.getInt(aSrc) - (int) getLow().getInt(aSrc);
	}

	public void dump(int aIndent) {
		logger.debug("%s", toString());
	}

	@Override
	public ZILType createSubtype(ZILRange aRange, ZILTypeDeclaration aDeclaration, ZILIContainer aContainer, ASTObject aSrc) throws ZamiaException {
		return new ZILTypeInteger (aRange, this, aDeclaration, aContainer, aSrc);
	}

}
