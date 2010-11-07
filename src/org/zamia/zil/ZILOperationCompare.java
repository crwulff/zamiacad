/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 22, 2008
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLGraph;
import org.zamia.rtl.RTLSignal;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.vhdl.ast.OperationCompare.CompareOp;
import org.zamia.zil.interpreter.ZILCompareStmt;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class ZILOperationCompare extends ZILOperation {

	private CompareOp fOp;

	private ZILOperation fA;

	private ZILOperation fB;

	public ZILOperationCompare(CompareOp aOp, ZILOperation aA, ZILOperation aB, ZILType type, ZILIContainer container, VHDLNode src) {
		super(type, container, src);

		fOp = aOp;
		fA = aA;
		fB = aB;
	}

	@Override
	public void generateCode(ZILInterpreterCode aCode, RTLCache aCache) throws ZamiaException {
		fA.generateCode(aCode, aCache);
		if (fB != null) {
			fB.generateCode(aCode, aCache);
		}

		aCode.add(new ZILCompareStmt(fOp, getSrc()));
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		return "OperationCompare (op=" + fOp + ", a=" + fA + ", b=" + fB + ")";
	}

	@Override
	protected void doElaborate(RTLSignal aResult, Bindings aLastBindings, RTLCache aCache) throws ZamiaException {

		RTLGraph rtlg = aCache.getGraph();

		RTLSignal as = fA.elaborate(aLastBindings, aCache);
		RTLSignal bs = fB.elaborate(aLastBindings, aCache);

		RTLSignal res = rtlg.placeComparator(fOp, as, bs, getSrc());

		rtlg.sigJoin(res, aResult, getSrc());
	}

	@Override
	public ZILClock getClock() {
		return null;
	}

	@Override
	public ZILOperation inlineSubprograms(VariableRemapping aVR, ZILSequenceOfStatements aSOS, RTLCache aCache) throws ZamiaException {
		ZILOperation a = fA.inlineSubprograms(aVR, aSOS, aCache);
		ZILOperation b = null;
		if (fB != null)
			b = fB.inlineSubprograms(aVR, aSOS, aCache);

		return new ZILOperationCompare(fOp, a, b, getType(), aSOS.getContainer(), getSrc());
	}

	@Override
	public ZILOperation resolveVariables(Bindings aVR, ZILSequenceOfStatements aSOS, RTLCache aCache) throws ZamiaException {
		ZILOperation a = fA.resolveVariables(aVR, aSOS, aCache);
		ZILOperation b = null;
		if (fB != null)
			b = fB.resolveVariables(aVR, aSOS, aCache);

		return new ZILOperationCompare(fOp, a, b, getType(), aSOS.getContainer(), getSrc());
	}

	@Override
	public boolean isSynthesizable() throws ZamiaException {
		return fA.isSynthesizable() && fB.isSynthesizable();
	}

	@Override
	public boolean isConstant() throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");
	}

	@Override
	public int getNumOperands() {
		return 2;
	}

	@Override
	public ZILOperation getOperand(int aIdx) {
		return aIdx == 0 ? fA : fB;
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		fA.computeReadSignals(aReadSignals);
		fB.computeReadSignals(aReadSignals);
	}

	public ZILOperation getA() {
		return fA;
	}

	public ZILOperation getB() {
		return fB;
	}

}
