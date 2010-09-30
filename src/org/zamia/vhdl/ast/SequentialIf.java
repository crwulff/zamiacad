/*
 * Copyright 2004-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on 27.06.2004
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
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSequentialIf;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class SequentialIf extends SequentialStatement {

	private Operation fCond;

	private SequenceOfStatements fThenStmt, fElseStmt;

	public SequentialIf(Operation aCond, SequenceOfStatements aThenStmt, String aLabel, ASTObject aParent, long aLocation) {
		super(aLabel, aParent, aLocation);
		setCond(aCond);
		fThenStmt = aThenStmt;
		fThenStmt.setParent(this);
		fElseStmt = null;
	}

	private void setCond(Operation aCond) {
		fCond = aCond;
		fCond.setParent(this);
	}

	public void setElse(SequenceOfStatements aStmts) {
		fElseStmt = aStmts;
		fElseStmt.setParent(this);
	}

	public SequenceOfStatements getElseStmt() {
		return fElseStmt;
	}

	public SequenceOfStatements getThenStmt() {
		return fThenStmt;
	}

	public Operation getCond() {
		return fCond;
	}

	@Override
	public int getNumChildren() {
		return 3;
	}

	@Override
	public ASTObject getChild(int aIdx) {
		switch (aIdx) {
		case 0:
			return fCond;
		case 1:
			return fElseStmt;
		case 2:
			return fThenStmt;
		}
		return null;
	}

	@Override
	public void dumpVHDL(int aIndent, PrintStream aOut) {
		printlnIndented("if " + fCond + " then ", aIndent, aOut);
		fThenStmt.dumpVHDL(aIndent + 2, aOut);
		if (fElseStmt != null) {
			printlnIndented("else", aIndent, aOut);
			fElseStmt.dumpVHDL(aIndent + 2, aOut);
		}
		printlnIndented("end if;", aIndent, aOut);
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aResult, ArrayList<SearchJob> aTODO) throws ZamiaException {
		fCond.findReferences(aId, aCat, RefType.Read, aDepth + 1, aZPrj, aContainer, aCache, aResult, aTODO);

		fThenStmt.findReferences(aId, aCat, aRefType, aDepth + 1, aZPrj, aContainer, aCache, aResult, aTODO);
		if (fElseStmt != null) {
			fElseStmt.findReferences(aId, aCat, aRefType, aDepth + 1, aZPrj, aContainer, aCache, aResult, aTODO);
		}

	}

	@Override
	public void generateIG(IGSequenceOfStatements aSeq, IGContainer aContainer, IGElaborationEnv aEE) throws ZamiaException {
		IGOperation zCond = fCond.computeIGOperation(aContainer.findBoolType(), aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);

		IGSequenceOfStatements thenSequence = new IGSequenceOfStatements(null, getLocation(), aEE.getZDB());
		fThenStmt.generateIG(thenSequence, aContainer, aEE);

		IGSequentialIf zsif = new IGSequentialIf(zCond, thenSequence, getLabel(), getLocation(), aEE.getZDB());

		if (fElseStmt != null) {
			IGSequenceOfStatements elseSequence = new IGSequenceOfStatements(null, fElseStmt.getLocation(), aEE.getZDB());
			fElseStmt.generateIG(elseSequence, aContainer, aEE);
			zsif.setElse(elseSequence);
		}

		aSeq.add(zsif);
	}

}