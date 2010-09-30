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
import org.zamia.zil.interpreter.ZILAssertStmt;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILLabel;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 *
 */
public class ZILSequentialAssert extends ZILSequentialStatement {

	private ZILAssertion fAssertion;
	
	public ZILSequentialAssert(ZILAssertion aAssertion, ZILIContainer aContainer, ASTObject aSrc) {
		super(aContainer, aSrc);
		fAssertion = aAssertion;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		return "SequentialAssert (assertopm="+fAssertion+")";
	}

	@Override
	public Bindings computeBindings(ZILClock aClock, RTLCache aCache, VariableRemapping aVR) throws ZamiaException {
		return null;
	}

	@Override
	public void generateCode(ZILInterpreterCode aCode, RTLCache aCache, ZILLabel aLoopExitLabel) throws ZamiaException {

		fAssertion.getOp().generateCode(aCode, aCache);
		
		ZILOperation report = fAssertion.getReport();
		
		report.generateCode(aCode, aCache);

		aCode.add(new ZILAssertStmt(getSrc()));
	}

	@Override
	protected void inlineSubprograms(VariableRemapping vr_, ZILSequenceOfStatements sos_, RTLCache cache_, String returnVarName_) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException ("Not implemented method called.");
//		sos_.add(new SequentialAssert(assertion.inlineSubprograms(vr_, sos_, cache_, returnVarName_), sos_, location));
	}
	


	@Override
	public boolean isSynthesizable() throws ZamiaException {
		return false;
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		fAssertion.computeReadSignals(aReadSignals);
	}

}
