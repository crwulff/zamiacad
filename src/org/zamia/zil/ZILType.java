/*
 * Copyright 2004-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.zil;

import java.io.Serializable;
import java.math.BigDecimal;

import org.zamia.ZamiaException;
import org.zamia.vhdl.ast.VHDLNode;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public abstract class ZILType extends ZILObject implements Serializable {

	protected ZILTypeDeclaration fDeclaration;

	// for convenience:
	public final static ZILTypeEnum bit;

	public static ZILTypeInteger intType;

	public static ZILType realType;

	static {
		bit = new ZILTypeEnum(null, null, null, null);
		bit.addEnumLiteral('0', null, null);
		bit.addEnumLiteral('1', null, null);

		try {
			ZILValue iLeft = new ZILValue(-2147483648l, (ZILTypeInteger) null, null, null);
			ZILValue iRight = new ZILValue(2147483647l, (ZILTypeInteger) null, null, null);
			ZILValue bAscending = new ZILValue('1', bit, null, null);

			intType = new ZILTypeInteger(new ZILRange(iLeft, iRight, bAscending, null, null), null, null, null, null);

			iLeft = new ZILValue(BigDecimal.valueOf(Double.MIN_VALUE), (ZILTypeReal) null, null, null);
			iRight = new ZILValue(BigDecimal.valueOf(Double.MAX_VALUE), (ZILTypeReal) null, null, null);
			bAscending = new ZILValue('1', bit, null, null);

			realType = new ZILTypeReal(new ZILRange(iLeft, iRight, bAscending, null, null), null, null, null, null);
		} catch (ZamiaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	// debugging:
	private static int counter = 0;
	protected int cnt = 0;

	public ZILType(ZILTypeDeclaration aDeclaration, ZILIContainer aContainer, VHDLNode aSrc) {
		super(null, aContainer, aSrc);
		fType = this; // FIXME: void?
		fDeclaration = aDeclaration;
		cnt = counter++;
	}

	public ZILTypeDeclaration getDeclaration() {
		return fDeclaration;
	}

	// overriden in array type (single element arrays return their element type
	// here)
	// 15.05.2008: this is not the correct way: std_logic_1164.vhdl
	// if std_logic == std_logic_vector then is_x(std_logic) ==
	// is_x(std_logic_vector) which is plain wrong
	protected ZILType getSimplifiedType() {
		return this;
	}

	public final boolean isCompatible(ZILType aSrc) {
		ZILType t1 = getSimplifiedType();
		aSrc = aSrc.getSimplifiedType();
		return t1.computeIsCompatible(aSrc);
	}

	protected abstract boolean computeIsCompatible(ZILType aType);

	// overriden in enum / array
	// true for char enums and arrays of char enums
	public boolean isLogic() {
		return false;
	}

	// overriden in enum
	// true for char enums
	public boolean isBit() {
		return false;
	}

	// overridden in record / array
	public boolean isAtomic() {
		return true;
	}

	public abstract ZILType createSubtype(ZILRange aRange, ZILTypeDeclaration aDeclaration, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException;

	private ZILType enableType = null;

	public ZILType getEnableType() {

		if (enableType == null)
			enableType = computeEnableType();

		return enableType;
	}

	protected abstract ZILType computeEnableType();

	public abstract ZILType convertType(ZILType aExpT, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException;

	// may be overwritten, e.g. in ZILTypeEnum
	public ZILType qualifyType(ZILType aType, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {
		return convertType(aType, aContainer, aSrc);
	}

	// overwritten in SigTypeArray to check for open array types
	public boolean isOpen() {
		return false;
	}

	// overwritten in SigTypeArray, will return element type in case of
	// single-element array
	public ZILType simplify() {
		return this;
	}

	@Override
	public String getId() {
		if (fDeclaration != null) {
			return fDeclaration.getId();
		}

		return "Anonymous type";
	}

}