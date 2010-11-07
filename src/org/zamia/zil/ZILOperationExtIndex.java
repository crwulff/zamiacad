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
import org.zamia.rtl.RTLGraph;
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
public class ZILOperationExtIndex extends ZILOperation {

	private ZILOperation fOperand;

	private ZILOperation fIndex;

	public ZILOperationExtIndex(ZILOperation aIndex, ZILOperation aOperand, ZILType aType, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aType, aContainer, aSrc);

		fIndex = aIndex;
		fOperand = aOperand;

	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		return "OperationExtIndex (index=" + fIndex + ", operand=" + fOperand + ")";
	}

	@Override
	protected void doElaborate(RTLSignal result_, Bindings aLastBindings, RTLCache cache_) throws ZamiaException {

		RTLGraph rtlg = cache_.getGraph();

		RTLSignal s = fOperand.elaborate(aLastBindings, cache_);
		
		RTLSignal idxS = fIndex.elaborate(aLastBindings, cache_);

		rtlg.placeArraySel(s, idxS, result_, getType(), getSrc());
	}

	//
	//	private SigType generate(InterpreterCode code_, OperationCache cache_, SigType tIn_) throws ZamiaException {
	//
	//		int nIndices = indices.size();
	//
	//		SigType type = tIn_;
	//
	//		for (int i = 0; i < nIndices; i++) {
	//
	//			if (!(type instanceof SigTypeArray)) {
	//				throw new ZamiaException("Tried to index into something that is not an array.", this, cache_);
	//			}
	//
	//			SigTypeArray tIn = (SigTypeArray) type;
	//			SigTypeDiscrete idxType = tIn.getIndexType();
	//			SigType elementType = tIn.getElementType();
	//
	//			Operation exp = indices.get(i);
	//
	//			SigType tExp = exp.getType(cache_, idxType, null, false);
	//			if (tExp instanceof SigTypeRef) {
	//
	//				SigType t = ((SigTypeRef) tExp).getRefType();
	//
	//				if (!(t instanceof SigTypeDiscrete)) {
	//					throw new ZamiaException("Discrete range/type expected here.", exp, cache_);
	//				}
	//
	//				SigTypeDiscrete td = (SigTypeDiscrete) t;
	//
	//				int l = (int) td.getLeft();
	//				int r = (int) td.getRight();
	//
	//				SigTypeArray resType = (SigTypeArray) tIn.createSubType(l, r, td.isAscending(), this);
	//
	//				code_.add(new PushStmt(new Value(r, SigType.intType), this));
	//				code_.add(new PushStmt(new Value(l, SigType.intType), this));
	//				if (td.isAscending()) {
	//					code_.add(new PushStmt(Value.getBit(Value.BIT_1), this));
	//				} else {
	//					code_.add(new PushStmt(Value.getBit(Value.BIT_0), this));
	//				}
	//
	//				code_.add(new ExtRangeStmt(resType, this));
	//
	//				type = resType;
	//			} else {
	//
	//				indices.get(i).generateCode(code_, cache_, tIn.getIndexType());
	//				code_.add(new ExtIndexStmt(type, elementType, this));
	//
	//				type = elementType;
	//			}
	//
	//		}
	//		return type;
	//	}

	//	@Override
	//	public boolean isSynthesizable(VariableRemapping vr_, OperationCache cache_) throws ZamiaException {
	//		int n = indices.size();
	//		for (int i = 0; i < n; i++)
	//			if (!indices.get(i).isSynthesizable(vr_, cache_))
	//				return false;
	//		return true;
	//	}

	//	@Override
	//	public NameExtension inlineSubprograms(VariableRemapping vr_, SequenceOfStatements sos_, OperationCache cache_) throws ZamiaException {
	//
	//		int n = indices.size();
	//
	//		NameExtensionIndex res = new NameExtensionIndex(indices.get(0).inlineSubprograms(vr_, sos_, cache_), null, location);
	//
	//		for (int i = 1; i < n; i++) {
	//			res.add(indices.get(i).inlineSubprograms(vr_, sos_, cache_));
	//		}
	//
	//		return res;
	//	}
	//
	//	@Override
	//	public NameExtension resolveVariables(VariableBindings vr_, SequenceOfStatements sos_, OperationCache cache_) throws ZamiaException {
	//		// FIXME: implement
	//		throw new RuntimeException ("Not implemented method called.");
	//		int n = indices.size();
	//
	//		NameExtensionIndex res = new NameExtensionIndex(indices.get(0).resolveVariables(vr_, sos_, cache_), null, location);
	//
	//		for (int i = 1; i < n; i++) {
	//			res.add(indices.get(i).resolveVariables(vr_, sos_, cache_));
	//		}
	//
	//		return res;
	//	}

	@Override
	public boolean isConstant() throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");
	}

	@Override
	public void generateCode(ZILInterpreterCode code_, RTLCache aCache) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");
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
	}

	@Override
	public boolean isSynthesizable() throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");
	}

	@Override
	public ZILOperation resolveVariables(Bindings vbs_, ZILSequenceOfStatements sos_, RTLCache cache_) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");
	}

	@Override
	public int getNumOperands() {
		return 2;
	}

	@Override
	public ZILOperation getOperand(int aIdx) {
		return aIdx == 0 ? fIndex : fOperand;
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		fOperand.computeReadSignals(aReadSignals);
		fIndex.computeReadSignals(aReadSignals);
	}

}
