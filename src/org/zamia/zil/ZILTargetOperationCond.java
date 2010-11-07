/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 25, 2009
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLGraph;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.RTLSignalAE;
import org.zamia.rtl.RTLTargetCond;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class ZILTargetOperationCond extends ZILTargetOperation {

	private ZILTargetOperation fTOP;

	private ZILOperation fCond;

	public ZILTargetOperationCond(ZILTargetOperation aTOP, ZILOperation aCond, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aTOP.getType(), aContainer, aSrc);
		fTOP = aTOP;
		fCond = aCond;
	}

	@Override
	protected void doElaborate(RTLSignalAE aResult, Bindings aLastBindings, RTLCache aCache) throws ZamiaException {

		RTLSignalAE src = fTOP.elaborate(aLastBindings, aCache);
		RTLSignal cond = fCond.elaborate(aLastBindings, aCache);

		ZILType tSrc = src.getSignal().getType();
		RTLGraph rtlg = aCache.getGraph();

		RTLSignal res = aResult.getSignal();
		RTLSignal en = aResult.getEnable();

		RTLTargetCond targetCond = new RTLTargetCond(tSrc, rtlg, null, getSrc());
		rtlg.add(targetCond);
		RTLPort p = targetCond.getD();
		p.setSignal(src.getSignal());
		if (src.getEnable() != null) {
			p = targetCond.getE();
			p.setSignal(src.getEnable());
		}
		p = targetCond.getZ();
		p.setSignal(res);
		p = targetCond.getZE();
		p.setSignal(en);
		p = targetCond.getC();
		p.setSignal(cond);
	}

	public void dump(int aIndent) {
		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");
	}

	@Override
	public void generateCode(ZILInterpreterCode aCode, RTLCache aCache) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");
	}

	@Override
	public ZILType getSourceType() {
		return getType();
	}

	@Override
	public ZILTargetOperation inlineSubprograms(VariableRemapping aVR, ZILSequenceOfStatements aSOS, RTLCache aCache) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Not implemented method called.");
	}

	@Override
	public ZILTargetOperation getSource() {
		throw new RuntimeException("Internal error: getSource() called on ZILTargetOperationCond");
	}

	@Override
	public ZILTargetOperation resolveVariables(Bindings aVb, ZILSequenceOfStatements aSos, ZILClock aClk, RTLCache aCache) throws ZamiaException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSource(ZILTargetOperation aSource) {
		throw new RuntimeException("Internal error: setSource() called on ZILTargetOperationCond");
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		throw new RuntimeException("Internal error: computeReadSignals() called on ZILTargetOperationCond");
	}

}
