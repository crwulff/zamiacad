/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 22, 2008
 */
package org.zamia.zil;

import java.util.ArrayList;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLSignal;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILJumpEventStmt;
import org.zamia.zil.interpreter.ZILJumpNCStmt;
import org.zamia.zil.interpreter.ZILJumpStmt;
import org.zamia.zil.interpreter.ZILJumpTimeoutStmt;
import org.zamia.zil.interpreter.ZILLabel;
import org.zamia.zil.interpreter.ZILTimeoutStmt;
import org.zamia.zil.interpreter.ZILWaitStmt;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 *
 */
public class ZILSequentialWait extends ZILSequentialStatement {

	private ZILOperation fTimeoutClause;
	private ZILOperation fConditionClause;
	private ArrayList<ZILSignal> fSensitivityList;

	public ZILSequentialWait(ZILOperation aTimeoutClause, ZILOperation aConditionClause, ArrayList<ZILSignal> aSensitivityList, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aContainer, aSrc);
		fTimeoutClause = aTimeoutClause;
		fConditionClause = aConditionClause;
		fSensitivityList = aSensitivityList;
	}

	public void dump(int aIndent) {

		StringBuilder buf = new StringBuilder("wait ");

		if (fTimeoutClause != null) {
			buf.append("for "+fTimeoutClause+" ");
		}
		
		if (fConditionClause != null) {
			buf.append("unit "+fConditionClause+" ");
		}
		
		if (fSensitivityList != null) {
			buf.append("on ");
			int n = fSensitivityList.size();
			for (int i = 0; i<n; i++) {
				buf.append (fSensitivityList.get(i));
				if (i<n-1)
					buf.append(", ");
			}
		}
		
		logger.debug(aIndent, "%s;", buf.toString());
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		if (fSensitivityList != null) {
			int n = fSensitivityList.size();
			for (int i = 0; i<n; i++) {
				buf.append (fSensitivityList.get(i));
			}
		}
		return "wait for "+fTimeoutClause+" until "+fConditionClause+" on "+buf;
	}

	@Override
	public boolean isSynthesizable() throws ZamiaException {
		return false; // 1st / last wait statment in a ZILProcess is handled separately
	}

	public ZILClock getClock() throws ZamiaException {
		return fConditionClause != null && fTimeoutClause == null && fSensitivityList == null ? fConditionClause.getClock() : null;
	}

	@Override
	public Bindings computeBindings(ZILClock aClock, RTLCache aCache, VariableRemapping aVR) throws ZamiaException {
		return null;
	}

	@Override
	public void generateCode(ZILInterpreterCode aCode, RTLCache aCache, ZILLabel aLoopExitLabel) throws ZamiaException {
		
		if (fTimeoutClause != null) {
			fTimeoutClause.generateCode(aCode, aCache);
			aCode.add(new ZILTimeoutStmt(getSrc()));
		}
		
		ZILLabel waitLoopLabel = new ZILLabel();
		ZILLabel waitDoneLabel = new ZILLabel();
		ZILLabel waitCondLabel = new ZILLabel();
		aCode.defineLabel(waitLoopLabel);
		
		aCode.add(new ZILWaitStmt(getSrc()));
		
		if (fTimeoutClause != null) {
			aCode.add(new ZILJumpTimeoutStmt(waitDoneLabel, getSrc()));
		}
		
		if (fSensitivityList != null) {
			
			int n = fSensitivityList.size();
			
			for (int i = 0; i<n; i++) {
				
				ZILSignal zs = fSensitivityList.get(i);
				
				RTLSignal signal = zs.elaborate(null, aCache);
				
				aCode.add(new ZILJumpEventStmt(signal, waitCondLabel, getSrc()));
			}

			aCode.add(new ZILJumpStmt(waitLoopLabel, getSrc()));
		}

		aCode.defineLabel(waitCondLabel);
		
		if (fConditionClause != null) {
			fConditionClause.generateCode(aCode, aCache);
			aCode.add(new ZILJumpNCStmt(waitLoopLabel, getSrc()));
		}
		
		if (fConditionClause == null && fSensitivityList == null) {
			aCode.add(new ZILJumpStmt(waitLoopLabel, getSrc()));
		}

		aCode.defineLabel(waitDoneLabel);
		
	}

	@Override
	protected void inlineSubprograms(VariableRemapping vr_, ZILSequenceOfStatements sos_, RTLCache cache_, String returnVarName_) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Internal Error: unimplemented method called on " + this);
//		SequentialWait sw = new SequentialWait(null, location);
//		Operation cc = conditionClause.inlineSubprograms(vr_, sos_, cache_);
//		sw.setConditionClause(cc);
//		sos_.add(sw);
	}

	@Override
	protected Bindings resolveVariables(Bindings vb_, ZILSequenceOfStatements sos_, ZILClock aClk, RTLCache cache_, VariableRemapping vr_) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Internal Error: unimplemented method called on " + this);
//		SequentialWait sw = new SequentialWait(null, location);
//		// Operation cc = conditionClause.resolveVariables(vr_, sos_, cache_);
//		sw.setConditionClause(conditionClause);
//		sos_.add(sw);
//		return null;
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		if (fConditionClause != null)
			fConditionClause.computeReadSignals(aReadSignals);
		
		if (fTimeoutClause != null)
			fTimeoutClause.computeReadSignals(aReadSignals);
		
		if (fSensitivityList != null) {
			int n = fSensitivityList.size();
			for (int i = 0; i<n; i++) {
				aReadSignals.add(fSensitivityList.get(i));
			}
		}
	}



	public ZILOperation getTimeoutClause() {
		return fTimeoutClause;
	}

	public ZILOperation getConditionClause() {
		return fConditionClause;
	}

	public ArrayList<ZILSignal> getSensitivityList() {
		return fSensitivityList;
	}



}
