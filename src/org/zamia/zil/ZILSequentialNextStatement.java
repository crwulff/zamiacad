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
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILLabel;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class ZILSequentialNextStatement extends ZILSequentialStatement {

	private String fLabel;

	private ZILOperation fCond;

	public ZILSequentialNextStatement(ZILOperation aCond, String aLabel, ZILIContainer aContainer, ASTObject aSrc) {
		super(aContainer, aSrc);
		fCond = aCond;
		fLabel = aLabel;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		return "SequentialNextStatement (cond=" + fCond + ", label=" + fLabel + ")";
	}

	@Override
	public void generateCode(ZILInterpreterCode code_, RTLCache cache_, ZILLabel loopExitLabel_) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");
		//		if (exp != null) {
		//		exp.generateCode(code_, cache_, getBitType(cache_));
		//	} else {
		//		code_.add(new PushStmt(Value.getBit(Value.BIT_1), this));
		//	}
		//	// FIXME: use jump instruction instead
		//	code_.add(new NextStmt(this));
	}

	@Override
	protected void inlineSubprograms(VariableRemapping vr_, ZILSequenceOfStatements sos_, RTLCache cache_, String returnVarName_) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");
		//		sos_.add(new NextStatement(label, exp != null ? exp.inlineSubprograms(vr_, sos_, cache_): null, sos_, location));
	}

	@Override
	public boolean isSynthesizable() throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		if (fCond!= null)
			fCond.computeReadSignals(aReadSignals);
	}

}
