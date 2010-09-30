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
import org.zamia.rtl.RTLSignalAE;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.synthesis.Binding;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class ZILTargetOperationDestination extends ZILTargetOperation {

	private ZILIReferable fObj;

	private ZILTargetOperation fSource;

	public ZILTargetOperationDestination(ZILIReferable aObj, ZILIContainer aContainer, ASTObject aSrc) throws ZamiaException {
		super(aObj.getType(), aContainer, aSrc);
		fObj = aObj;
	}

	public void generateCode(boolean aInertial, ZILOperation aDelay, ZILOperation aReject, ZILInterpreterCode aCode, RTLCache aCache) throws ZamiaException {

		if (aDelay != null) {
			aDelay.generateCode(aCode, aCache);
		}
		
		if (aReject != null) {
			aReject.generateCode(aCode, aCache);
		}
		
		fObj.generateInterpreterCodeRef(aInertial, aDelay !=null, aReject != null, aCode, aCache);
		fSource.generateCode(aCode, aCache);

	}

	
	@Override
	public void generateCode(ZILInterpreterCode aCode, RTLCache aCache) throws ZamiaException {

		fObj.generateInterpreterCodeRef(false, false, false, aCode, aCache);
		fSource.generateCode(aCode, aCache);

	}

	protected void doElaborate(RTLSignalAE aResult, Bindings aLastBindings, RTLCache aCache) throws ZamiaException {

		throw new ZamiaException("Internal error: ZILTargetOperationDestination.doElaborate() called");

	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "TargetOperationDestination {");
		logger.debug(aIndent + 1, "obj=%s", fObj.toString());
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
		return "TargetOperationDestination(obj=" + fObj + ", src=" + fSource + ")";
	}

	public Binding computeBinding(ZILClock aClock, RTLCache aCache) throws ZamiaException {

		Binding b = new Binding(null, fObj);
		if (aClock != null) {
			b.setClock(aClock);
			b.setSyncValue(this);
		} else {
			b.setASyncValue(this);
		}

		return b;
	}

	public ZILIReferable getObject() {
		return fObj;
	}

	@Override
	public ZILType getSourceType() {
		return fObj.getType();
	}

	@Override
	public ZILTargetOperation inlineSubprograms(VariableRemapping aVr, ZILSequenceOfStatements aSos, RTLCache aCache) throws ZamiaException {
		ZILTargetOperationDestination tod = new ZILTargetOperationDestination(fObj, aSos.getContainer(), getSrc());

		tod.setSource(fSource.inlineSubprograms(aVr, aSos, aCache));

		return tod;
	}

	@Override
	public void setSource(ZILTargetOperation aSource) {
		fSource = aSource;
	}

	@Override
	public ZILTargetOperation getSource() {
		return fSource;
	}

	@Override
	public ZILTargetOperation resolveVariables(Bindings aVb, ZILSequenceOfStatements aSos, ZILClock aClk, RTLCache aCache) throws ZamiaException {

		ZILTargetOperation source = fSource.resolveVariables(aVb, aSos, aClk, aCache);

		if (fObj instanceof ZILVariable) {

			aVb.bind((ZILVariable) fObj, source, aClk);
			
			return null;
		} else {

			ZILTargetOperationDestination tod = new ZILTargetOperationDestination(fObj, aSos.getContainer(), getSrc());

			tod.setSource(source);

			return tod;
		}
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		fSource.computeReadSignals(aReadSignals);
	}

}
