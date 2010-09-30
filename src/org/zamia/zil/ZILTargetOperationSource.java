/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Mar 17, 2007
 */

package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLGraph;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.RTLSignalAE;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILPopStmt;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class ZILTargetOperationSource extends ZILTargetOperation {

	private ZILOperation fObj;

	public ZILTargetOperationSource(ZILOperation aObj, ZILIContainer aContainer, ASTObject aSrc) {
		super(aObj.getType(), aContainer, aSrc);
		fObj = aObj;
	}

	@Override
	public void generateCode(ZILInterpreterCode aCode, RTLCache aCache) throws ZamiaException {
		fObj.generateCode(aCode, aCache);
		
		aCode.add(new ZILPopStmt(getSrc()));
	}

	@Override
	protected void doElaborate(RTLSignalAE aResult, Bindings aLastBindings, RTLCache aCache) throws ZamiaException {
		RTLSignal s = fObj.elaborate(aLastBindings, aCache);

		RTLGraph rtlg = aCache.getGraph();

		RTLSignal one = rtlg.placeOne(getType(), getSrc());

		RTLSignal res = aResult.getSignal();
		RTLSignal en = aResult.getEnable();

		rtlg.sigJoin(s, res, getSrc());
		rtlg.sigJoin(one, en, getSrc());
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "TargetOperationSource {");
		logger.debug(aIndent + 1, "obj=%s", fObj.toString());
		logger.debug(aIndent, "}");
	}

	@Override
	public String toString() {
		return "TargetOperationSource(obj=" + fObj + ")";
	}

	@Override
	public ZILType getSourceType() {
		return fObj.getType();
	}

	@Override
	public ZILTargetOperation inlineSubprograms(VariableRemapping aVr_, ZILSequenceOfStatements aSos_, RTLCache aCache_) throws ZamiaException {

		return new ZILTargetOperationSource(fObj.inlineSubprograms(aVr_, aSos_, aCache_), aSos_.getContainer(), getSrc());

	}

	@Override
	public ZILTargetOperation resolveVariables(Bindings aVb, ZILSequenceOfStatements aSos, ZILClock aClk, RTLCache aCache) throws ZamiaException {
		return new ZILTargetOperationSource(fObj.resolveVariables(aVb, aSos, aCache), aSos.getContainer(), getSrc());
	}

	@Override
	public ZILTargetOperation getSource() {
		throw new RuntimeException("Internal error: getSource() called on ZILTargetOperationSource.");
	}

	@Override
	public void setSource(ZILTargetOperation aSource) {
		throw new RuntimeException("Internal error: setSource() called on ZILTargetOperationSource.");
	}

	public ZILOperation getObj() {
		return fObj;
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		fObj.computeReadSignals(aReadSignals);
	}
}
