/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */
package org.zamia.zil.interpreter;

import java.io.Serializable;

import org.zamia.ZamiaException;
import org.zamia.rtl.sim.Simulator;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 *
 */
@SuppressWarnings("serial")
public abstract class ZILStmt implements Serializable {
	
	public enum ReturnStatus {CONTINUE, WAIT, RETURN};
	
	protected VHDLNode fSrc;

	public ZILStmt(VHDLNode aSrc) {
		fSrc = aSrc;
	}

	public abstract ReturnStatus execute(Simulator aSim, ZILInterpreterRuntimeEnv aRuntime, ZILInterpreter aInterpreter)
			throws ZamiaException;

	public VHDLNode getSource() {
		return fSrc;
	}
	
	public void wire (ZILInterpreter aInterpreter) throws ZamiaException {
	}
	
	public void init (Simulator aSimulator) throws ZamiaException {
	}
	
	protected void pushBool(boolean aBool, ZILInterpreterRuntimeEnv aRuntime) {
		ZILValue v = aBool ? ZILValue.getBit(ZILValue.BIT_1, getSource()) : ZILValue.getBit(ZILValue.BIT_0, getSource()); 
		aRuntime.push(v);
	}

}

