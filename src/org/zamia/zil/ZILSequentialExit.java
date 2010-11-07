/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 12, 2009
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILLabel;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 *
 */
public class ZILSequentialExit extends ZILSequentialStatement {

	private ZILOperation fCond;

	public ZILSequentialExit(ZILOperation aCond, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aContainer, aSrc);
		fCond = aCond;
	}

	public void dump(int indent) {
		logger.debug(indent, "%s", toString());
	}

	@Override
	public String toString() {
		return "SequentialExit (cond="+fCond+")";
	}
	
	@Override
	public boolean isSynthesizable() {
		// FIXME
		return false;
	}
	
	@Override
	public Bindings computeBindings(ZILClock clock_, RTLCache cache_, VariableRemapping vr_) throws ZamiaException {
		// FIXME: implement
		
		throw new ZamiaException ("Sorry, SequentialExit.computeBindings not implemented yet.");
	}
	
	@Override
	public void generateCode(ZILInterpreterCode code_, RTLCache cache_, ZILLabel loopExitLabel_) throws ZamiaException {
		throw new ZamiaException ("Internal error: not implemented method called in "+this);
//		if (loopExitLabel_ == null)
//			throw new ZamiaException ("Exit statement without surrounding loop detected.");
//		code_.add(new JumpStmt(loopExitLabel_, this));
	}
	@Override
	protected void inlineSubprograms(VariableRemapping vr_, ZILSequenceOfStatements sos_, RTLCache cache_, String returnVarName_) throws ZamiaException {
		throw new ZamiaException ("Internal error: not implemented method called in "+this);
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		if (fCond != null) {
			fCond.computeReadSignals(aReadSignals);
		}
	}


}
