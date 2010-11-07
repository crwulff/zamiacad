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
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILLabel;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class ZILSequentialAssignment extends ZILSequentialStatement {

	public final static boolean dump = false;

	private ZILTargetOperationDestination fTarget;

	private ZILOperation fDelay, fReject; 

	private boolean fInertial; 

	public ZILSequentialAssignment(ZILTargetOperationDestination aTargetOp, boolean aInertial, ZILOperation aReject, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aContainer, aSrc);
		fTarget = aTargetOp;
		fReject = aReject;
		fInertial = aInertial;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "SequentialSignalAssignment (reject=%s, inertial=%s, delay=%s) {", fReject, fInertial, fDelay);
		fTarget.dump(aIndent+1);
		logger.debug(aIndent, "}");
	}

	@Override
	public String toString() {
		return "SequentialSignalAssignment (target=" + fTarget + ", delay="+fDelay+ ", reject="+fReject+", inertial="+fInertial+")";
	}

	@Override
	protected Bindings resolveVariables(Bindings aVB, ZILSequenceOfStatements aSOS, ZILClock aClk, RTLCache aCache, VariableRemapping aVR) throws ZamiaException {

		Bindings vb = new Bindings(aVB);

		ZILTargetOperationDestination tod = (ZILTargetOperationDestination) fTarget.resolveVariables(vb, aSOS, aClk, aCache);

		if (tod != null) {
			ZILSequentialAssignment ssa = new ZILSequentialAssignment(tod, fInertial, null, aSOS.getContainer(), getSrc());
			aSOS.add(ssa);
		}
		
		return vb;
	}

	@Override
	protected void inlineSubprograms(VariableRemapping aVR, ZILSequenceOfStatements aSOS, RTLCache aCache, String aReturnVarName) throws ZamiaException {
		ZILSequentialAssignment ssa = new ZILSequentialAssignment((ZILTargetOperationDestination) fTarget.inlineSubprograms(aVR, aSOS, aCache), fInertial, null, aSOS.getContainer(), getSrc());
		aSOS.add(ssa);
	}

	@Override
	public boolean isSynthesizable() throws ZamiaException {
		return fDelay == null && fReject == null;
	}

	@Override
	public Bindings computeBindings(ZILClock clock_, RTLCache cache_, VariableRemapping vr_) throws ZamiaException {

		if (dump)
			logger.debug("ZILSequentialAssignment.computeBinding(): clock=%s, target=%s", clock_, fTarget);

		Bindings bindings = new Bindings();

		if (clock_ != null) {
			bindings.bindClocked(fTarget, clock_);
			//bindings.setClock(clock_);
		} else {
			bindings.bind(fTarget);
		}

		return bindings;

		//SigType type = target.computeType(cache_);

		// since our waveform is required to be synthesizable:
		//		Operation op = waveform.getElement(0).getValue();

		//op.computeType(type, cache_);

		//		return fTarget.computeBindings (op, clock_, cache_, vr_);

		//		Signal t = target.elaborate (context_);
		//		
		//		Bindings bindings = new Bindings (bindings_);
		//		
		//		Operation enable = new OperationLiteral ("1", OperationLiteral.CAT_STRING, null);
		//		enable.computeType(context_, SigType.bit);
		//		if (clock_ != null) {
		//			bindings.bindClocked(context_, t, v, enable, location);
		//			bindings.setClock(clock_, location);
		//		} else
		//			bindings.bind(context_, t, v, enable, location);
		//		
		//		return bindings;
	}

	@Override
	public void generateCode(ZILInterpreterCode aCode, RTLCache aCache, ZILLabel aLoopExitLabel) throws ZamiaException {
		fTarget.generateCode(fInertial, fDelay, fReject, aCode, aCache);
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		fTarget.computeReadSignals(aReadSignals);
		if (fDelay != null) {
			fDelay.computeReadSignals(aReadSignals);
		}
		if (fReject != null) {
			fReject.computeReadSignals(aReadSignals);
		}
	}

	public void setDelay(ZILOperation aDelay) {
		fDelay = aDelay;
	}

}
