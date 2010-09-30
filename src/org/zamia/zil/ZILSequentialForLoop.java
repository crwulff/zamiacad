/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.zil;

import org.zamia.DUManager;
import org.zamia.ZamiaException;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILLabel;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * An endless loop.
 * 
 * @author Guenter Bartsch
 * 
 */
public class ZILSequentialForLoop extends ZILSequentialStatement implements ZILIContainer {

	private ZILSequenceOfStatements fStmtSequence;

	private ZILRange fRange;

	private ZILVariable fVar;

	public ZILSequentialForLoop(ZILVariable aVar, ZILRange aRange, ZILIContainer aContainer, ASTObject aSrc) {
		super(aContainer, aSrc);
		fVar = aVar;
		fRange = aRange;
	}

	public void setBody(ZILSequenceOfStatements aStmtSequence) {
		fStmtSequence = aStmtSequence;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "loop {");
		fStmtSequence.dump(aIndent + 1);
		logger.debug(aIndent, "}");
	}

	@Override
	public String toString() {
		return "SequentialForLoop (var=" + fVar + ", range=" + fRange + ")";
	}

	@Override
	public boolean isSynthesizable() throws ZamiaException {
		ZILOperation left = fRange.getLeft();
		ZILOperation right = fRange.getRight();

		ZILValue cl = left.computeConstant();
		ZILValue cr = right.computeConstant();

		return cl != null && cr != null && fStmtSequence.isSynthesizable();
	}

	@Override
	public void generateCode(ZILInterpreterCode code_, RTLCache cache_, ZILLabel loopExitLabel_) throws ZamiaException {

		// FIXME: implement
		throw new RuntimeException("Not implemented method called.");

		//		
		//		// we need a new context for the loop body
		//		// in which we can declare the loop variable
		//
		//		Resolver resolver = new Resolver(getParent().getResolver(cache_), this);
		//		cache_.setResolver(this, resolver);
		//
		//		code_.add(new ZILEnterStmt(this));
		//
		//		// the loop var is always an int (seems harmless)
		//
		//		SigTypeDiscrete typeHint = SigType.intType;
		//
		//		// generate code to evaluate the range
		//		// we don't know anything static about the range,
		//		// not even whether it's ascending or descending
		//
		//		String rangeVarId = fVarId + "__%RANGE%__";
		//
		//		code_.add(new ZILNewVarStmt(rangeVarId, typeHint, this));
		//		resolver.createVariable(rangeVarId, typeHint, this, location);
		//
		//		code_.add(new PushStmt(rangeVarId, this)); // target
		//		range.generateCode(typeHint, code_, cache_);
		//		code_.add(new PopStmt(this));
		//
		//		// generate code to init loop var
		//
		//		code_.add(new ZILNewVarStmt(varId, typeHint, this));
		//		resolver.createVariable(varId, typeHint, this, location);
		//
		//		code_.add(new PushStmt(varId, this)); // target
		//		code_.add(new PushStmt(rangeVarId, this));
		//		code_.add(new RangeStmt(RangeOp.LEFT, null, this));
		//		code_.add(new PopStmt(this));
		//
		//		code_.add(new PushStmt(rangeVarId, this));
		//		code_.add(new RangeStmt(RangeOp.ASCENDING, null, this));
		//
		//		Label descL = new Label();
		//
		//		code_.add(new ZILJumpNCStmt(descL, this));
		//
		//		//
		//		// ascending case
		//		//
		//
		//		// generate loop outer body code
		//
		//		Label labelStartAsc = new Label();
		//		code_.defineLabel(labelStartAsc);
		//
		//		// exit test
		//
		//		Label labelExit = new Label();
		//		code_.add(new PushStmt(varId, this));
		//		code_.add(new PushStmt(rangeVarId, this));
		//		code_.add(new RangeStmt(RangeOp.RIGHT, null, this));
		//		code_.add(new CompareStmt(CompareOp.LESSEQ, this));
		//		code_.add(new ZILJumpNCStmt(labelExit, this));
		//
		//		// generate loop inner body code
		//
		//		body.generateCode(null, code_, cache_, labelExit);
		//
		//		// generate increment statements
		//		code_.add(new PushStmt(varId, this)); // target
		//		code_.add(new PushStmt(varId, this));
		//		code_.add(new PushStmt(new Value(1, SigType.intType), this));
		//		code_.add(new MathStmt(MathOp.ADD, this));
		//		code_.add(new PopStmt(this));
		//
		//		code_.add(new JumpStmt(labelStartAsc, this));
		//
		//		code_.defineLabel(descL);
		//
		//		//
		//		// descending case
		//		//
		//
		//		// generate loop outer body code
		//
		//		ZILLabel labelStartDesc = new ZILLabel();
		//		code_.defineLabel(labelStartDesc);
		//
		//		// exit test
		//
		//		code_.add(new PushStmt(varId, this));
		//		code_.add(new PushStmt(rangeVarId, this));
		//		code_.add(new RangeStmt(RangeOp.RIGHT, null, this));
		//		code_.add(new CompareStmt(CompareOp.GREATEREQ, this));
		//		code_.add(new ZILJumpNCStmt(labelExit, this));
		//
		//		// generate loop inner body code
		//
		//		body.generateCode(null, code_, cache_, labelExit);
		//
		//		// generate decrement statements
		//		code_.add(new PushStmt(varId, this)); // target
		//		code_.add(new PushStmt(varId, this));
		//		code_.add(new PushStmt(new Value(1, SigType.intType), this));
		//		code_.add(new MathStmt(MathOp.SUB, this));
		//		code_.add(new PopStmt(this));
		//
		//		code_.add(new JumpStmt(labelStartDesc, this));
		//
		//		//
		//		// exit code
		//		//
		//
		//		code_.defineLabel(labelExit);
		//		code_.add(new ZILExitContextStmt(this));
	}

	@Override
	protected void inlineSubprograms(VariableRemapping aVR, ZILSequenceOfStatements aSOS, RTLCache aCache, String aReturnVarName) throws ZamiaException {

		int f = 0;
		int t = 0;

		ZILRange range = this.fRange;

		ZILOperation left = range.getLeft();
		ZILOperation right = range.getRight();

		ZILOperation from, to;

		if (range.isAscending()) {
			from = left;
			to = right;
		} else {
			from = right;
			to = left;
		}

		from = from.inlineSubprograms(aVR, aSOS, aCache);
		to = to.inlineSubprograms(aVR, aSOS, aCache);

		ZILValue fv = from.computeConstant();
		if (fv == null) {
			throw new ZamiaException("Constant expected here.", from.getSrc());
		}
		f = fv.getInt(getSrc());

		ZILValue tv = to.computeConstant();
		if (tv == null) {
			throw new ZamiaException("Constant expected here.", to.getSrc());
		}

		t = tv.getInt(getSrc());

		VariableRemapping vr = new VariableRemapping(aVR);

		ZILVariable loopVar = vr.remap(fVar, aSOS.getContainer(), getSrc());

		for (int i = f; i <= t; i++) {

			ZILTargetOperationDestination tod = new ZILTargetOperationDestination(loopVar, aSOS.getContainer(), getSrc());

			ZILValue val = new ZILValue(i, ZILType.intType, aSOS.getContainer(), getSrc());

			tod.setSource(new ZILTargetOperationSource(val, aSOS.getContainer(), getSrc()));

			ZILSequentialAssignment assignment = new ZILSequentialAssignment(tod, true, null, aSOS.getContainer(), getSrc());

			aSOS.add(assignment);

			fStmtSequence.inlineSubprograms(vr, aSOS, aCache, aReturnVarName);
		}
	}

	public void add(ZILIObject aObject, ASTObject aSrc) {
		throw new RuntimeException("Internal error.");
	}

	public ZILIObject resolve(String aId) {
		if (!fVar.getId().equals(aId))
			return getContainer().resolve(aId);
		return fVar;
	}

	public String getId() {
		return null;
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		fRange.computeReadSignals(aReadSignals);
		fStmtSequence.computeReadSignals(aReadSignals);
	}

	public void addEntityImporter(String libId, DUManager adu) {
		throw new RuntimeException ("For loops do not support entity imports.");
	}
}
