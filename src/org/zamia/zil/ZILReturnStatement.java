/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 28, 2008
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILLabel;
import org.zamia.zil.interpreter.ZILReturnStmt;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 *
 */
public class ZILReturnStatement extends ZILSequentialStatement {

	private ZILOperation fExp;

	public ZILReturnStatement(ZILOperation aExp, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aContainer, aSrc);
		fExp = aExp;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "return %s;", fExp);
	}

	@Override
	public String toString() {
		return "return (exp="+fExp+")";
	}

	@Override
	public void generateCode(ZILInterpreterCode code_, RTLCache cache_, ZILLabel loopExitLabel_) throws ZamiaException {
		if (fExp != null)
			fExp.generateCode(code_, cache_);
		
		code_.add(new ZILReturnStmt(getSrc()));
	}

	@Override
	protected void inlineSubprograms(VariableRemapping vr_, ZILSequenceOfStatements sos_, RTLCache cache_, String returnVarName_) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException ("Not implemented method called.");
//		Target target = new Target (new Name (returnVarName_, null, location), null, location);
//		SequentialVariableAssignment sva = new SequentialVariableAssignment(target, exp.inlineSubprograms(vr_, sos_, cache_), null, location);
//		sos_.add(sva);
	}
	@Override
	public boolean isSynthesizable() throws ZamiaException {
		return true;
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		fExp.computeReadSignals(aReadSignals);
	}




}
