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
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILLabel;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * An endless loop.
 * 
 * @author Guenter Bartsch
 * 
 */
public class ZILSequentialWhile extends ZILSequentialStatement {

	private ZILSequenceOfStatements fStmtSequence;

	private ZILOperation fCond;

	public ZILSequentialWhile(ZILOperation aCond, ZILSequenceOfStatements aStmtSequence, ZILIContainer aContainer, ASTObject aSrc) {
		super(aContainer, aSrc);
		fCond = aCond;
		fStmtSequence = aStmtSequence;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "while (%s) {", fCond);
		fStmtSequence.dump(aIndent + 1);
		logger.debug(aIndent, "}");
	}

	@Override
	public String toString() {
		return "SequentialWhile (cond=" + fCond + ")";
	}

	@Override
	public boolean isSynthesizable() {
		return false;
	}

	@Override
	public Bindings computeBindings(ZILClock clock_, RTLCache cache_, VariableRemapping vr_) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Sorry, SequentialWhile.computeBindings not implemented yet.");
	}

	@Override
	public void generateCode(ZILInterpreterCode code_, RTLCache cache_, ZILLabel loopExitLabel_) throws ZamiaException {
		//
		//		// exit test
		//
		//		Label labelExit = new Label();
		//
		//		Label loopLabel = new Label();
		//		code_.defineLabel(loopLabel);
		//		
		//		cond.generateCode(code_, cache_, SigType.bit);
		//		
		//		code_.add(new ZILJumpNCStmt(labelExit, this));
		//
		//		// generate loop inner body code
		//
		//		body.generateCode(null, code_, cache_, labelExit);
		//
		//		// jump back to the beginning
		//		
		//		code_.add(new JumpStmt(loopLabel, this));
		//
		//		// exit label:
		//		
		//		code_.defineLabel(labelExit);
	}

	@Override
	protected void inlineSubprograms(VariableRemapping vr_, ZILSequenceOfStatements sos_, RTLCache cache_, String returnVarName_) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Internal Error: unimplemented method called on " + this);
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		fCond.computeReadSignals(aReadSignals);
		fStmtSequence.computeReadSignals(aReadSignals);
	}

}
