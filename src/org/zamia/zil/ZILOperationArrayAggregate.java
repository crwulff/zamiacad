/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 30, 2008
 */
package org.zamia.zil;

import java.util.ArrayList;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLSignal;
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
public class ZILOperationArrayAggregate extends ZILOperation {

	class AggEntry {
		public ZILOperation fPosOp;
		public ZILRange fPosRange;

		public ZILOperation fObj;

		public AggEntry(ZILOperation aPos, ZILOperation aObj) {
			fPosOp = aPos;
			fObj = aObj;
		}
		public AggEntry(ZILRange aRange, ZILOperation aObj) {
			fPosRange = aRange;
			fObj = aObj;
		}
	}

	private ArrayList<AggEntry> fEntries;
	private ArrayList<ZILOperation> fPositionalEntries;
	private ZILOperation fOthers;

	public ZILOperationArrayAggregate(ZILType aType, ZILIContainer aContainer, ASTObject aSrc) {
		super(aType, aContainer, aSrc);
		fEntries = new ArrayList<AggEntry>();
		fPositionalEntries = new ArrayList<ZILOperation>();
	}

	public void set(ZILRange aRange, ZILOperation aObj) {
		fEntries.add(new AggEntry(aRange, aObj));
	}
	public void set(ZILOperation aOperation, ZILOperation aObj) {
		fEntries.add(new AggEntry(aOperation, aObj));
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("OperationArrayAggregate(");

		int n = fEntries.size();
		for (int i = 0; i < n; i++) {
			AggEntry assignment = fEntries.get(i);
			if (assignment.fPosOp != null)
				buf.append(assignment.fPosOp);
			else
				buf.append(assignment.fPosRange);
			buf.append(":=");
			buf.append(assignment.fObj);
			if (i < (n - 1)) {
				buf.append(", ");
			}
		}
		buf.append(", others="+fOthers);
		buf.append(")");
		return buf.toString();
	}

	@Override
	protected void doElaborate(RTLSignal result_, Bindings aLastBindings, RTLCache cache_) throws ZamiaException {
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
		//		int n = aggregate.getNumElements();
		//		Aggregate nAggregate = new Aggregate(null, location);
		//		for (int i = 0; i < n; i++) {
		//			ElementAssociation ea = aggregate.getElement(i);
		//			Operation o = ea.getExpression();
		//			Operation oexp = o.inlineSubprograms(vr_, sos_, cache_);
		//			nAggregate.add(new ElementAssociation(ea.getChoices(), oexp, nAggregate, location));
		//		}
		//
		//		Operation others = aggregate.getOthers();
		//		if (others != null) {
		//			nAggregate.setOthers(others.inlineSubprograms(vr_, sos_, cache_));
		//		}
		//
		//		return new OperationAggregate(nAggregate, null, location);
	}

	@Override
	public ZILOperation resolveVariables(Bindings vbs_, ZILSequenceOfStatements sos_, RTLCache cache_) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException ("Not implemented method called.");
		//		int n = aggregate.getNumElements();
		//		Aggregate nAggregate = new Aggregate(null, location);
		//		for (int i = 0; i < n; i++) {
		//			ElementAssociation ea = aggregate.getElement(i);
		//			Operation o = ea.getExpression();
		//			Operation oexp = o.resolveVariables(vr_, sos_, cache_);
		//			nAggregate.add(new ElementAssociation(ea.getChoices(), oexp, nAggregate, location));
		//		}
		//		Operation others = aggregate.getOthers();
		//		if (others != null) {
		//			nAggregate.setOthers(others.resolveVariables(vr_, sos_, cache_));
		//		}
		//		return new OperationAggregate(nAggregate, null, location);
	}

	@Override
	public boolean isSynthesizable() throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException ("Not implemented method called.");
//		int n = fEntries.size();
//		for (int i = 0; i < n; i++) {
//
//			AggEntry ea = fEntries.get(i);
//			if (!ea.fObj.isSynthesizable(vr_, cache_))
//				return false;
//		}
//
//		return true;
	}

	@Override
	public void generateCode(ZILInterpreterCode aCode, RTLCache aCache) throws ZamiaException {
		throw new ZamiaException ("Sorry, this code is currently broken.");
		
		//		ZILTypeArray at = (ZILTypeArray) getType();
//		//		ZILType elementType = at.getElementType();
//
//		int n = fEntries.size();
//		for (int i = 0; i < n; i++) {
//
//			AggEntry entry = fEntries.get(i);
//
//			aCode.add(new ZILPushStmt(new ZILValue(entry.fPos, ZILType.intType, null, getSrc()), getSrc()));
//			entry.fObj.generateCode(aCode, aCache);
//		}
//
//		aCode.add(new ZILArrayAggregateStmt(at, n, getSrc()));
	}

	@Override
	public boolean isConstant() throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException ("Not implemented method called.");
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
			
			AggEntry entry = fEntries.get(i);
			
			entry.fObj.computeReadSignals(aReadSignals);
		}
	}

	public void setOthers(ZILOperation aOthers) {
		fOthers = aOthers;
	}

	public void add(ZILOperation aObj) {
		fPositionalEntries.add(aObj);
	}


}
