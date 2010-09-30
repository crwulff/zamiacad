/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 31, 2009
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLGraph;
import org.zamia.rtl.RTLSignal;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILPushStmt;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 *
 */
public class ZILOperationReadSignal extends ZILOperation {

	private ZILSignal fSignal;

	public ZILOperationReadSignal(ZILSignal aSignal, ZILIContainer aContainer, ASTObject aSrc) {
		super(aSignal.getType(), aContainer, aSrc);
		fSignal = aSignal;
	}

	@Override
	protected void doElaborate(RTLSignal aResult, Bindings aLastBindings, RTLCache aCache) throws ZamiaException {
		
		RTLSignal signal = fSignal.elaborate(aLastBindings, aCache);
		
		RTLGraph rtlg = aCache.getGraph();
		
		rtlg.sigJoin(signal, aResult, getSrc());
	}

	@Override
	public void generateCode(ZILInterpreterCode aCode, RTLCache aCache) throws ZamiaException {
		
		if (aCache == null) {
			throw new ZamiaException ("Cannot evaluate signals in this context.", getSrc());
		}
		
		RTLSignal signal = fSignal.elaborate(null, aCache);
		aCode.add(new ZILPushStmt(signal, getSrc()));
	}

	@Override
	public ZILClock getClock() throws ZamiaException {
		return null;
	}

	@Override
	public ZILOperation inlineSubprograms(VariableRemapping aVR, ZILSequenceOfStatements aSOS, RTLCache aCache) throws ZamiaException {
		return new ZILOperationReadSignal(fSignal, aSOS.getContainer(), getSrc());
	}

	@Override
	public boolean isConstant() throws ZamiaException {
		return false;
	}

	@Override
	public boolean isSynthesizable() throws ZamiaException {
		return true;
	}

	@Override
	public ZILOperation resolveVariables(Bindings aVBS, ZILSequenceOfStatements aSOS, RTLCache aCache) throws ZamiaException {
		return new ZILOperationReadSignal(fSignal, aSOS.getContainer(), getSrc());
	}

	public void dump(int aIndent) {
		logger.debug (aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		return "ZILOperationReadSignal (signal="+fSignal+")";
	}

	public ZILSignal getSignal() {
		return fSignal;
	}

	@Override
	public int getNumOperands() {
		return 0;
	}

	@Override
	public ZILOperation getOperand(int aIdx) {
		return null;
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		aReadSignals.add(fSignal);
	}
}
