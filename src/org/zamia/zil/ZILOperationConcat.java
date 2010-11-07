/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 24, 2009
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLSignal;
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

public class ZILOperationConcat extends ZILOperation {

	private ZILOperation fA, fB;

	public ZILOperationConcat(ZILOperation aA, ZILOperation aB, ZILType aType, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aType, aContainer, aSrc);
		fA = aA;
		fB = aB;
	}

	@Override
	protected void doElaborate(RTLSignal result_, Bindings aLastBindings, RTLCache cache_) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Not implemented method called.");
	}

	@Override
	public void generateCode(ZILInterpreterCode code_, RTLCache aCache) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");

		// FIXME

		//		fA.generateCode(code_, cache_, typeHint_);
		//		fB.generateCode(code_, cache_, typeHint_);
		//
		//		code_.add(new ConcatStmt(getType(), this));
	}

	@Override
	public ZILClock getClock() throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");
	}

	@Override
	public ZILOperation inlineSubprograms(VariableRemapping vr_, ZILSequenceOfStatements sos_, RTLCache cache_) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");
		// FIXME
		//		Operation a = this.a.inlineSubprograms(vr_, sos_, cache_);
		//		Operation b = null;
		//		if (this.b != null)
		//			b = this.b.inlineSubprograms(vr_, sos_, cache_);
		//
		//		return new OperationConcat(a, b, null, location);
	}

	@Override
	public boolean isSynthesizable() throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");
		//		if (!fA.isSynthesizable(vr_, cache_))
		//			return false;
		//		if (!fB.isSynthesizable(vr_, cache_))
		//			return false;
		//
		//		return true;
	}

	@Override
	public ZILOperation resolveVariables(Bindings vbs_, ZILSequenceOfStatements sos_, RTLCache cache_) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");
		// FIXME
		//		Operation a = this.a.resolveVariables(vr_, sos_, cache_);
		//		Operation b = null;
		//		if (this.b != null)
		//			b = this.b.resolveVariables(vr_, sos_, cache_);
		//
		//		return new OperationConcat(a, b, null, location);
	}

	public void dump(int indent) {
		// TODO Auto-generated method stub
		// FIXME: implement dump

	}

	@Override
	public boolean isConstant() throws ZamiaException {
		return fA.isConstant() && fB.isConstant();
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

}
