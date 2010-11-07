/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 8, 2008
 */
package org.zamia.zil;

import org.zamia.vhdl.ast.VHDLNode;

/**
 * 
 * @author Guenter Bartsch
 *
 */
public class ZILTypeDeclaration extends ZILObject {

	public ZILTypeDeclaration(String aId, ZILType aType, ZILIContainer aContainer, VHDLNode aSrc) {
		super (aId, aType, aContainer, aSrc);
	}
	
//	@Override
//	public void generateCode(ZILInterpreterCode aCode, RTLCache aCache) throws ZamiaException {
//		
//		aCode.add(new ZILPushStmt(fType, getSrc()));
//		
//	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}
	
	@Override
	public String toString() {
		return "type "+getId()+" is "+getType(); 
	}

	private static int cnt = 0;
	
	public static ZILTypeDeclaration generateAnonymousDeclaration(VHDLNode aSrc) {
		int myCnt = cnt++;
		return new ZILTypeDeclaration("#anonymous type "+myCnt+"#", null, null, aSrc);
	}

	public void setType(ZILType aType) {
		fType = aType;
	}

}
