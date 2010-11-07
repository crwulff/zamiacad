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
import org.zamia.vhdl.ast.OperationLogic.LogicOp;
import org.zamia.zil.ZILOperationSignalAttribute.SAOp;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILLogicStmt;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class ZILOperationLogic extends ZILOperation {

	private LogicOp fOp;

	private ZILOperation fA;

	private ZILOperation fB;

	public ZILOperationLogic(LogicOp aOp, ZILOperation aA, ZILOperation aB, ZILType type, ZILIContainer container, VHDLNode src) {
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

		aCode.add(new ZILLogicStmt(fOp, getSrc()));
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		return "OperationLogic (op=" + fOp + ", a=" + fA + ", b=" + fB + ")";
	}

	@Override
	protected void doElaborate(RTLSignal result_, Bindings aLastBindings, RTLCache cache_) throws ZamiaException {

		RTLGraph rtlg = cache_.getGraph();

		RTLSignal res = null;

		RTLSignal as = fA.elaborate(aLastBindings, cache_);

		RTLSignal bs = null;
		if (fB != null) {
			bs = fB.elaborate(aLastBindings, cache_);
		}

		res = rtlg.placeOperationLogic(fOp, as, bs, getSrc());

		rtlg.sigJoin(res.getCurrent(), result_, getSrc());
	}

	@Override
	public ZILClock getClock() throws ZamiaException {

		if (fOp != LogicOp.AND)
			return null;

		ZILOperationSignalAttribute zosa = null;
		ZILOperationCompare zoc = null;
		ZILOperationReadSignal zors = null;
		ZILValue v = null;

		if ((fA instanceof ZILOperationSignalAttribute) && (fB instanceof ZILOperationCompare)) {

			zosa = (ZILOperationSignalAttribute) fA;

			zoc = (ZILOperationCompare) fB;

			ZILOperation fA1 = zoc.getA();
			ZILOperation fB1 = zoc.getB();

			if (fA1 instanceof ZILOperationReadSignal && fB1 instanceof ZILValue) {
				zors = (ZILOperationReadSignal) fA1;
				v = (ZILValue) fB1;
			}
			if (fB1 instanceof ZILOperationReadSignal && fA1 instanceof ZILValue) {
				zors = (ZILOperationReadSignal) fB1;
				v = (ZILValue) fA1;
			}
		} else if ((fB instanceof ZILOperationSignalAttribute) && (fA instanceof ZILOperationCompare)) {

			zosa = (ZILOperationSignalAttribute) fB;

			zoc = (ZILOperationCompare) fA;

			ZILOperation fA1 = zoc.getA();
			ZILOperation fB1 = zoc.getB();

			if (fA1 instanceof ZILOperationReadSignal && fB1 instanceof ZILValue) {
				zors = (ZILOperationReadSignal) fA1;
				v = (ZILValue) fB1;
			}
			if (fB1 instanceof ZILOperationReadSignal && fA1 instanceof ZILValue) {
				zors = (ZILOperationReadSignal) fB1;
				v = (ZILValue) fA1;
			}
		}

		if (zosa != null && zoc != null && zors != null && v != null) {
			if (zosa.getOperation() != SAOp.EVENT)
				return null;

			ZILSignal s1 = zosa.getSignal();

			ZILSignal s2 = zors.getSignal();

			if (s2 != s1)
				return null;

			if (!v.getType().isBit())
				return null;

			return new ZILClock(s1, v.isLogicOne());
		}

		return null;
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
	public ZILOperation inlineSubprograms(VariableRemapping aVR, ZILSequenceOfStatements aSOS, RTLCache aCache) throws ZamiaException {
		ZILOperation a = fA.inlineSubprograms(aVR, aSOS, aCache);
		ZILOperation b = null;
		if (fB != null)
			b = fB.inlineSubprograms(aVR, aSOS, aCache);

		return new ZILOperationLogic(fOp, a, b, getType(), aSOS.getContainer(), getSrc());
	}

	@Override
	public ZILOperation resolveVariables(Bindings aVR, ZILSequenceOfStatements aSOS, RTLCache aCache) throws ZamiaException {
		ZILOperation a = fA.resolveVariables(aVR, aSOS, aCache);
		ZILOperation b = null;
		if (fB != null)
			b = fB.resolveVariables(aVR, aSOS, aCache);

		return new ZILOperationLogic(fOp, a, b, getType(), aSOS.getContainer(), getSrc());
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
		if (fB != null) {
			fB.computeReadSignals(aReadSignals);
		}
	}

}
