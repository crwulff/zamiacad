/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jul 24, 2005
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
import org.zamia.instgraph.IGSequentialNext;


/**
 * A sequential next statement.
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class SequentialNextStatement extends SequentialStatement {

	private Operation fExp;

	private String fNextLabel;

	public SequentialNextStatement(String aNextLabel, Operation aExp, String aLabel, VHDLNode aParent, long aLocation) {
		super(aLabel, aParent, aLocation);
		fNextLabel = aNextLabel;
		fExp = aExp;
		if (fExp != null)
			fExp.setParent(this);
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	@Override
	public VHDLNode getChild(int aIdx) {
		return fExp;
	}

	@Override
	public void dumpVHDL(int aIndent, PrintStream aOut) {
		// FIXME: implement
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aEE,
			ReferenceSearchResult aRSR, ArrayList<SearchJob> aTODO) throws ZamiaException {
		if (fExp != null)
			fExp.findReferences(aId, aCat, RefType.Read, aDepth + 1, aZPrj, aContainer, aEE, aRSR, aTODO);
	}

	@Override
	public void generateIG(IGSequenceOfStatements aSeq, IGContainer aContainer, IGElaborationEnv aEE) throws ZamiaException {

		IGOperation zExp = fExp != null ? fExp.computeIGOperation(aContainer.findBoolType(), aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null) : null;
		aSeq.add(new IGSequentialNext(fNextLabel, zExp, getLabel(), getLocation(), aEE.getZDB()));

	}

}
