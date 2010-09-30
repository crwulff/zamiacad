/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 20, 2008
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLSignal;
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
public class ZILOperationExtRange extends ZILOperation {

	private ZILRange fRange;
	private ZILOperation fOperand;

	public ZILOperationExtRange(ZILRange aRange, ZILOperation aOperand, ZILType aType, ZILIContainer aContainer, ASTObject aSrc) {
		super(aType, aContainer, aSrc);
		
		fRange = aRange;
		fOperand = aOperand;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		return "OperationExtRange (range="+fRange+", operand="+fOperand+")";
	}
	
//	@Override
//	public boolean isSynthesizable(VariableRemapping vr_, OperationCache cache_) throws ZamiaException {
//		int n = ranges.size();
//		for (int i = 0; i < n; i++) {
//			if (!ranges.get(i).isSynthesizable(vr_, cache_))
//				return false;
//		}
//		return true;
//	}


//	@Override
//	public RTLSignal elaborate(RTLSignal s_, OperationCache cache_, VariableRemapping vr_) throws ZamiaException {
//
//		int nRanges = ranges.size();
//		if (nRanges != 1) {
//			// FIXME: implement
//			throw new ZamiaException("Sorry, multiple ranges not supported yet.", this, cache_);
//		}
//		Range range = ranges.get(0);
//
//		SigTypeArray type = (SigTypeArray) s_.getType();
//		SigType resType = cache_.getType(this);
//		if (resType == null)
//			throw new ZamiaException("Internal Error:\ncomputeType() was not called before elaborate() in NameExtensionIndex.", this, cache_);
//
//		RTLGraph rtlg = cache_.getRTLGraph();
//
//		if (!range.isRange()) {
//
//			Operation exp = range.getLeft();
//
//			RTLSignal sExp = exp.elaborate(cache_, type.getIndexType(), vr_);
//
//			return rtlg.placeArraySel(s_, sExp, resType, this);
//
//		} else {
//
//			if (range.isAscending() != type.getIndexType().isAscending())
//				throw new ZamiaException("Range direction mismatch.", range, cache_);
//
//			Value lv = range.getLeft().getConstant(cache_, type.getIndexType(), null, false);
//			if (lv == null)
//				throw new ZamiaException("Constant range expected.", range, cache_);
//			Value rv = range.getRight().getConstant(cache_, type.getIndexType(), null, false);
//			if (rv == null)
//				throw new ZamiaException("Constant range expected.", range, cache_);
//
//			int l = lv.getInt(this);
//			int r = rv.getInt(this);
//
//			// FIXME: boundary checks
//
//			return rtlg.placeArrayRangeSel(s_, l, r, type.getIndexType().isAscending(), resType, this);
//
//		}
//	}
	@Override
	public boolean isConstant() throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException ("Not implemented method called.");
	}

	@Override
	public void generateCode(ZILInterpreterCode code_, RTLCache aCache) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException ("Not implemented method called.");
	}

	@Override
	public ZILClock getClock() throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException ("Not implemented method called.");
	}

	@Override
	public ZILOperation inlineSubprograms(VariableRemapping vr_, ZILSequenceOfStatements sos_, RTLCache cache_) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException ("Not implemented method called.");
	}

	@Override
	public boolean isSynthesizable() throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException ("Not implemented method called.");
	}

	@Override
	public ZILOperation resolveVariables(Bindings vbs_, ZILSequenceOfStatements sos_, RTLCache cache_) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException ("Not implemented method called.");
	}

	@Override
	protected void doElaborate(RTLSignal result_, Bindings aLastBindings, RTLCache cache_) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException ("Not implemented method called.");
	}

	@Override
	public int getNumOperands() {
		return 1;
	}

	@Override
	public ZILOperation getOperand(int aIdx) {
		return fOperand;
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		fOperand.computeReadSignals(aReadSignals);
		fRange.computeReadSignals(aReadSignals);
	}

}
