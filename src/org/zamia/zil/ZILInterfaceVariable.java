/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.zil;

import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILNewObjectStmt;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class ZILInterfaceVariable extends ZILVariable implements ZILInterface {
	
	private PortDir fDir;

	public ZILInterfaceVariable(String aId, ZILType aType, PortDir aDir, ZILValue aInitialValue, ZILIContainer aContainer, ASTObject aSrc) {
		super(aId, aType, aInitialValue, aContainer, aSrc);
		
		fDir = aDir;
		
	}

	public PortDir getDir() {
		return fDir;
	}


	public boolean isValidTarget() {
		return fDir != PortDir.IN;
	}

	public void generateCreationCode(ZILInterpreterCode aCode) {
		aCode.add(new ZILNewObjectStmt(this, getSrc()));
	}

}
