/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.zil.interpreter;

import org.zamia.ZamiaException;
import org.zamia.rtl.sim.PortVarWriter;
import org.zamia.rtl.sim.Simulator;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class ZILPopStmt extends ZILStmt {

	public ZILPopStmt(VHDLNode src_) {
		super(src_);
	}

	@Override
	public String toString() {
		return "POP";
	}

	@Override
	public ReturnStatus execute(Simulator aSim, ZILInterpreterRuntimeEnv aRuntime, ZILInterpreter aInterpreter) throws ZamiaException {
		//System.out.println("executing:   POP");

		ZILValue v = aRuntime.pop().getValue(aSim);

		ZILStackFrame targetSF = aRuntime.pop();
		
		PortVarWriter svw = targetSF.getSignalVarWriter();
		
		svw.setValue(v);
		
		if (svw.isVariable()) {
			svw.execute(aSim, aRuntime);
		}
		
		return ReturnStatus.CONTINUE;
	}

}
