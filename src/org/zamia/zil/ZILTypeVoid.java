/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 20, 2008
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.vhdl.ast.Range;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class ZILTypeVoid extends ZILType {

	private static ZILTypeVoid fInstance = null;
	
	private ZILTypeVoid() {
		super (null, null, null);
	}
	
	public static ZILTypeVoid getInstance() {
		if (fInstance == null) {
			fInstance = new ZILTypeVoid();
		}
		return fInstance;
	}
	
	@Override
	protected ZILType computeEnableType() {
		return this;
	}

	@Override
	protected boolean computeIsCompatible(ZILType aType) {
		return false;
	}

	@Override
	public ZILType convertType(ZILType aType, ZILIContainer aContainer, ASTObject aSrc) throws ZamiaException {
		throw new ZamiaException ("Tried to convert a void type.");
	}

	@Override
	public ZILType createSubtype(ZILRange aRange, ZILTypeDeclaration aDeclaration, ZILIContainer aContainer, ASTObject aSrc) throws ZamiaException {
		throw new ZamiaException ("Tried to subtype a void type.");
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "void");
	}
	
	@Override
	public String toString() {
		return "void";
	}

}
