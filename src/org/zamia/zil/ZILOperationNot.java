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
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILNotStmt;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class ZILOperationNot extends ZILOperation {

	private ZILOperation fA;

	public ZILOperationNot(ZILOperation aA, ZILIContainer container,
			VHDLNode src) {
		super(aA.getType(), container, src);

		fA = aA;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		return "OperationNOT (a=" + fA + ")";
	}

	@Override
	protected void doElaborate(RTLSignal result_, Bindings aLastBindings,
			RTLCache cache_) throws ZamiaException {

		RTLGraph rtlg = cache_.getGraph();

		RTLSignal res = null;

		res = rtlg.placeOperationLogic(LogicOp.NOT, fA.elaborate(aLastBindings,
				cache_), null, getSrc());

		rtlg.sigJoin(res, result_, getSrc());
	}

	@Override
	public ZILClock getClock() throws ZamiaException {
		return null;
	}

	@Override
	public ZILOperation resolveVariables(Bindings vr_,
			ZILSequenceOfStatements sos_, RTLCache cache_)
			throws ZamiaException {
		 ZILOperation a = fA.resolveVariables(vr_, sos_, cache_);
		 return new ZILOperationNot(a, sos_.getContainer(), getSrc());
	}

	@Override
	public void generateCode(ZILInterpreterCode aCode, RTLCache aCache)
			throws ZamiaException {
		fA.generateCode(aCode, aCache);
		aCode.add(new ZILNotStmt(getSrc()));
	}

	@Override
	public boolean isSynthesizable() throws ZamiaException {
		if (!fA.isSynthesizable())
			return false;

		return true;
	}

	@Override
	public ZILOperation inlineSubprograms(VariableRemapping vr_,
			ZILSequenceOfStatements sos_, RTLCache cache_)
			throws ZamiaException {
		ZILOperation a = fA.inlineSubprograms(vr_, sos_, cache_);
		return new ZILOperationNot(a, sos_.getContainer(), getSrc());
	}

	@Override
	public boolean isConstant() throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");
	}

	@Override
	public int getNumOperands() {
		return 1;
	}

	@Override
	public ZILOperation getOperand(int aIdx) {
		return fA;
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		fA.computeReadSignals(aReadSignals);
	}
}
