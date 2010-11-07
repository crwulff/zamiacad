/*
 * Copyright 2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.vhdl.ast.Range;


/**
 * when a name that denotes a type itself is asked for its
 * type, it will return a ZILTypeRef
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class ZILTypeRef extends ZILType {

	private ZILType fRefType;
	
	public ZILTypeRef(ZILType aRefType, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aRefType != null ? aRefType.getDeclaration() : null, aContainer, aSrc);
		fRefType = aRefType;
	}

	@Override
	protected ZILType computeEnableType() {
		return null;
	}

	@Override
	protected boolean computeIsCompatible(ZILType aType) {
		return false;
	}

	@Override
	public ZILType convertType(ZILType aType, ZILIContainer aContainer, VHDLNode aSrc)
			throws ZamiaException {
		return null;
	}

	@Override
	public ZILType createSubtype(ZILRange aRange, ZILTypeDeclaration aDef, ZILIContainer aContainer, VHDLNode aSrc)
			throws ZamiaException {
		throw new ZamiaException ("Cannot create subtypes from type references.", aSrc);
	}

	public ZILType getRefType() {
		return fRefType;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "REF %s", fRefType);
	}
	
	@Override
	public String toString() {
		return "REF " + fRefType;
	}

}
