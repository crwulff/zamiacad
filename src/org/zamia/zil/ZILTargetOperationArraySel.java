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
import org.zamia.rtl.RTLGraph;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.RTLSignalAE;
import org.zamia.rtl.RTLTargetArraySel;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILPushRefStmt;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * An elaborated, variable target array selector in the RTL graph
 * 
 * @author Guenter Bartsch
 * 
 */

public class ZILTargetOperationArraySel extends ZILTargetOperation {

	private ZILOperation fIndexExpression;
	private ZILType fSourceType;
	private ZILTargetOperation fSource;

	public ZILTargetOperationArraySel(ZILOperation aIndexExpression, ZILType aSourceType, ZILType aDestinationType, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aDestinationType, aContainer, aSrc);
		fSourceType = aSourceType;
		fIndexExpression = aIndexExpression;
	}

	@Override
	public void generateCode(ZILInterpreterCode aCode, RTLCache aCache) throws ZamiaException {
		fIndexExpression.generateCode(aCode, aCache);
		aCode.add(new ZILPushRefStmt(false, getSrc()));

		fSource.generateCode(aCode, aCache);
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "TargetOperationArraySel {");
		logger.debug(aIndent + 1, "index=%s", fIndexExpression.toString());
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
		return "TargetOperationArraySel(index=" + fIndexExpression + ", src=" + getSrc() + ")";
	}

	@Override
	protected void doElaborate(RTLSignalAE result_, Bindings aLastBindings, RTLCache cache_) throws ZamiaException {
		ZILTypeArray tOut = (ZILTypeArray) getType();

		RTLSignalAE src = getSource().elaborate(aLastBindings, cache_);

		RTLSignal e = fIndexExpression.elaborate(aLastBindings, cache_);

		ZILType tSrc = src.getSignal().getType();
		ZILType tSel = e.getType();

		RTLGraph rtlg = cache_.getGraph();

		RTLSignal res = result_.getSignal();
		RTLSignal en = result_.getEnable();

		RTLTargetArraySel arraySel = new RTLTargetArraySel(tSrc, tOut, tSel, rtlg, null, getSrc());
		rtlg.add(arraySel);

		RTLPort p = arraySel.getD();
		p.setSignal(src.getSignal());
		if (src.getEnable() != null) {
			p = arraySel.getE();
			p.setSignal(src.getEnable());
		}
		p = arraySel.getZ();
		p.setSignal(res);
		p = arraySel.getZE();
		p.setSignal(en);
		p = arraySel.getS();
		p.setSignal(e);

	}

	@Override
	public ZILType getSourceType() {
		return fSourceType;
	}
	
	@Override
	public ZILTargetOperation inlineSubprograms(VariableRemapping aVr_, ZILSequenceOfStatements aSos_, RTLCache aCache_) throws ZamiaException {
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
		fIndexExpression.computeReadSignals(aReadSignals);
		fSource.computeReadSignals(aReadSignals);
	}

}
