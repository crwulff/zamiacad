/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.zil.synthesis;

import java.io.PrintStream;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLGraph;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLRegister;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.RTLSignalAE;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.RTLCache;
import org.zamia.zil.ZILClock;
import org.zamia.zil.ZILIReferable;
import org.zamia.zil.ZILTargetOperationDestination;
import org.zamia.zil.ZILType;


/**
 * 
 * @author guenter bartsch
 * 
 *         represents a signal binding
 * 
 */

public class Binding {

	public static final boolean dump = false;

	private ZILIReferable fReferable;

	private ZILTargetOperationDestination fSyncValue = null, fASyncValue = null;

	private ZILClock fClk = null;

	//	private Bindings bindings;

	public Binding(Bindings aBindings, ZILIReferable aReferable) {
		fReferable = aReferable;
		//		bindings = bindings_;
	}

	public ZILIReferable getReferable() {
		return fReferable;
	}

	public ZILTargetOperationDestination getSyncValue() {
		return fSyncValue;
	}

	public void setSyncValue(ZILTargetOperationDestination aSyncValue) {
		fSyncValue = aSyncValue;
	}

	public ZILTargetOperationDestination getASyncValue() {
		return fASyncValue;
	}

	public void setASyncValue(ZILTargetOperationDestination aASyncValue) {
		fASyncValue = aASyncValue;
	}

	public String toString() {
		return ("Binding referable=" + fReferable + " aSyncValue=" + fASyncValue + " syncValue=" + fSyncValue);
	}

	public void elaborate(Bindings aLastBindings, RTLCache aCache) throws ZamiaException {

		if (dump) {
			System.out.println("Binding.elaborate " + this);
			dump(System.out, 0);
		}

		RTLSignalAE asyncSAE = null;
		RTLSignal asyncValue = null;
		RTLSignal asyncEnable = null;
		if (fASyncValue != null) {

			asyncSAE = fASyncValue.getSource().elaborate(aLastBindings, aCache);
			asyncValue = asyncSAE.getSignal();
			asyncEnable = asyncSAE.getEnable();
		}

		RTLSignalAE syncSAE = null;
		RTLSignal syncValue = null;
		RTLSignal syncEnable = null;
		if (this.fSyncValue != null) {
			syncSAE = this.fSyncValue.getSource().elaborate(aLastBindings, aCache);
			syncValue = syncSAE.getSignal();
			syncEnable = syncSAE.getEnable();
		}

		/*
		 * at this point we have elaborated both the sync and async values and
		 * enable signals (SignalAE - Signal and Enable) of this binding. Now we
		 * need to infer registers if neccessary.
		 * 
		 * Constant propagation on the enable signal will take care of the
		 * decision wheter this needs to be a latch, flipflop, flipflop with
		 * asynchroneous inputs or just plain combinational logic.
		 */

		
		ZILType t = null;
		if (syncValue != null)
			t = syncValue.getType();
		if (asyncValue != null)
			t = asyncValue.getType();
		if (t == null)
			throw new ZamiaException("Binding has neither a sync nor an async value.");

		RTLSignal targetSignal = fReferable.elaborate(aLastBindings, aCache);
		
		RTLGraph rtlg = aCache.getGraph();

		if (syncValue == null && asyncEnable == null) {
			rtlg.sigJoin(asyncValue, targetSignal, null);
			return;
		}

		ASTObject src = targetSignal.getSource();

		RTLRegister reg = new RTLRegister(rtlg, t, targetSignal.getId(), src);
		rtlg.add(reg);
		RTLPort pq = reg.getZ();
		pq.setSignal(targetSignal);

		if (asyncValue != null) {
			RTLPort p = reg.getASyncData();
			p.setSignal(asyncValue);

			if (asyncEnable != null) {
				p = reg.getASyncEnable();
				p.setSignal(asyncEnable);
			}
		}

		if (syncValue != null) {
			RTLPort p = reg.getSyncData();
			p.setSignal(syncValue);

			if (syncEnable != null) {
				p = reg.getSyncEnable();
				p.setSignal(syncEnable);
			}

			p = reg.getClk();
			p.setSignal(fClk.getSignal().elaborate(aLastBindings, aCache));
		}
	}

	public void dump(PrintStream out_, int offset_) {
		ASTObject.printSpaces(out_, offset_);
		out_.println("Binding dump for referable:" + fReferable);
		ASTObject.printSpaces(out_, offset_);
		out_.println("ASyncValue: ");
		if (fASyncValue != null) {
			fASyncValue.dump(offset_ + 2);
			//FIXME			IntermediateObject.dump(out_, aSyncValue, offset_ + 2, new HashSet<Object>());
		} else {
			ASTObject.printSpaces(out_, offset_);
			out_.println("  null");
		}
		ASTObject.printSpaces(out_, offset_);
		out_.println("syncValue: ");
		if (fSyncValue != null) {
			fSyncValue.dump(offset_ + 2);
			// FIXME			IntermediateObject.dump(out_, syncValue, offset_ + 2, new HashSet<Object>());
		} else {
			ASTObject.printSpaces(out_, offset_);
			out_.println("  null");
		}
	}

	public void setClock(ZILClock clk_) {
		fClk = clk_;
	}

	public ZILClock getClock() {
		return fClk;
	}
}
