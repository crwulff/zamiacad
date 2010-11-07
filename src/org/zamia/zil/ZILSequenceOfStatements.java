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
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILLabel;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class ZILSequenceOfStatements extends ZILSequentialStatement {

	private final static boolean dump = false;

	private ArrayList<ZILSequentialStatement> fStatements;

	public ZILSequenceOfStatements(ZILIContainer aContainer, VHDLNode aSrc) {
		super(aContainer, aSrc);

		fStatements = new ArrayList<ZILSequentialStatement>();
	}

	public void add(ZILSequentialStatement aStatement) {
		fStatements.add(aStatement);
	}

	public void dump(int aIndent) {
		int n = fStatements.size();
		for (int i = 0; i < n; i++) {
			ZILSequentialStatement stmt = fStatements.get(i);
			stmt.dump(aIndent);
		}
	}

	@Override
	public String toString() {
		return "SequenceOfStatements(" + fStatements.size() + " statements)";
	}

	@Override
	protected Bindings resolveVariables(Bindings aVB, ZILSequenceOfStatements aSOS, ZILClock aClk, RTLCache aCache, VariableRemapping aVR) throws ZamiaException {

		Bindings retBindings = new Bindings(aVB);

		int n = getNumStatements();
		for (int i = 0; i < n; i++) {
			ZILSequentialStatement stmt = getStatement(i);

			Bindings tmpBindings = stmt.resolveVariables(retBindings, aSOS, aClk, aCache, aVR);

			retBindings.merge(tmpBindings);
		}

		return retBindings;
	}

	@Override
	public void generateCode(ZILInterpreterCode aCode, RTLCache aCache, ZILLabel aLoopExitLabel) throws ZamiaException {
		for (int i = 0; i < getNumStatements(); i++) {
			ZILSequentialStatement stmt = getStatement(i);
			stmt.generateCode(aCode, aCache, aLoopExitLabel);
		}
	}

	@Override
	protected void inlineSubprograms(VariableRemapping aVR, ZILSequenceOfStatements aSOS, RTLCache aCache, String aReturnVarName) throws ZamiaException {
		int n = getNumStatements();
		for (int i = 0; i < n; i++) {
			ZILSequentialStatement stmt = getStatement(i);

			stmt.inlineSubprograms(aVR, aSOS, aCache, aReturnVarName);
		}
	}

	@Override
	public Bindings computeBindings(ZILClock aClock, RTLCache aCache, VariableRemapping aVR) throws ZamiaException, ZamiaException {

		ZILClock clock = aClock;
		Bindings newBindings = new Bindings();

		for (int i = 0; i < getNumStatements(); i++) {
			ZILSequentialStatement stmt = getStatement(i);

			if (stmt instanceof ZILSequentialWait) {
				if (i > 0)
					throw new ZamiaException("Wait statement has to be the first statement in process to be synthesizeable.", stmt.getSrc());
				if (clock != null)
					throw new ZamiaException("Clock has already been specified in this scope.", stmt.getSrc());
				ZILSequentialWait sw = (ZILSequentialWait) stmt;
				clock = sw.getClock();
				if (dump) {
					logger.debug("Clock detected: %s", clock);
				}
			} else {

				if (dump) {
					logger.debug("SequenceOfStatements: computing bindings for " + stmt);
					logger.debug("SequenceOfStatements: bindings before " + stmt + ": ");
					newBindings.dumpBindings();
				}

				Bindings tmpBindings = stmt.computeBindings(clock, aCache, aVR);

				if (dump) {
					logger.debug("SequenceOfStatements: new bindings from " + stmt + ": ");
					tmpBindings.dumpBindings();
				}

				newBindings.merge(tmpBindings);

				if (dump) {
					logger.debug("SequenceOfStatements: new bindings after merge of results from " + stmt + ": ");
					newBindings.dumpBindings();
				}
			}
		}

		return newBindings;
	}

	public int getNumStatements() {
		return fStatements.size();
	}

	public ZILSequentialStatement getStatement(int aIdx) {
		return fStatements.get(aIdx);
	}

	@Override
	public boolean isSynthesizable() throws ZamiaException {
		
		int n = getNumStatements();
		
		for (int i = 0; i < n; i++) {
			ZILSequentialStatement stmt = getStatement(i);

			if (!stmt.isSynthesizable()) {
				logger.debug("Warning: This statement sequence is not synthesizable because of stmt #" + i + ": " + stmt);
				return false;
			}
		}
		return true;
	}

	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		
		int n = fStatements.size();
		
		for (int i = 0; i<n; i++) {
	
			ZILSequentialStatement stmt = fStatements.get(i);
			
			stmt.computeReadSignals(aReadSignals);
		}
	}

}
