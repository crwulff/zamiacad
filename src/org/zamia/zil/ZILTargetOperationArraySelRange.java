/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Mar 14, 2007
 */

package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLSignalAE;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILPushRefStmt;
import org.zamia.zil.interpreter.ZILPushStmt;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * An elaborated, variable target array selector in the RTL graph
 * 
 * @author Guenter Bartsch
 *
 */

public class ZILTargetOperationArraySelRange extends ZILTargetOperation {

	private int fLeft;
	private int fRight;
	private boolean fAscending;
	private ZILType fSourceType;
	private ZILTargetOperation fSource;

	public ZILTargetOperationArraySelRange(int aLeft, int aRight, boolean aAscending, ZILType aSourceType, ZILType aDestinationType, ZILIContainer aContainer, VHDLNode aSrc) {
		super (aDestinationType, aContainer, aSrc);
		fLeft = aLeft;
		fRight = aRight;
		fAscending = aAscending;
		fSourceType = aSourceType;
	}

//	@Override
//	protected RTLSignalAE doElaborate(RTLSignalAE result_, OperationCache cache_, SigType tOut_) throws ZamiaException {
//		
//		if (!(tOut_ instanceof SigTypeArray)) {
//			throw new ZamiaException ("Internal error: array type expected", this, cache_);
//		}
//		
//		SigTypeArray tOut = (SigTypeArray) tOut_;
//		
//		SigType tIn = tOut.createSubType(left, right, ascending, this);
//		
//		RTLSignalAE src = this.src.elaborate(cache_, tIn);
//		
//		SigType tSrc = src.getSignal().getType();
//
//		RTLGraph rtlg = cache_.getRTLGraph();
//		
//		RTLSignal res = result_.getSignal();
//		RTLSignal en = result_.getEnable();
//		
//		RTLTargetArraySelRange arraySel = new RTLTargetArraySelRange(tSrc, tOut, left, right,ascending, rtlg, null, this);
//		rtlg.add(arraySel);
//		RTLPort p = arraySel.getD();
//		p.setSignal(src.getSignal());
//		if (src.getEnable() != null) {
//			p = arraySel.getE();
//			p.setSignal(src.getEnable());
//		}
//		p = arraySel.getZ();
//		p.setSignal(res);
//		p = arraySel.getZE();
//		p.setSignal(en);
//		
//		return result_;
//	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "TargetOperationArraySelRange {");
		logger.debug(aIndent+1, "left=%d", fLeft);
		logger.debug(aIndent+1, "ascending?=%s", fAscending ? "yes" : "no");
		logger.debug(aIndent+1, "right=%d", fRight);
		ZILTargetOperation src = getSource();
		if (src != null) {
			src.dump(aIndent+1);
		} else {
			logger.debug (aIndent+1, "src=null");
		}
		logger.debug(aIndent, "}");
	}
	
	@Override
	public String toString() {
		String rangeStr = fAscending ? fLeft +" to "+fRight : fLeft + " downto " + fRight;
		return "TargetOperationSelRange(range="+rangeStr+", src="+getSource()+")"; 
	}

	@Override
	protected void doElaborate(RTLSignalAE aResult, Bindings aLastBindings, RTLCache aCache) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException ("Not implemented method called.");
	}

	@Override
	public void generateCode(ZILInterpreterCode aCode, RTLCache aCache) throws ZamiaException {
		
		if (fAscending) {
			aCode.add(new ZILPushStmt(new ZILValue(fLeft, ZILType.intType, null, getSrc()), getSrc()));
			aCode.add(new ZILPushStmt(new ZILValue(fRight, ZILType.intType, null, getSrc()), getSrc()));
		} else {
			aCode.add(new ZILPushStmt(new ZILValue(fRight, ZILType.intType, null, getSrc()), getSrc()));
			aCode.add(new ZILPushStmt(new ZILValue(fLeft, ZILType.intType, null, getSrc()), getSrc()));
		}

		aCode.add(new ZILPushRefStmt(true, getSrc()));
	}

	@Override
	public ZILType getSourceType() {
		return fSourceType;
	}

//	@Override
//	public TargetOperation elaborateAsTarget(OperationCache cache_, SigType resType_, VariableRemapping vr_) throws ZamiaException {
//		int nRanges = ranges.size();
//		if (nRanges != 1) {
//			// FIXME: implement
//			throw new ZamiaException("Sorry, multiple ranges not supported yet.", this, cache_);
//		}
//		Range range = ranges.get(0);
//
//		if (!range.isRange()) {
//
//			Operation exp = range.getLeft();
//
//			TargetOperationArraySel arraySel = new TargetOperationArraySel(exp, this, location);
//
//			return arraySel;
//		} else {
//
//			SigType tt = getType(resType_, cache_, vr_);
//
//			if (!(tt instanceof SigTypeArray)) {
//				throw new ZamiaException("Array type expected here, " + tt + " found instead.", this, cache_);
//			}
//
//			SigTypeArray at = (SigTypeArray) tt;
//			SigTypeDiscrete idxType = at.getIndexType();
//
//			Value lc = range.getLeft().getConstant(cache_, idxType, null, false);
//			if (lc == null)
//				throw new ZamiaException("Only constant ranges supported here.", this, cache_);
//
//			Value rc = range.getRight().getConstant(cache_, idxType, null, false);
//			if (rc == null)
//				throw new ZamiaException("Only constant ranges supported here.", this, cache_);
//
//			int l = lc.getInt(this);
//			int r = rc.getInt(this);
//
//			TargetOperationArraySelRange arraySel = new TargetOperationArraySelRange(l, r, idxType.isAscending(), this, location);
//			return arraySel;
//		}
//	}
//
//	@Override
//	public SigType generateCodeAsTarget(InterpreterCode code_, OperationCache cache_, SigType type_) throws ZamiaException {
//		return generate(code_, cache_, type_);
//	}
//
//	private SigType generate(InterpreterCode code_, OperationCache cache_, SigType type_) throws ZamiaException {
//
//		int nRanges = ranges.size();
//		if (nRanges != 1) {
//			// FIXME: implement
//			throw new ZamiaException("Sorry, multiple ranges not supported yet.", this, cache_);
//		}
//		Range range = ranges.get(0);
//
//		SigType resType = getType(type_, cache_, null);
//
//		Operation left = range.getLeft();
//		Operation right = range.getRight();
//
//		right.generateCode(code_, cache_, SigType.intType);
//		left.generateCode(code_, cache_, SigType.intType);
//		if (range.isAscending()) {
//			code_.add(new PushStmt(Value.getBit(Value.BIT_1), this));
//		} else {
//			code_.add(new PushStmt(Value.getBit(Value.BIT_0), this));
//		}
//
//		code_.add(new ExtRangeStmt((SigTypeArray) type_, this));
//
//		return resType;
//	}
//
//	@Override
//	public SigType generateCode(InterpreterCode code_, OperationCache cache_, SigType type_) throws ZamiaException {
//		return generate(code_, cache_, type_);
//	}

	@Override
	public ZILTargetOperation inlineSubprograms(VariableRemapping aVR, ZILSequenceOfStatements aSOS, RTLCache aCache) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Not implemented method called.");
	}

	@Override
	public ZILTargetOperation getSource() {
		return fSource;
	}

	@Override
	public ZILTargetOperation resolveVariables(Bindings aVb, ZILSequenceOfStatements aSos, ZILClock aClk, RTLCache aCache) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Not implemented method called.");
	}

	@Override
	public void setSource(ZILTargetOperation aSource) {
		fSource = aSource;
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		fSource.computeReadSignals(aReadSignals);
	}

}
