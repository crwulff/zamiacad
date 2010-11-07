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
import org.zamia.rtl.RTLSignal;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.vhdl.ast.Suffix;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class ZILOperationSuffix extends ZILOperation {

	private ZILOperation fObject;

	private Suffix fSuffix;

	public ZILOperationSuffix(ZILOperation aObject, Suffix aSuffix, ZILType aType, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aType, aContainer, aSrc);
		fSuffix = aSuffix;
		fObject = aObject;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		return "OperationSuffix (suffix=" + fSuffix + ", obj=" + fObject + ")";
	}

	@Override
	protected void doElaborate(RTLSignal result_, Bindings aLastBindings, RTLCache cache_) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");
		//
		//		RTLGraph rtlg = cache_.getRTLGraph();
		//
		//		SigType type = getType(s_.getType(), cache_, vr_);
		//
		//		return rtlg.placeRecordSel(s_, suffix.getId(), type, this);
	}

	@Override
	public void generateCode(ZILInterpreterCode code_, RTLCache aCache) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");
		//
		//		if (tIn_ instanceof SigTypeAccess) {
		//			if (!(suffix.getType() == SuffixType.ALL))
		//				throw new ZamiaException("ALL expected.", suffix, cache_);
		//
		//			SigTypeAccess sta = (SigTypeAccess) tIn_;
		//			SigType elementType = sta.getSubtype();
		//			code_.add(new ExtSuffixStmt("ALL", elementType, this));
		//			return elementType;
		//		}
		//
		//		if (!(tIn_ instanceof SigTypeRecord))
		//			throw new ZamiaException("Tried to use suffix on something that is not a record type.", this, cache_);
		//
		//		if (!(suffix.getType() == SuffixType.ID))
		//			throw new ZamiaException("Identifier expected.", suffix, cache_);
		//
		//		SigTypeRecord rt = (SigTypeRecord) tIn_;
		//
		//		String id = suffix.getId();
		//
		//		RecordField rf = rt.findRecordField(id, this);
		//
		//		SigType elementType = rf.type;
		//
		//		code_.add(new ExtSuffixStmt(id, elementType, this));
		//
		//		return elementType;
	}

	@Override
	public boolean isConstant() throws ZamiaException {
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
		return 1;
	}

	@Override
	public ZILOperation getOperand(int aIdx) {
		return fObject;
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		fObject.computeReadSignals(aReadSignals);
	}

}
