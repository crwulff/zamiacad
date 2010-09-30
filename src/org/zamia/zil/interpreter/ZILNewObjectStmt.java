/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 24, 2008
 */
package org.zamia.zil.interpreter;

import org.zamia.ZamiaException;
import org.zamia.rtl.sim.Simulator;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.ZILIReferable;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class ZILNewObjectStmt extends ZILStmt {

	private ZILIReferable fObject;

	public ZILNewObjectStmt(ZILIReferable aObject, ASTObject aSrc) {
		super(aSrc);
		fObject = aObject;
	}

	@Override
	public ReturnStatus execute(Simulator aSim, ZILInterpreterRuntimeEnv aRuntime, ZILInterpreter aInterpreter) throws ZamiaException {

		aRuntime.newObject(fObject);
		
		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "NEW OBJECT "+fObject;
	}
}
