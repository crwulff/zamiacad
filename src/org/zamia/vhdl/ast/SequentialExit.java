/*
 * Copyright 2006-2009 by the authors indicated in the @author tags.
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
import org.zamia.instgraph.IGSequentialExit;
import org.zamia.instgraph.IGType;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class SequentialExit extends SequentialStatement {

	private String fExitLabel;

	private Operation fCond;

	public SequentialExit(String aExitLabel, Operation aCond, String aLabel, ASTObject aParent, long aLocation) {
		super(aLabel, aParent, aLocation);
		fCond = aCond;
		if (fCond != null) {
			fCond.setParent(this);
		}
		fExitLabel = aExitLabel;
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	@Override
	public ASTObject getChild(int aIdx) {
		return fCond;
	}

	@Override
	public void dumpVHDL(int aIndent, PrintStream aOut) {

		StringBuilder buf = new StringBuilder();

		if (getLabel() != null) {
			buf.append(getLabel() + ": ");
		}

		buf.append("EXIT ");

		if (fExitLabel != null) {
			buf.append(fExitLabel.toString());
			buf.append(" ");
		}

		if (fCond != null) {
			buf.append("WHEN ");
			buf.append(fCond.toVHDL());
		}

		buf.append(";");
		printlnIndented(buf.toString(), aIndent, aOut);
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aEE,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
	}

	@Override
	public void generateIG(IGSequenceOfStatements aSeq, IGContainer aContainer, IGElaborationEnv aEE) throws ZamiaException {
		IGType b = aContainer.findBoolType();

		IGOperation zCond = fCond != null ? fCond.computeIGOperation(b, aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null) : null;

		aSeq.add(new IGSequentialExit(fExitLabel, zCond, getLabel(), getLocation(), aEE.getZDB()));
	}

}
