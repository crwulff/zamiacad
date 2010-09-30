/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Aug 6, 2007
 */

package org.zamia.zil;

import java.math.BigDecimal;

import org.zamia.ZamiaException;
import org.zamia.util.HashMapArray;
import org.zamia.vhdl.ast.ASTObject;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class ZILTypePhysical extends ZILTypeScalar {

	private HashMapArray<String, ZILValue> fUnits = new HashMapArray<String, ZILValue>(1);

	public ZILTypePhysical(ZILRange aRange, ZILType aBaseTyppe, ZILTypeDeclaration aDeclaration, ZILIContainer aContainer, ASTObject aSrc) {
		super(aRange, aBaseTyppe, aDeclaration, aContainer, aSrc);
	}

	// physical types have units

	public void addUnit(String aId, BigDecimal aScale) throws ZamiaException {
		fUnits.put(aId, new ZILValue(aId, aScale, this, getContainer(), getSrc()));
	}

	public BigDecimal getScale(String aId) throws ZamiaException {
		ZILValue u = fUnits.get(aId);
		if (u == null)
			throw new ZamiaException("Unit " + aId + " unknown for this type.", getSrc().getLocation());

		return u.getReal(getSrc());
	}

	public int getNumUnits() {
		return fUnits.size();
	}

	public ZILValue getUnit(int aIdx) {
		return fUnits.get(aIdx);
	}

	public ZILValue findUnit(String aId) {
		return fUnits.get(aId);
	}

	protected HashMapArray<String, ZILValue> getUnits() {
		return fUnits;
	}

	protected void setUnits(HashMapArray<String, ZILValue> aUnits) {
		fUnits = aUnits;
	}

	@Override
	protected boolean computeIsCompatible(ZILType aType) {

		if (aType == this)
			return true;

		if (aType == null)
			return false;

		ZILType baseType = getBaseType();
		
		if (baseType != null)
			return baseType.isCompatible(aType);

		if (!(aType instanceof ZILTypePhysical))
			return false;

		ZILTypePhysical pt2 = (ZILTypePhysical) aType;
		ZILType bt1 = pt2.getBaseType();

		if (bt1 != aType)
			return isCompatible(bt1);

		return true;

		//		ZILTypePhysical pt1 = this;
		//		
		//		// reduce both to their base types:
		//		
		//		while (pt1.getBaseType() != pt1) {
		//			pt1 = (ZILTypePhysical) pt1.getBaseType();
		//		}
		//		while (pt2.getBaseType() != pt2) {
		//			pt2 = (ZILTypePhysical) pt2.getBaseType();
		//		}
		//		
		//		
		//		
		//		if (baseType == null && bt == pt) {
		//			return this == bt;
		//		}
		//		
		//		if (bt != null && !bt.isCompatible(getBaseType()))
		//			return false;
		//		if (bt == null && baseType != null)
		//			return false;
		//		
		//		return true;
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
		logger.debug("%f %s %f", getLeft(), dirStr, getRight());
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
		ZILTypePhysical tp = new ZILTypePhysical(aRange, this, aDeclaration, aContainer, aSrc);
		
		tp.setUnits(getUnits()); 
		
		return tp;
	}
}
