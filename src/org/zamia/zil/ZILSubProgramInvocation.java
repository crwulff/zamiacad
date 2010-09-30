/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 28, 2008
 */
package org.zamia.zil;

import java.util.ArrayList;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLSignal;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.interpreter.ZILCallStmt;
import org.zamia.zil.interpreter.ZILEnterNewContextStmt;
import org.zamia.zil.interpreter.ZILExitContextStmt;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class ZILSubProgramInvocation extends ZILOperation {

	private ZILSubProgram fSub;

	private ArrayList<ZILTargetOperationDestination> fParams;

	public ZILSubProgramInvocation(ZILSubProgram aSub, ArrayList<ZILTargetOperationDestination> aParams, ZILIContainer aContainer, ASTObject aSrc) {
		super(aSub.getType(), aContainer, aSrc);
		fSub = aSub;
		fParams = aParams;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("SubProgramInvocation (sub=" + fSub + ", params=");
		// +", type="+getType()+")";

		if (fParams != null) {
			int n = fParams.size();
			for (int i = 0; i < n; i++) {
				ZILIObject param = fParams.get(i);
				buf.append(param);
				if (i < (n - 1)) {
					buf.append(", ");
				}
			}
		} else {
			buf.append("null");
		}

		buf.append(")");

		return buf.toString();
	}

	@Override
	protected void doElaborate(RTLSignal aResult, Bindings aLastBindings, RTLCache aCache) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Sorry, not implemented yet.");
	}

	@Override
	public void generateCode(ZILInterpreterCode aCode, RTLCache aCache) throws ZamiaException {

		// create context

		aCode.add(new ZILEnterNewContextStmt(getSrc()));

		ZILInterfaceList intfs = fSub.getParameters();
		int n = intfs.getNumInterfaces();
		for (int i = 0; i < n; i++) {
			ZILInterface intf = intfs.getInterface(i);

			intf.generateCreationCode(aCode);

		}

		n = fParams.size();
		for (int i = 0; i < n; i++) {

			ZILTargetOperationDestination tod = fParams.get(i);

			tod.generateCode(aCode, aCache);
		}

		aCode.add(new ZILCallStmt(fSub.generateCode(aCache), getSrc()));

		aCode.add(new ZILExitContextStmt(getSrc()));
	}

	@Override
	public ZILClock getClock() throws ZamiaException {

		String id = fSub.getId();
		if (id.equals("RISING_EDGE")) {

			ZILTargetOperationDestination param = fParams.get(0);

			ZILTargetOperation top = param.getSource();

			if (!(top instanceof ZILTargetOperationSource)) {
				throw new ZamiaException("Signal parameter expected here.", getSrc());
			}

			ZILTargetOperationSource src = (ZILTargetOperationSource) top;

			ZILOperation obj = src.getObj();

			if (!(obj instanceof ZILOperationReadSignal)) {
				throw new ZamiaException("Signal parameter expected here.", getSrc());
			}

			return new ZILClock(((ZILOperationReadSignal) obj).getSignal(), true);
		} else if (id.equals("FALLING_EDGE")) {
			ZILTargetOperationDestination param = fParams.get(0);

			ZILTargetOperation top = param.getSource();

			if (!(top instanceof ZILTargetOperationSource)) {
				throw new ZamiaException("Signal parameter expected here.", getSrc());
			}

			ZILTargetOperationSource src = (ZILTargetOperationSource) top;

			ZILOperation obj = src.getObj();

			if (!(obj instanceof ZILOperationReadSignal)) {
				throw new ZamiaException("Signal parameter expected here.", getSrc());
			}

			return new ZILClock(((ZILOperationReadSignal) obj).getSignal(), false);
		}
		return null;
	}

	@Override
	public int getNumOperands() {
		// FIXME: implement
		throw new RuntimeException("Sorry, not implemented yet.");
	}

	@Override
	public ZILOperation getOperand(int aIdx) {
		// FIXME: implement
		throw new RuntimeException("Sorry, not implemented yet.");
	}

	@Override
	public ZILOperation inlineSubprograms(VariableRemapping aVR, ZILSequenceOfStatements aSOS, RTLCache aCache) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Sorry, not implemented yet.");
	}

	@Override
	public boolean isConstant() throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Sorry, not implemented yet.");
	}

	@Override
	public boolean isSynthesizable() throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Sorry, not implemented yet.");
	}

	@Override
	public ZILOperation resolveVariables(Bindings aVBs, ZILSequenceOfStatements aSOS, RTLCache aCache) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Sorry, not implemented yet.");
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {

		if (fParams != null) {
			int n = fParams.size();
			for (int i = 0; i < n; i++) {
				fParams.get(i).computeReadSignals(aReadSignals);
			}
		}
	}

}
