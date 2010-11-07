/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 31, 2009
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * Bidirectional parameter access (BUFFER, LINKAGE, INOUT)
 * @author Guenter Bartsch
 *
 */
public class ZILOperationAccessParameter extends ZILOperation {

	private ZILInterfaceVariable fParameter;
	private PortDir fDir;

	public ZILOperationAccessParameter(ZILInterfaceVariable aParameter, PortDir aDir, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aParameter.getType(), aContainer, aSrc);
		fParameter = aParameter;
		fDir = aDir;
	}

	@Override
	protected void doElaborate(RTLSignal result_, Bindings aLastBindings, RTLCache cache_) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException ("Not implemented method called.");
	}

	@Override
	public void generateCode(ZILInterpreterCode code_, RTLCache aCache) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException ("Not implemented method called.");
	}

	@Override
	public ZILClock getClock() throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException ("Not implemented method called.");
	}

	@Override
	public ZILOperation inlineSubprograms(VariableRemapping vr_, ZILSequenceOfStatements sos_, RTLCache cache_) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException ("Not implemented method called.");
	}

	@Override
	public boolean isConstant() throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException ("Not implemented method called.");
	}

	@Override
	public boolean isSynthesizable() throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException ("Not implemented method called.");
	}

	@Override
	public ZILOperation resolveVariables(Bindings vbs_, ZILSequenceOfStatements sos_, RTLCache cache_) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException ("Not implemented method called.");
	}

	public void dump(int indent) {
		// FIXME: implement
		throw new RuntimeException ("Not implemented method called.");
	}

	@Override
	public String toString() {
		return "ZILOperationAccessInterface (intf="+fParameter+", dir="+fDir+")";
	}

	@Override
	public int getNumOperands() {
		return 0;
	}

	@Override
	public ZILOperation getOperand(int aIdx) {
		return null;
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
	}
}
