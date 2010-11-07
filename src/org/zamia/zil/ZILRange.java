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
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.vhdl.ast.Range;
import org.zamia.vhdl.ast.SequenceOfStatements;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class ZILRange extends ZILObject {

	private ZILOperation fRight;
	private ZILOperation fLeft;
	private ZILOperation fAscending;
	private boolean fBAscending;

	public ZILRange(ZILOperation aLeft, ZILOperation aRight, ZILOperation aAscending, ZILIContainer aContainer, VHDLNode aSrc) {
		super(ZILTypeVoid.getInstance(), aContainer, aSrc);
		
		fLeft = aLeft;
		fRight = aRight;
		fAscending = aAscending;
	}
	
	public ZILRange(ZILIContainer aContainer, VHDLNode aSrc) {
		super(ZILTypeVoid.getInstance(), aContainer, aSrc);
	}

	public ZILRange(ZILOperation aLeft, ZILOperation aRight, boolean aAscending, ZILIContainer aContainer, VHDLNode aSrc) {
		super(ZILTypeVoid.getInstance(), aContainer, aSrc);
		
		fLeft = aLeft;
		fRight = aRight;
		fBAscending = aAscending;
	}

	public ZILRange(long aLeft, long aRight, boolean aAscending, ZILIContainer aContainer, VHDLNode aSrc) {
		this (aContainer, aSrc);

		try {
			fLeft = new ZILValue(aLeft, ZILType.intType, aContainer, aSrc);
			fRight = new ZILValue(aRight, ZILType.intType, aContainer, aSrc);
		} catch (ZamiaException e) {
			el.logException(e);
		}
		fBAscending = aAscending;
	}

	public boolean isConstant() throws ZamiaException {
		if (fAscending != null && !fAscending.isConstant())
			return false;
		return fLeft.isConstant() && fRight.isConstant();
	}

	public ZILOperation getLeft() {
		return fLeft;
	}

	public ZILOperation getRight() {
		return fRight;
	}

	public ZILOperation getAscending() {
		if (fAscending == null) {
			return fBAscending ? ZILValue.getBit(ZILValue.BIT_1, null) : ZILValue.getBit(ZILValue.BIT_0, null);
		}
		return fAscending;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		
		return "range (left="+fLeft+", right="+fRight+", ascending="+fAscending+")";
	}

	public Range inlineSubprograms(VariableRemapping vr_,
			SequenceOfStatements sos_, RTLCache cache_)
			throws ZamiaException {

		// FIXME: implement
		throw new RuntimeException ("Not implemented method called.");
//		Operation l = left.inlineSubprograms(vr_, sos_, cache_);
//		Operation r = l;
//		if (right != left) {
//			r = right.inlineSubprograms(vr_, sos_, cache_);
//		}
//
//		return new Range(l, r, ascending, null, location);
	}

	public Range resolveVariables(Bindings vr_,
			SequenceOfStatements sos_, RTLCache cache_)
			throws ZamiaException {

		// FIXME: implement
		throw new RuntimeException ("Not implemented method called.");
//		Operation l = left.resolveVariables(vr_, sos_, cache_);
//		Operation r = l;
//		if (right != left) {
//			r = right.resolveVariables(vr_, sos_, cache_);
//		}
//
//		return new Range(l, r, ascending, null, location);
	}

	public void generateCode(ZILTypeDiscrete typeHint_, ZILInterpreterCode code_, RTLCache cache_) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException ("Not implemented method called.");
//		if (isRange()) {
//			
//			left.generateCode(code_, cache_, typeHint_);
//			right.generateCode(code_, cache_, typeHint_);
//			if (isAscending()) {
//				code_.add(new PushStmt(Value.getBit(Value.BIT_1), this));
//			} else {
//				code_.add(new PushStmt(Value.getBit(Value.BIT_0), this));
//			}
//			
//			code_.add(new RangeStmt(RangeOp.CREATE, typeHint_, this));
//
//		} else {
//			getLeft().generateCode(code_, cache_, typeHint_);
//		}
	}

	public boolean isSynthesizable() throws ZamiaException {
		return fLeft.isSynthesizable() && fRight.isSynthesizable() && (fAscending == null || fAscending.isSynthesizable());
	}

	public ZILRange inlineSubprograms(VariableRemapping aVr, ZILSequenceOfStatements aSos, RTLCache aCache) throws ZamiaException {
		
		ZILOperation left = fLeft.inlineSubprograms(aVr, aSos, aCache);
		
		ZILOperation right = fRight != null ? fRight.inlineSubprograms(aVr, aSos, aCache) : left;
		
		return new ZILRange(left, right, fAscending, aSos.getContainer(), getSrc());
	}

	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		fLeft.computeReadSignals(aReadSignals);
		if (fRight != null && fLeft != fRight)
			fRight.computeReadSignals(aReadSignals);
	}

	public void setAscending(ZILOperation aAscending) {
		fAscending = aAscending;
	}

	public void setLeft(ZILOperation aLeft) {
		fLeft = aLeft;
	}

	public void setRight(ZILOperation aRight) {
		fRight = aRight;
	}

	public void setAscending(boolean aAscending) {
		fBAscending = aAscending;
		fAscending = null;
	}
	
	public boolean isAscending () throws ZamiaException {
		if (fAscending == null) {
			return fBAscending;
		}
		ZILValue v = fAscending.computeConstant();
		if (v == null)
			throw new ZamiaException ("Range is not constant", getSrc());
		
		return v.isLogicOne();
	}

	public long computeCardinality(VHDLNode aSrc) throws ZamiaException {
		
		if (!isConstant()) {
			throw new ZamiaException ("Constant range expected here", aSrc);
		}

		ZILValue left = fLeft.computeConstant();
		ZILValue right = fRight.computeConstant();
		long ll = left.getLong(aSrc);
		long lr = right.getLong(aSrc);
		
		return isAscending() ? lr - ll + 1 : ll - lr +1; 
	}
}
