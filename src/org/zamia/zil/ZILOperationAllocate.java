/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 24, 2009
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLSignal;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * FIXME: right now this is just a placeholder
 * 
 * @author Guenter Bartsch
 *
 */
public class ZILOperationAllocate extends ZILOperation {

	public ZILOperationAllocate(ZILType type, ZILIContainer container, VHDLNode src) {
		super(type, container, src);
		// TODO Auto-generated constructor stub
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
		// TODO Auto-generated method stub
//		// FIXME SigType t = td.elaborate(cache_, null);
//		
//		code_.add(new NewStmt(this));

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
	public boolean isConstant() throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException ("Not implemented method called.");
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
