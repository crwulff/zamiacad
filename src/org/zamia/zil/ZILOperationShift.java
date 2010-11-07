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
public class ZILOperationShift extends ZILOperation {

	private int fOp;

	private ZILOperation fA;

	private ZILOperation fB;

	public ZILOperationShift(int aOp, ZILOperation aA, ZILOperation aB, ZILType type, ZILIContainer container, VHDLNode src) {
		super(type, container, src);

		fOp = aOp;
		fA = aA;
		fB = aB;

	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		return "OperationShift (op=" + fOp + ", a=" + fA + ", b=" + fB + ")";
	}

	@Override
	protected void doElaborate(RTLSignal result_, Bindings aLastBindings, RTLCache cache_) throws ZamiaException {
		// FIXME: implement.
		throw new ZamiaException("Sorry, OperationShift.elaborate is not implemented yet.");
	}

	@Override
	public ZILClock getClock() throws ZamiaException {
		return null;
	}

	@Override
	public void generateCode(ZILInterpreterCode code_, RTLCache aCache) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Sorry, OperationMath.generateCode is not implemented yet.");
	}

	@Override
	public boolean isSynthesizable() throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");
		//		if (!a.isSynthesizable(vr_, cache_))
		//			return false;
		//		if (b != null && !b.isSynthesizable(vr_, cache_))
		//			return false;
		//		
		//		return true;
	}

	@Override
	public ZILOperation inlineSubprograms(VariableRemapping vr_, ZILSequenceOfStatements sos_, RTLCache cache_) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");
		//		Operation a = this.a.inlineSubprograms(vr_, sos_, cache_);
		//		Operation b = null;
		//		if (this.b != null)
		//			b = this.b.inlineSubprograms(vr_, sos_, cache_);
		//		
		//		return new OperationShift(op, a, b, null, location);
	}

	@Override
	public ZILOperation resolveVariables(Bindings vr_, ZILSequenceOfStatements sos_, RTLCache cache_) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");
		//		Operation a = this.a.resolveVariables(vr_, sos_, cache_);
		//		Operation b = null;
		//		if (this.b != null)
		//			b = this.b.resolveVariables(vr_, sos_, cache_);
		//		
		//		return new OperationShift(op, a, b, null, location);
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

}
