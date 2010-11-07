/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 7, 2008
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLGraph;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLRegister;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.RTLSignalAE;
import org.zamia.rtl.RTLTargetEMux;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILPushRefStmt;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableBinding;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class ZILVariable extends ZILOperation implements ZILIReferable {

	private static int fCounter = 1;
	
	private ZILValue fInitialValue;

	// transients:
	private int fUID;

	private boolean fIgnoreOldValue = false;

	public ZILVariable(String aId, ZILType aType, ZILValue aInitialValue, ZILIContainer aContainer, VHDLNode aSrc) {
		this(aId, aType, aInitialValue, 0, aContainer, aSrc);
	}

	public ZILVariable(String aId, ZILType aType, ZILValue aInitialValue, int aUID, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aType, aContainer, aSrc);
		fId = aId;
		fUID = aUID > 0 ? aUID : fCounter++;
		
		fInitialValue = aInitialValue;
	}

	public ZILValue getInitialValue() {
		return fInitialValue;
	}

	public void dump(int indent) {
		logger.debug("VAR %s: %s := %s", getId(), getType(), fInitialValue);
	}

	@Override
	public String toString() {
		return "VAR " + getId() + ": " + getType() + ":= " + fInitialValue;
	}

	public void generateInterpreterCodeRef(boolean isInertial, boolean aHaveDelay, boolean aHaveReject, ZILInterpreterCode aCode, RTLCache aCache) throws ZamiaException {

		if (aHaveDelay) {
			throw new ZamiaException("Variable assignments cannot have delays.", getSrc());
		}

		if (aHaveReject) {
			throw new ZamiaException("Variable assignments cannot have rejects.", getSrc());
		}

		aCode.add(new ZILPushRefStmt(this, getSrc()));
	}

	public boolean isValidTarget() {
		return true;
	}

	@Override
	protected void doElaborate(RTLSignal aResult, Bindings aLastBindings, RTLCache aCache) throws ZamiaException {

		RTLGraph rtlg = aCache.getGraph();

		VariableBinding ovb = aLastBindings.getVariableBindingByUID(fUID);
		
		ZILTargetOperation oldvalue = ovb != null ? ovb.getTO() : null;

		if (fIgnoreOldValue)
			oldvalue = null;
		else
			fIgnoreOldValue = true;

		VariableBinding vb = aCache.getVariableBinding(this);

		RTLSignal s = null;

		if (vb != null) {
			RTLSignalAE sae = vb.getTO().elaborate(aLastBindings, aCache);

			s = sae.getSignal();
			RTLSignal enable = sae.getEnable();
			ZILType t = s.getType();

			if (oldvalue != null && oldvalue != vb.getTO()) {

				RTLSignalAE sae2 = oldvalue.elaborate(aLastBindings, aCache);

				RTLSignal s2 = sae2.getSignal();
				RTLSignal enable2 = sae2.getEnable();

				RTLTargetEMux emux = new RTLTargetEMux(t, rtlg, null, getSrc());
				rtlg.add(emux);

				RTLPort p = emux.getD1();
				p.setSignal(s);
				p = emux.getE1();
				p.setSignal(enable);
				p = emux.getD2();
				p.setSignal(s2);
				p = emux.getE2();
				p.setSignal(enable2);

				sae = rtlg.createUnnamedSignalAE(t, getSrc());
				s = sae.getSignal();
				enable = sae.getEnable();
				p = emux.getZ();
				p.setSignal(s);
				p = emux.getZE();
				p.setSignal(enable);

			}

			if (enable != null) {

				RTLRegister reg = new RTLRegister(rtlg, t, null, getSrc());
				rtlg.add(reg);

				if (vb.getClk() != null) {
					RTLPort p = reg.getSyncData();
					p.setSignal(s);

					p = reg.getSyncEnable();
					p.setSignal(enable);

					p = reg.getClk();
					p.setSignal(vb.getClk().getSignal().elaborate(aLastBindings, aCache));

				} else {
					RTLPort p = reg.getASyncData();
					p.setSignal(s);

					p = reg.getASyncEnable();
					p.setSignal(enable);
				}

				s = rtlg.createUnnamedSignal(vb.getTO().getType(), getSrc());

				RTLPort pq = reg.getZ();
				pq.setSignal(s);
			}

		} else if (oldvalue != null) {
			RTLSignalAE sae = oldvalue.elaborate(aLastBindings, aCache);

			s = sae.getSignal();
			RTLSignal enable = sae.getEnable();

			if (enable != null) {

				RTLRegister reg = new RTLRegister(rtlg, s.getType(), null, getSrc());
				rtlg.add(reg);

				RTLPort p = reg.getASyncData();
				p.setSignal(s);

				p = reg.getASyncEnable();
				p.setSignal(enable);

				s = rtlg.createUnnamedSignal(s.getType(), getSrc());

				RTLPort pq = reg.getZ();
				pq.setSignal(s);
			}

			fIgnoreOldValue = false;

		} else
			throw new ZamiaException("Variable was never assigned a value: " + this, getSrc());

		rtlg.sigJoin(s, aResult, getSrc());
	}

	@Override
	public void generateCode(ZILInterpreterCode code_, RTLCache aCache) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Not implemented method called.");
	}

	@Override
	public ZILClock getClock() throws ZamiaException {
		return null;
	}

	@Override
	public ZILOperation inlineSubprograms(VariableRemapping vr_, ZILSequenceOfStatements sos_, RTLCache cache_) throws ZamiaException {
		return new ZILVariable(getId(), getType(), fInitialValue, fUID, sos_.getContainer(), getSrc());
	}

	@Override
	public boolean isConstant() throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Not implemented method called.");
	}

	@Override
	public boolean isSynthesizable() throws ZamiaException {
		return true;
	}

	@Override
	public ZILOperation resolveVariables(Bindings vbs_, ZILSequenceOfStatements sos_, RTLCache cache_) throws ZamiaException {

		VariableBinding binding = vbs_.get(this);
		cache_.setVariableBinding(this, binding);

		return this;
		//return new ZILVariable(getId(), getType(), fInitialValue, fUID, sos_.getContainer(), getSrc());
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
	}

	public int getUID() {
		return fUID;
	}

}
