/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILLabel;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * An endless loop.
 * 
 * @author Guenter Bartsch
 *
 */
public class ZILSequentialLoop extends ZILSequentialStatement {

	private ZILSequenceOfStatements fStmtSequence;

	public ZILSequentialLoop(ZILSequenceOfStatements aStmtSequence, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aContainer, aSrc);
		fStmtSequence = aStmtSequence;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "loop {");
		fStmtSequence.dump(aIndent+1);
		logger.debug(aIndent, "}");
	}

	@Override
	public String toString() {
		return "SequentialLoop ()";
	}

	@Override
	public void generateCode(ZILInterpreterCode code_, RTLCache cache_, ZILLabel loopExitLabel_) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException ("Not implemented method called.");
	}

	@Override
	protected void inlineSubprograms(VariableRemapping vr_, ZILSequenceOfStatements sos_, RTLCache cache_, String returnVarName_) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException ("Not implemented method called.");
	}

	@Override
	public boolean isSynthesizable() throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException ("Not implemented method called.");
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		fStmtSequence.computeReadSignals(aReadSignals);
	}

}
