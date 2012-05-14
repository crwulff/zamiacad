/*
 * Copyright 2004-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.vhdl.ast;

import java.io.PrintStream;
import java.util.ArrayList;

import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGOperationObject;
import org.zamia.instgraph.IGProcess;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSequentialIf;
import org.zamia.instgraph.IGSequentialSetConnection;
import org.zamia.instgraph.IGStructure;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGObject.IGObjectCat;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class ConditionalSignalAssignment extends ConcurrentSignalAssignment {

	private Target fTarget;

	private ArrayList<ConditionalWaveform> fCWS; // of ConditionalWaveform

	public ConditionalSignalAssignment(Target aTarget, VHDLNode aParent, long aLocation) {
		super(aParent, aLocation);
		fCWS = new ArrayList<ConditionalWaveform>();
		setTarget(aTarget);
	}

	public ConditionalSignalAssignment(VHDLNode aParent, long aLocation) {
		this(null, aParent, aLocation);
	}

	public void setTarget(Target aTarget) {
		fTarget = aTarget;
		if (fTarget != null)
			fTarget.setParent(this);
	}

	public void add(Waveform aWaveform, Operation aCond, long aLocation) {
		fCWS.add(new ConditionalWaveform(aWaveform, aCond, this, aLocation));
		aWaveform.setParent(this);
		if (aCond != null)
			aCond.setParent(this);
	}

	public void add(ConditionalWaveform aCW) {
		fCWS.add(aCW);
		aCW.setParent(this);
	}

	public int getNumConditionalWaveforms() {
		return fCWS.size();
	}

	public ConditionalWaveform getConditionalWaveform(int aIdx) {
		return (ConditionalWaveform) fCWS.get(aIdx);
	}

	public int getNumCW() {
		return fCWS.size();
	}

	protected ConditionalWaveform getCW(int aIdx) {
		return fCWS.get(aIdx);
	}

	public String toString() {

		StringBuilder buf = new StringBuilder();

		if (fLabel != null) {
			buf.append(fLabel + ": ");
		}

		if (fPostponed) {
			buf.append("POSTPONED ");
		}

		buf.append(fTarget.toVHDL() + " <= ");

		if (guarded) {
			buf.append("GUARDED ");
		}

		if (delayMechanism != null) {
			buf.append(delayMechanism.toString());
			buf.append(" ");
		}

		int n = fCWS.size();
		for (int i = 0; i < n; i++) {
			ConditionalWaveform cw = fCWS.get(i);

			buf.append(cw.toString());
			if (i < n - 1) {
				buf.append(" ELSE ");
			}

		}

		buf.append(";");

		return buf.toString();
	}

	public void dump() {
		System.out.println(toString());
	}

	public Target getTarget() {
		return fTarget;
	}

	@Override
	public int getNumChildren() {
		return fCWS.size() + 1;
	}

	@Override
	public VHDLNode getChild(int idx_) {
		if (idx_ >= fCWS.size())
			return fTarget;
		return fCWS.get(idx_);
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aRSR, ArrayList<SearchJob> aTODO) throws ZamiaException {

		if (aCat != ObjectCat.Signal)
			return;

		fTarget.findReferences(aId, aCat, RefType.Write, aDepth + 1, aZPrj, aContainer, aCache, aRSR, aTODO);

		int n = getNumConditionalWaveforms();
		for (int i = 0; i < n; i++) {
			ConditionalWaveform cw = getConditionalWaveform(i);
			cw.findReferences(aId, aCat, RefType.Read, aDepth + 1, aZPrj, aContainer, aCache, aRSR, aTODO);
		}
	}

	@Override
	public void dumpVHDL(int aIndent, PrintStream aOut) {
		printlnIndented(toString(), aIndent, aOut);
	}

	public void computeIG(DMUID aDUUID, IGContainer aContainer, IGStructure aStruct, IGElaborationEnv aEE) throws ZamiaException {

		/*
		 * let's simply turn this into a small process
		 */

		IGProcess proc = new IGProcess(fPostponed, aContainer.getDBID(), fLabel, getLocation(), aEE.getZDB());

		IGSequenceOfStatements sos = new IGSequenceOfStatements(fLabel, getLocation(), aEE.getZDB());
		proc.setStatementSequence(sos);

		IGSequenceOfStatements cur = sos;

		/*
		 * take care of guard expression
		 */

		if (guarded) {

			IGSequenceOfStatements thenSOS = new IGSequenceOfStatements(null, getLocation(), aEE.getZDB());

			IGObject guardSignal = aContainer.resolveObject("GUARD");

			if (guardSignal == null) {
				throw new ZamiaException("Couldn't resolve guard signal", this);
			}

			if (guardSignal.getCat() != IGObjectCat.SIGNAL) {
				throw new ZamiaException("Guard item is not a signal: " + guardSignal, this);
			}

			IGType guardType = guardSignal.getType();

			if (!guardType.isBool()) {
				throw new ZamiaException("Guard signal is not boolean.", this);
			}

			IGOperation guardExpr = new IGOperationObject(guardSignal, getLocation(), aEE.getZDB());

			IGSequentialIf guardIf = new IGSequentialIf(guardExpr, thenSOS, null, getLocation(), aEE.getZDB());

			sos.add(guardIf);

			cur = thenSOS;

			thenSOS.add(new IGSequentialSetConnection(true, null, getLocation(), aEE.getZDB()));

			// disconnect otherwise

			IGSequenceOfStatements elseSOS = new IGSequenceOfStatements(null, getLocation(), aEE.getZDB());

			elseSOS.add(new IGSequentialSetConnection(false, null, getLocation(), aEE.getZDB()));

			guardIf.setElse(elseSOS);
		}

		IGSequentialIf si = null;
		int n = getNumConditionalWaveforms();
		for (int i = 0; i < n; i++) {
			ConditionalWaveform cw = getCW(i);

			Waveform wv = cw.getWaveform();
			Operation cond = cw.getCond();

			if (si != null) {
				IGSequenceOfStatements elseStmt = new IGSequenceOfStatements(null, wv.getLocation(), aEE.getZDB());
				si.setElse(elseStmt);
				cur = elseStmt;
			}

			if (cond != null) {

				IGType boolType = aContainer.findBoolType();

				IGOperation zCond = cond.computeIGOperation(boolType, aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);

				IGSequenceOfStatements thenStmt = new IGSequenceOfStatements(null, wv.getLocation(), aEE.getZDB());

				wv.generateIGSequence(fTarget, delayMechanism, thenStmt, aContainer, aEE);

				si = new IGSequentialIf(zCond, thenStmt, null, cond.getLocation(), aEE.getZDB());
				cur.add(si);

			} else {
				wv.generateIGSequence(fTarget, delayMechanism, cur, aContainer, aEE);
			}
		}

		proc.appendFinalWait(null);

		aStruct.addStatement(proc);
	}

}