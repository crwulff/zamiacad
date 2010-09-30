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
import org.zamia.util.HashMapArray;
import org.zamia.vhdl.ast.ASTObject;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class ZILTypeEnum extends ZILTypeDiscrete {

	private HashMapArray<String, ZILEnumLiteral> enumLiterals;

	private boolean isCharEnum = false;

	public ZILTypeEnum(ZILTypeEnum aBaseType, ZILTypeDeclaration aDeclaration, ZILIContainer aContainer, ASTObject aSrc) {
		super(new ZILRange(aContainer, aSrc), aBaseType, aDeclaration, aContainer, aSrc);

		enumLiterals = new HashMapArray<String, ZILEnumLiteral>();
	}

	// enum stuff
	public void addEnumLiteral(String aLiteral, ZILIContainer aContainer, ASTObject aSrc) {
		ZILEnumLiteral el = new ZILEnumIdLiteral(aLiteral, enumLiterals.size(), this, aContainer, aSrc);
		addEnumLiteral(el);
	}

	// enum stuff
	public void addEnumLiteral(char aLiteral, ZILIContainer aContainer, ASTObject aSrc) {
		ZILEnumLiteral el = new ZILEnumCharLiteral(aLiteral, enumLiterals.size(), this, aContainer, aSrc);
		addEnumLiteral(el);
	}

	private void addEnumLiteral(ZILEnumLiteral aLiteral) {

		if (aLiteral instanceof ZILEnumCharLiteral)
			isCharEnum = true;

		aLiteral.setOrd(enumLiterals.size());

		enumLiterals.put(aLiteral.getId(), aLiteral);

		setRight(new ZILValue(aLiteral, this, aLiteral.getContainer(), aLiteral.getSrc()));

		if (enumLiterals.size() == 1) {
			setLeft(new ZILValue(aLiteral, this, aLiteral.getContainer(), aLiteral.getSrc()));
		}
	}

	public ZILEnumLiteral findEnumLiteral(String id_) throws ZamiaException {
		ZILEnumLiteral el = enumLiterals.get(id_);
		return el;
	}

	public ZILEnumLiteral findEnumLiteral(char c_) {
		return enumLiterals.get("" + c_);
	}

	public int getNumEnumLiterals() {
		return enumLiterals.size();
	}

	public ZILEnumLiteral getEnumLiteral(int aIdx, ASTObject aSrc) throws ZamiaException {
		if (aIdx >= enumLiterals.size())
			throw new ZamiaException("Enum ord out of bounds: " + aIdx);
		return enumLiterals.get(aIdx);
	}

	@Override
	protected boolean computeIsCompatible(ZILType t2) {

		if (t2 == this)
			return true;

		if (!(t2 instanceof ZILTypeEnum))
			return false;

		if (isBit() && t2.isBit())
			return true;

		ZILTypeEnum et = (ZILTypeEnum) t2;

		int numEnumLiterals = getNumEnumLiterals();
		int numEnumLiterals2 = et.getNumEnumLiterals();

		if (numEnumLiterals2 != numEnumLiterals)
			return false;

		for (int i = 0; i < numEnumLiterals; i++) {
			try {
				ZILEnumLiteral el1 = getEnumLiteral(i, null);
				ZILEnumLiteral el2 = et.getEnumLiteral(i, null);

				if (!el1.getId().equals(el2.getId()))
					return false;
			} catch (ZamiaException e) {
				el.logException(e);
			}
		}

		return true;
	}

	@Override
	protected ZILType computeEnableType() {
		return ZILType.bit;
	}

	@Override
	public ZILType convertType(ZILType aExp, ZILIContainer aContainer, ASTObject aSrc) throws ZamiaException {
		if (aExp == this)
			return this;
		throw new ZamiaException("Type conversion for enum types is not allowed.", aSrc);
	}

	@Override
	public ZILType qualifyType(ZILType aType, ZILIContainer aContainer, ASTObject aSrc) throws ZamiaException {
		if (!(aType instanceof ZILTypeEnum)) {
			throw new ZamiaException("Tried to qualify an enum type as " + aType, aSrc);
		}

		return aType;
	}

	public boolean isLogic() {
		return isCharEnum;
	}

	public boolean isBit() {
		return isCharEnum;
	}

	@Override
	public String toString() {

		StringBuilder buf = new StringBuilder("{");

		int n = enumLiterals.size();
		for (int i = 0; i < n; i++) {
			ZILEnumLiteral el = enumLiterals.get(i);

			buf.append(el.toString());
			if (i < (n - 1))
				buf.append(", ");
		}

		buf.append("}");

		return buf.toString();
	}

	@Override
	public int getOrd(ZILValue aValue, ASTObject aSrc) throws ZamiaException {
		ZILEnumLiteral el = findEnumLiteral(aValue.getEnumLiteral().getId());
		if (el == null)
			throw new ZamiaException("Literal " + aValue + " not found in enum type " + this, aSrc);
		return el.getOrd();
	}

	public void check() {

		int ord = 0;
		int n = enumLiterals.size();
		for (int i = 0; i < n; i++) {

			try {
				ZILEnumLiteral el = getEnumLiteral(i, null);
				if (el.getOrd() != ord) {
					System.out.println("Inconsistent enum!");
				}
			} catch (ZamiaException e) {
				el.logException(e);
			}

			ord++;
		}
	}

	public void dump(int aIndent) {
		logger.debug("%s", toString());
	}

	@Override
	public ZILType createSubtype(ZILRange aRange, ZILTypeDeclaration aDeclaration, ZILIContainer aContainer, ASTObject aSrc) throws ZamiaException {
		ZILTypeEnum et = new ZILTypeEnum(this, aDeclaration, aContainer, aSrc);

		ZILValue left = aRange.getLeft().computeConstant();
		ZILValue right = aRange.getRight().computeConstant();
		ZILValue ascending = aRange.getAscending().computeConstant();

		if (left == null || right == null || ascending == null) {
			throw new ZamiaException("Constant range expected here.", aSrc);
		}

		int iLeft = getOrd(left, aSrc);
		int iRight = getOrd(right, aSrc);

		boolean bAscending = ascending.isLogicOne();

		if (bAscending) {

			for (int i = iLeft; i <= iRight; i++) {
				ZILEnumLiteral l = enumLiterals.get(i);
				et.addEnumLiteral(l);
			}

		} else {

			for (int i = iRight; i >= iLeft; i--) {
				ZILEnumLiteral l = enumLiterals.get(i);
				et.addEnumLiteral(l);
			}

		}

		return et;
	}

}
