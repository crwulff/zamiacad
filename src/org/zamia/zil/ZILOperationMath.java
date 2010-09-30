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
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.vhdl.ast.OperationMath.MathOp;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILMathStmt;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class ZILOperationMath extends ZILOperation {

	private MathOp fOp;

	private ZILOperation fA;

	private ZILOperation fB;

	public ZILOperationMath(MathOp aOp, ZILOperation aA, ZILOperation aB, ZILType type, ZILIContainer container, ASTObject src) {
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

		aCode.add(new ZILMathStmt(fOp, getSrc()));
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		return "OperationMath (op=" + fOp + ", a=" + fA + ", b=" + fB + ")";
	}

	@Override
	protected void doElaborate(RTLSignal result_, Bindings aLastBindings, RTLCache cache_) throws ZamiaException {
		RTLGraph rtlg = cache_.getGraph();

		ZILType resType = getType();

		RTLSignal as = fA.elaborate(aLastBindings, cache_);

		RTLSignal bs = null;
		if (fB != null) {
			bs = fB.elaborate(aLastBindings, cache_);

			// resType = resType.computeResultingType(bt, location);
			// as = rtlg.convertSignal(as, resType, this);
			// bs = rtlg.convertSignal(bs, resType, this);
		}

		RTLSignal res = rtlg.placeOperationMath(fOp, as, bs, resType, getSrc());

		rtlg.sigJoin(res, result_, getSrc());
	}


	@Override
	public ZILClock getClock() throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException ("Not implemented method called.");
	}

	@Override
	public ZILOperation inlineSubprograms(VariableRemapping aVR, ZILSequenceOfStatements aSOS, RTLCache aCache) throws ZamiaException {
		ZILOperation a = fA.inlineSubprograms(aVR, aSOS, aCache);
		ZILOperation b = null;
		if (fB != null)
			b = fB.inlineSubprograms(aVR, aSOS, aCache);

		return new ZILOperationMath(fOp, a, b, getType(), aSOS.getContainer(), getSrc());
	}

	@Override
	public boolean isSynthesizable() throws ZamiaException {
		if (!fA.isSynthesizable())
			return false;
		if (fB != null && !fB.isSynthesizable())
			return false;

		return true;
	}

	@Override
	public ZILOperation resolveVariables(Bindings aVBS, ZILSequenceOfStatements aSOS, RTLCache aCache) throws ZamiaException {
		ZILOperation a = fA.resolveVariables(aVBS, aSOS, aCache);
		ZILOperation b = null;
		if (fB != null)
			b = fB.resolveVariables(aVBS, aSOS, aCache);

		return new ZILOperationMath(fOp, a, b, getType(), aSOS.getContainer(), getSrc());
	}
	@Override
	public boolean isConstant() throws ZamiaException {
		
		if (fB != null && !fB.isConstant())
			return false;
		
		return fA.isConstant();
	}

	@Override
	public int getNumOperands() {
		return fB != null ? 2 : 1;
	}

	@Override
	public ZILOperation getOperand(int aIdx) {
		return aIdx == 0 ? fA : fB;
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		fA.computeReadSignals(aReadSignals);
		if (fB != null) {
			fB.computeReadSignals(aReadSignals);
		}
	}

}
