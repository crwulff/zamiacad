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
import org.zamia.rtl.RTLTargetEMux;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class ZILTargetOperationEMux extends ZILTargetOperation {

	private ZILTargetOperation fValue;

	private ZILTargetOperation fOldValue;

	public ZILTargetOperationEMux(ZILTargetOperation aOldValue, ZILTargetOperation aValue, ZILIContainer aContainer, ASTObject aSrc) {
		super(aOldValue.getType(), aContainer, aSrc);
		fOldValue = aOldValue;
		fValue = aValue;
	}

	@Override
	protected void doElaborate(RTLSignalAE aResult, Bindings aLastBindings, RTLCache aCache) throws ZamiaException {

		RTLGraph rtlg = aCache.getGraph();

		RTLTargetEMux emux = new RTLTargetEMux(getType(), rtlg, null, getSrc());
		rtlg.add(emux);

		// connect outputs

		RTLSignal res = aResult.getSignal();
		RTLSignal en = aResult.getEnable();

		RTLPort pz = emux.getZ();
		pz.setSignal(res);
		RTLPort pze = emux.getZE();
		pze.setSignal(en);

		// connect d1

		RTLSignalAE v = fValue.elaborate(aLastBindings, aCache);

		RTLPort p = emux.getD1();
		p.setSignal(v.getSignal());
		if (v.getEnable() != null) {
			p = emux.getE1();
			p.setSignal(v.getEnable());
		}

		// connect d2

		RTLSignalAE ov = fOldValue.elaborate(aLastBindings, aCache);

		p = emux.getD2();
		p.setSignal(ov.getSignal());
		if (ov.getEnable() != null) {
			p = emux.getE2();
			p.setSignal(ov.getEnable());
		}
	}

	public void dump(int indent) {
		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");
	}

	public ZILTargetOperation getValue() {
		return fValue;
	}

	public ZILTargetOperation getOldValue() {
		return fOldValue;
	}

	@Override
	public void generateCode(ZILInterpreterCode code, RTLCache cache) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");
	}

	@Override
	public ZILType getSourceType() {
		return getType();
	}

	@Override
	public ZILTargetOperation inlineSubprograms(VariableRemapping aVr, ZILSequenceOfStatements aSos, RTLCache aCache) throws ZamiaException {
		ZILTargetOperationEMux top = new ZILTargetOperationEMux(fOldValue.inlineSubprograms(aVr, aSos, aCache), fValue.inlineSubprograms(aVr, aSos, aCache), aSos.getContainer(), getSrc());

		top.setSource(getSource().inlineSubprograms(aVr, aSos, aCache));

		return top;
	}

	@Override
	public ZILTargetOperation resolveVariables(Bindings aVb, ZILSequenceOfStatements aSos, ZILClock aClk, RTLCache aCache) throws ZamiaException {
		ZILTargetOperationEMux top = new ZILTargetOperationEMux(fOldValue.resolveVariables(aVb, aSos, aClk, aCache), fValue.resolveVariables(aVb, aSos, aClk, aCache), aSos.getContainer(), getSrc());

		top.setSource(getSource().resolveVariables(aVb, aSos, aClk, aCache));

		return top;
	}

	@Override
	public ZILTargetOperation getSource() {
		throw new RuntimeException ("getSource() called on ZILTargetOperationEMux");
	}

	@Override
	public void setSource(ZILTargetOperation aSource) {
		throw new RuntimeException ("setSource() called on ZILTargetOperationEMux");
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		fValue.computeReadSignals(aReadSignals);
		fOldValue.computeReadSignals(aReadSignals);
	}
}
