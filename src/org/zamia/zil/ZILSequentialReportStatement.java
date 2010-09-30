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
public class ZILSequentialReportStatement extends ZILSequentialStatement {

	private ZILOperation fSeverity;

	private ZILOperation fExp;

	public ZILSequentialReportStatement(ZILOperation aExp, ZILOperation aSeverity, ZILIContainer aContainer, ASTObject aSrc) {
		super(aContainer, aSrc);
		fExp = aExp;
		fSeverity = aSeverity;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		return "SequentialReportStatement(exp=" + fExp + ", severity=" + fSeverity + ")";
	}

	@Override
	public void generateCode(ZILInterpreterCode code_, RTLCache cache_, ZILLabel loopExitLabel_) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Internal Error: unimplemented method called on " + this);
	}

	@Override
	protected void inlineSubprograms(VariableRemapping vr_, ZILSequenceOfStatements sos_, RTLCache cache_, String returnVarName_) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Internal Error: unimplemented method called on " + this);
	}

	@Override
	public boolean isSynthesizable() throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Internal Error: unimplemented method called on " + this);
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		fExp.computeReadSignals(aReadSignals);
		if (fSeverity != null) {
			fSeverity.computeReadSignals(aReadSignals);
		}
	}
}
