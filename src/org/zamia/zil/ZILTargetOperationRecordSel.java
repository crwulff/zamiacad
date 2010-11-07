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
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * An elaborated, record selector in the RTL graph
 * 
 * @author Guenter Bartsch
 * 
 */

public class ZILTargetOperationRecordSel extends ZILTargetOperation {

	private ZILRecordField fRF;

	private ZILType fSourceType;

	private ZILTargetOperation fSource;

	public ZILTargetOperationRecordSel(ZILRecordField aRF, ZILType aSourceType, ZILType aDestinationType, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aDestinationType, aContainer, aSrc);
		fRF = aRF;
		fSourceType = aSourceType;
	}

	@Override
	public void generateCode(ZILInterpreterCode aCode, RTLCache aCache) throws ZamiaException {

		aCode.add(new ZILPushRefStmt(fRF, getSrc()));

		fSource.generateCode(aCode, aCache);

	}

	@Override
	protected void doElaborate(RTLSignalAE result, Bindings aLastBindings, RTLCache cache) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");
		//		
		//		if (!(tOut_ instanceof SigTypeRecord)) {
		//			throw new ZamiaException ("Internal error: record type expected", this, cache_);
		//		}
		//		
		//		SigTypeRecord tOut = (SigTypeRecord) tOut_;
		//		
		//		RTLSignalAE src = this.src.elaborate(cache_, tOut.getRecordField(id).type);
		//		
		//		SigType tSrc = src.getSignal().getType();
		//		RTLGraph rtlg = cache_.getRTLGraph();
		//		
		//		RTLSignal res = result_.getSignal();
		//		RTLSignal en = result_.getEnable();
		//		
		//		RTLTargetRecordSel arraySel = new RTLTargetRecordSel(tSrc, tOut, id, rtlg, null, this);
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
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "TargetOperationRecordSel {");
		logger.debug(aIndent + 1, "id=%s", fRF);
		ZILTargetOperation src = getSource();
		if (src != null) {
			src.dump(aIndent + 1);
		} else {
			logger.debug(aIndent + 1, "src=null");
		}
		logger.debug(aIndent, "}");
	}

	@Override
	public String toString() {
		return "TargetOperationRecordSel(rf=" + fRF + ", src=" + getSource() + ")";
	}

	@Override
	public ZILType getSourceType() {
		return fSourceType;
	}

	//	@Override
	//	public TargetOperation elaborateAsTarget(OperationCache cache_, SigType resType_, VariableRemapping vr_) throws ZamiaException {
	//		TargetOperationRecordSel recordSel = new TargetOperationRecordSel(suffix.getId(), location);
	//		return recordSel;
	//	}

	@Override
	public ZILTargetOperation inlineSubprograms(VariableRemapping aVr, ZILSequenceOfStatements aSos, RTLCache aCache) throws ZamiaException {
		ZILTargetOperationRecordSel top = new ZILTargetOperationRecordSel(fRF, fSourceType, getType(), aSos.getContainer(), getSrc());

		top.setSource(getSource().inlineSubprograms(aVr, aSos, aCache));

		return top;
	}

	@Override
	public ZILTargetOperation resolveVariables(Bindings aVb, ZILSequenceOfStatements aSos, ZILClock aClk, RTLCache aCache) throws ZamiaException {
		ZILTargetOperationRecordSel top = new ZILTargetOperationRecordSel(fRF, fSourceType, getType(), aSos.getContainer(), getSrc());

		top.setSource(getSource().resolveVariables(aVb, aSos, aClk, aCache));

		return top;
	}

	@Override
	public ZILTargetOperation getSource() {
		return fSource;
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
