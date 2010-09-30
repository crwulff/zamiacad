/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILNewObjectStmt;
import org.zamia.zil.interpreter.ZILPushRefStmt;

/**
 * @author guenter bartsch
 */

@SuppressWarnings("serial")
public class ZILInterfaceValue extends ZILValue implements ZILInterface {

	private PortDir fDir;

	public ZILInterfaceValue(String aId, ZILType aType, PortDir aDir, ZILValue aV, ZILIContainer aContainer, ASTObject aSrc) {
		super(aId, aType, aContainer, aSrc);
		fDir = aDir;
	}

	public PortDir getDir() {
		return fDir;
	}

	public void generateInterpreterCodeRef(boolean aIsInertial, boolean aHaveDelay, boolean aHaveReject, ZILInterpreterCode aCode, RTLCache aCache) throws ZamiaException {
		aCode.add(new ZILPushRefStmt(this, getSrc()));
	}

	public void generateCreationCode(ZILInterpreterCode aCode) {
		aCode.add(new ZILNewObjectStmt(this, getSrc()));
	}

	@Override
	public String toString() {
		return "InterfaceValue (id="+getId()+", dir="+fDir+")";
	}
	
}
