/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Mar 30, 2008
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
public abstract class ZILTypeScalar extends ZILType {

	protected ZILType fBaseType;

	protected ZILRange fRange;

	public ZILTypeScalar(ZILRange aRange, ZILType aBaseType, ZILTypeDeclaration aDeclaration, ZILIContainer aContainer, ASTObject aSrc) {
		super(aDeclaration, aContainer, aSrc);
		
		fRange = aRange;
		fBaseType = aBaseType;
	}

	@Override
	public ZILType convertType(ZILType aExpT, ZILIContainer aContainer, ASTObject aSrc) throws ZamiaException {
		if (!(aExpT instanceof ZILTypeScalar))
			throw new ZamiaException("Type conversion between scalar and non-scalar type is not allowed.", getSrc());
		return aExpT;
	}

	public void setLeft(ZILValue aLeft) {
		fRange.setLeft(aLeft);
	}

	public ZILOperation getLeft() {
		return fRange.getLeft();
	}

	public void setRight(ZILValue aRight) {
		fRange.setRight(aRight);
	}

	public ZILOperation getRight() {
		return fRange.getRight();
	}

	public ZILOperation getAscending() {
		return fRange.getAscending();
	}

	public ZILType getBaseType() {
		return fBaseType;
	}
	
	public ZILRange getRange() {
		return fRange;
	}

	public boolean isAscending() throws ZamiaException {
		return fRange.isAscending();
	}
	
	public ZILValue getHigh() throws ZamiaException {
		if (isAscending()) {
			return getRight().computeConstant();
		}
		return getLeft().computeConstant();
	}
	
	public ZILValue getLow() throws ZamiaException {
		if (isAscending()) {
			return getLeft().computeConstant();
		}
		return getRight().computeConstant();
	}
	
	public ZILRange getReverseRange() throws ZamiaException {
		return new ZILRange (getRight(), getLeft(), !isAscending(), getContainer(), getSrc());
	}

	public int getCardinality() throws ZamiaException {
		return getLength().getInt((ASTObject) null);
	}

	public ZILValue getLength() throws ZamiaException {
		
		int h = getHigh().getInt(getSrc());
		int l = getLow().getInt(getSrc());
		
		return new ZILValue(h-l+1, ZILType.intType, null, null);
	}

}
