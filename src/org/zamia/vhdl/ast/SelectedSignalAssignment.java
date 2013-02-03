/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jul 16, 2005
 */

package org.zamia.vhdl.ast;

import java.io.PrintStream;
import java.util.ArrayList;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationBinary;
import org.zamia.instgraph.IGOperationBinary.BinOp;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGProcess;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSequentialIf;
import org.zamia.instgraph.IGStructure;
import org.zamia.instgraph.IGType;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class SelectedSignalAssignment extends ConcurrentSignalAssignment {

	public static final boolean dump = false;

	private ArrayList<SelectedWaveform> fSWS;

	private Target fTarget;

	private Operation fWithExpr;

	public SelectedSignalAssignment(Operation aWithExpr, Target aTarget, VHDLNode aParent, long aLocation) {
		super(aParent, aLocation);
		fSWS = new ArrayList<SelectedWaveform>();
		fTarget = aTarget;
		fTarget.setParent(this);
		fWithExpr = aWithExpr;
		fWithExpr.setParent(this);
	}

	public void add(SelectedWaveform aSW) {
		fSWS.add(aSW);
		aSW.setParent(this);

	}

	public int getNumSW() {
		return fSWS.size();
	}

	protected SelectedWaveform getSW(int aIdx) {
		return fSWS.get(aIdx);
	}

	@Override
	public int getNumChildren() {
		return fSWS.size() + 2;
	}

	@Override
	public VHDLNode getChild(int aIdx) {
		switch (aIdx) {
		case 0:
			return fWithExpr;
		case 1:
			return fTarget;
		}
		return fSWS.get(aIdx - 2);
	}

	@Override
	public void dumpVHDL(int aIndent, PrintStream aOut) {
		printlnIndented(toString(), aIndent, aOut);
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {

		fTarget.findReferences(id_, category_, RefType.Write, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);

		fWithExpr.findReferences(id_, category_, RefType.Read, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);

		int n = fSWS.size();

		for (int i = 0; i < n; i++) {

			SelectedWaveform sw = fSWS.get(i);

			sw.findReferences(id_, category_, RefType.Read, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		}
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();

		if (fLabel != null) {
			buf.append(fLabel + ": ");
		}

		if (fPostponed) {
			buf.append("POSTPONED ");
		}

		buf.append("WITH " + fWithExpr + " SELECT " + fTarget + " <= ");

		if (guarded) {
			buf.append("GUARDED ");
		}

		if (delayMechanism != null) {
			buf.append(delayMechanism.toString());
			buf.append(" ");
		}

		int n = fSWS.size();
		for (int i = 0; i < n; i++) {
			buf.append(fSWS.get(i));
			if (i < n - 1) {
				buf.append(", ");
			}
		}

		return buf.toString();
	}

	public void computeIG(DMUID aDUUID, IGContainer aContainer, IGStructure aStructure, IGElaborationEnv aEE) throws ZamiaException {
		/*
		 * let's simply turn this into a small process
		 */

		IGProcess proc = IGProcess.create(fPostponed, aContainer.getDBID(), fLabel, getLocation(), aEE.getZDB());
		
		IGSequenceOfStatements sos = new IGSequenceOfStatements(fLabel, getLocation(), aEE.getZDB());
		proc.setStatementSequence(sos);

		IGSequenceOfStatements cur = sos;

		IGType bool = aContainer.findBoolType();
		
		IGOperation expr = fWithExpr.computeIGOperation(null, proc.getContainer(), aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);

		int n = fSWS.size();
		for (int i = 0; i < n; i++) {
			SelectedWaveform sw = fSWS.get(i);

			Waveform wv = sw.getWaveform();
			ArrayList<Range> choices = sw.getChoices();

			if (choices != null && choices.size() > 0 && choices.get(0) != null) {
				IGSequenceOfStatements thenStmt = new IGSequenceOfStatements(null, sw.getLocation(), aEE.getZDB());

				wv.generateIGSequence(fTarget, delayMechanism, thenStmt, proc.getContainer(), aEE);

				IGOperation cond = null;
				SourceLocation condLoc = null;

				int m = choices.size();
				for (int j = 0; j < m; j++) {

					Range r = choices.get(j);

					if (r == null)
						throw new ZamiaException("Selected Signal Assignment: Illegal others choice.", sw);

					IGOperation op = r.computeIG(expr.getType(), proc.getContainer(), aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);
					IGType opT = op.getType();
					
					IGOperation o;
					if (!opT.isRange()) {

						o = new IGOperationBinary(expr, op, BinOp.EQUAL, bool, r.getLocation(), aEE.getZDB());

					} else {

						IGOperation min = op.getRangeMin(proc.getContainer(), r.getLocation());
						IGOperation max = op.getRangeMax(proc.getContainer(), r.getLocation());

						IGOperation o1 = new IGOperationBinary(expr, min, BinOp.GREATEREQ, bool, r.getLocation(), aEE.getZDB());
						IGOperation o2 = new IGOperationBinary(expr, max, BinOp.LESSEQ, bool, r.getLocation(), aEE.getZDB());

						o = new IGOperationBinary(o1, o2, BinOp.AND, bool, r.getLocation(), aEE.getZDB());
					}

					if (cond == null) {
						cond = o;
						condLoc = r.getLocation();
					} else {
						cond = new IGOperationBinary(cond, o, BinOp.OR, bool, r.getLocation(), aEE.getZDB());
					}
				}

				IGSequentialIf si = new IGSequentialIf(cond, thenStmt, null, condLoc, aEE.getZDB());
				cur.add(si);

				IGSequenceOfStatements elseStmt = new IGSequenceOfStatements(null, condLoc, aEE.getZDB());
				si.setElse(elseStmt);
				cur = elseStmt;

			} else {

				// others

				if (i != (n - 1)) {
					throw new ZamiaException("others waveform has to be the last one.", sw);
				}
				wv.generateIGSequence(fTarget, delayMechanism, cur, proc.getContainer(), aEE);
			}
		}

		proc.appendFinalWait(null);
		
		aStructure.addStatement(proc);
	}

}
