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
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.vhdl.ast.Range;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class ZILTypeFile extends ZILType {

	private ZILType fElementType;

	public ZILTypeFile(ZILType aElementType, ZILTypeDeclaration aDeclaration, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aDeclaration, aContainer, aSrc);
		fElementType = aElementType;
	}

	public ZILType getElementType() {
		return fElementType;
	}

	@Override
	protected boolean computeIsCompatible(ZILType aType) {
		if (aType == this)
			return true;

		if (!(aType instanceof ZILTypeFile))
			return false;

		ZILTypeFile tf = (ZILTypeFile) aType;

		return tf.getElementType().isCompatible(fElementType);
	}

	@Override
	public ZILType createSubtype(ZILRange aRange, ZILTypeDeclaration aDeclaration, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {
		throw new ZamiaException("Cannot create subtypes from file type.", aSrc);
	}

	@Override
	protected ZILType computeEnableType() {
		return ZILType.bit;
	}

	@Override
	public ZILType convertType(ZILType aType, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {
		if (aType == this)
			return this;
		throw new ZamiaException("Type conversion for file types is not allowed.");
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "file of %s", fElementType);
	}
	
	@Override
	public String toString() {
		return "file of "+fElementType; 
	}

}
