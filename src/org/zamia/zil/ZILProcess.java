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
import org.zamia.rtl.RTLGraph;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.interpreter.ZILInterpreter;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILJumpStmt;
import org.zamia.zil.interpreter.ZILLabel;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class ZILProcess extends ZILConcurrentStatement {

	private ZILSequenceOfStatements fSOS;

	private boolean fPostponed; // FIXME: implement semantics

	public ZILProcess(boolean aPostponed, String aId, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aId, aContainer, aSrc);
	}

	public void setStatementSequence(ZILSequenceOfStatements aSOS) {
		fSOS = aSOS;
	}

	public void dump(int aIndent) {
		if (fPostponed) {
			logger.debug(aIndent, "%s : postponed process {", getId());

		} else {
			logger.debug(aIndent, "%s : process {", getId());
		}
		fSOS.dump(aIndent + 1);
		logger.debug(aIndent, "}");
	}

	@Override
	public String toString() {
		if (fPostponed) {
			return "postponed process (id=" + getId() + ")";
		}
		return "process (id=" + getId() + ")";
	}

	@Override
	public Bindings computeBindings(RTLCache aCache) throws ZamiaException {

		logger.debug("ZILProcess.computeBindings(): ***** computing bindings for process " + this + " *****");

		logger.debug("ZILProcess.computeBindings(): Original code:");
		fSOS.dump(0);

		/*
		 * Phase 1: inline SubPrograms, remove wait stmts, detect global clock
		 */

		logger.debug("ZILProcess.computeBindings(): Phase 1: inline SubPrograms, remove wait stmts, detect global clock");

		int n = fSOS.getNumStatements();
		ZILClock globalClock = null;
		ZILSequenceOfStatements sos = new ZILSequenceOfStatements(this, fSOS.getSrc());
		VariableRemapping vr = new VariableRemapping();

		for (int i = 0; i<n; i++) {
			
			ZILSequentialStatement stmt = fSOS.getStatement(i);
			
			if (stmt instanceof ZILSequentialWait) {

				ZILSequentialWait waitStmt = (ZILSequentialWait) stmt;
				
				if (i == 0) {
					globalClock = waitStmt.getClock();
				}
				
			} else {
				stmt.inlineSubprograms(vr, sos, aCache, null);
			}
		}

		sos.dump(0);

		/*
		 * Phase 2: resolve variables
		 */

		logger.debug("ZILProcess.computeBindings(): Phase 2: resolve variables");
		Bindings vb = new Bindings();
		ZILSequenceOfStatements sos2 = new ZILSequenceOfStatements(this, fSOS.getSrc());

		Bindings lastBindings = sos.resolveVariables(vb, sos2, null, aCache, null);

		logger.debug("ZILProcess.computeBindings(): Variables resolved:");

		sos2.dump(0);

		/*
		 * Phase 3: compute bindings
		 */

		logger.debug("ZILProcess.computeBindings(): Phase 3: compute bindings");
		Bindings bindings = sos2.computeBindings(globalClock, aCache, null);

		logger.debug("ZILProcess.computeBindings(): All done. " + bindings.getNumBindings() + " Bindings computed.");

		lastBindings.merge(bindings);

		//out_.println ("ZILProcess.computeBindings(): Bindings:");
		//bindings.dumpBindings(out_);

		return lastBindings;
	}

	private void generateIntComponent(RTLCache aCache) throws ZamiaException {

		RTLGraph rtlg = aCache.getGraph();

		ZILInterpreter interpreter = new ZILInterpreter(rtlg, getSrc());
		ZILInterpreterCode code = new ZILInterpreterCode(getId());
		interpreter.setCode(code);
		rtlg.add(interpreter);

		ZILLabel labelStart = new ZILLabel();
		code.defineLabel(labelStart);

		fSOS.generateCode(code, aCache, null);

		interpreter.wire();

		code.add(new ZILJumpStmt(labelStart, getSrc()));

		logger.debug("ZILProcess: Finished code generation for process %s. Code dump follows.", getId());
		code.dump();

	}

	@Override
	public void elaborate(RTLCache aCache) throws ZamiaException {

		logger.debug("ZILProcess.elaborate(): generating interpreter component for process %s at %s", getId(), getSrc());

		generateIntComponent(aCache);

	}

	@Override
	public boolean isBindingsProducer(RTLCache aCache) throws ZamiaException {
		
		int n = fSOS.getNumStatements();
		
		for (int i = 0; i<n; i++) {
			
			ZILSequentialStatement stmt = fSOS.getStatement(i);
			
			if (stmt instanceof ZILSequentialWait) {

				// we allow one single wait statement at the beginning
				// if it contains a valid clock expression
				// and we allow a final wait statement representing the signals read by the process
				// everything else is not synthesizable
				
				ZILSequentialWait waitStmt = (ZILSequentialWait) stmt;
				
				if (i == 0 && n>1) {
					if (waitStmt.getClock() != null) {
						continue;
					} 
					return false;
				} 
				
				if (i == n-1) {
					if (waitStmt.getTimeoutClause() == null && waitStmt.getSensitivityList() != null && waitStmt.getConditionClause() == null) {
						continue;
					}
				}
				
				return false;
			} else {
				if (!stmt.isSynthesizable())
					return false;
			}
		}
		
		return true;
	}

	public void appendFinalWait(ArrayList<ZILSignal> aSensSignals) {

		ArrayList<ZILSignal> sensSignals = aSensSignals;

		if (sensSignals == null) {
			HashSetArray<ZILSignal> sens = new HashSetArray<ZILSignal>();
			fSOS.computeReadSignals(sens);

			int n = sens.size();

			if (n > 0) {

				sensSignals = new ArrayList<ZILSignal>(n);

				for (int i = 0; i < n; i++) {
					sensSignals.add(sens.get(i));
				}
			}
		} else if (sensSignals.size() == 0) {
			sensSignals = null;
		}

		fSOS.add(new ZILSequentialWait(null, null, sensSignals, this, getSrc()));
	}

}
