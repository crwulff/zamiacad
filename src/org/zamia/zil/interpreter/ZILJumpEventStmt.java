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
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.sim.Simulator;
import org.zamia.vhdl.ast.ASTObject;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class ZILJumpEventStmt extends ZILJumpStmt {

	private RTLSignal fSignal;
	private RTLPort fPort;

	public ZILJumpEventStmt(RTLSignal aSignal, ZILLabel aLabel, ASTObject aSrc) {
		super(aLabel, aSrc);
		fSignal = aSignal;
	}

	@Override
	public ReturnStatus execute(Simulator aSim, ZILInterpreterRuntimeEnv aRuntime, ZILInterpreter aInterpreter) throws ZamiaException {

		RTLPort eventPort = aRuntime.getCurrentEventPort();
		
		if (eventPort == null || eventPort != fPort) {
			return ReturnStatus.CONTINUE;
		}
		
		aRuntime.setPC(adr);
		
		return ReturnStatus.CONTINUE;
	}

	@Override
	public void wire(ZILInterpreter aInterpreter) throws ZamiaException {
		fPort = aInterpreter.connectToSignal(fSignal, getSource());
	}

	@Override
	public String toString() {
		return "JUMP EVENT (signal="+fSignal+") " + adr;
	}
}

