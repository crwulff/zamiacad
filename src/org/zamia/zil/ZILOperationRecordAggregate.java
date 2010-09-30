/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 30, 2008
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLSignal;
import org.zamia.util.HashMapArray;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 *
 */
public class ZILOperationRecordAggregate extends ZILOperation {

	class Entry {
		public ZILRecordField fRF;
		public ZILOperation fObj;
		public Entry(ZILRecordField aRF, ZILOperation aObj) {
			fRF = aRF;
			fObj = aObj;
		}
		
	}

	private HashMapArray<ZILRecordField, Entry> fEntries;
	
	public ZILOperationRecordAggregate(ZILType aType, ZILIContainer aContainer, ASTObject aSrc) {
		super(aType, aContainer, aSrc);
		
		fEntries = new HashMapArray<ZILRecordField, Entry>();
		
	}

	public void set(ZILRecordField aRF, ZILOperation aObj) {
		fEntries.put(aRF, new Entry(aRF, aObj));
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("OperationRecordAggregate (");
		
		int n = fEntries.size();
		for (int i = 0; i<n; i++) {
			Entry entry = fEntries.get(i);
			buf.append(entry.fRF.id);
			buf.append(":=");
			buf.append(entry.fObj);
			if (i<(n-1)) {
				buf.append(", ");
			}
		}
		buf.append(")");
		return buf.toString();
	}
	@Override
	public boolean isConstant() throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException ("Not implemented method called.");
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
		throw new RuntimeException ("Not implemented method called.");
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

	@Override
	public int getNumOperands() {
		return fEntries.size();
	}

	@Override
	public ZILOperation getOperand(int aIdx) {
		return fEntries.get(aIdx).fObj;
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		int n = fEntries.size();
		for (int i = 0; i<n; i++) {
			Entry entry = fEntries.get(i);
			entry.fObj.computeReadSignals(aReadSignals);
		}
	}

}
