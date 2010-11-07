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
public class ZILTypeAccess extends ZILType {

	private ZILType fSubtype;

	public ZILTypeAccess (ZILType aSubtype, ZILTypeDeclaration aDefinition, ZILIContainer aContainer, VHDLNode aSrc) {
		super (aDefinition, aContainer, aSrc);
		fSubtype = aSubtype;
	}

	@Override
	protected boolean computeIsCompatible(ZILType aType) {
		
		if (this == aType)
			return true;
		
		if (!(aType instanceof ZILTypeAccess))
			return false;
		
		ZILTypeAccess ta = (ZILTypeAccess) aType;
		
		return fSubtype.isCompatible(ta.getSubtype());
	}

	public ZILType getSubtype() {
		return fSubtype;
	}

	@Override
	public ZILType createSubtype(ZILRange aRange, ZILTypeDeclaration aDeclaration, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {
		throw new ZamiaException ("Cannot create subtypes from acces type.", aSrc);
	}

	@Override
	protected ZILType computeEnableType() {
		return ZILType.bit;
	}

	@Override
	public ZILType convertType(ZILType aType, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {
		if (aType == this)
			return this;
		throw new ZamiaException ("Type conversion for access types is not allowed.", aSrc);
	}

	@Override
	public ZILType qualifyType(ZILType aType, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {
		if (aType == this)
			return this;
		throw new ZamiaException ("Type qualification for access types is not allowed.", aSrc);
	}

	public void dump(int aIndent) {
		logger.debug("%s", toString());
	}
	
	@Override
	public String toString() {
		return "access "+fSubtype; 
	}
}
