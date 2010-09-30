/*
 * Copyright 2008-2009 by the authors indicated in the @author tags.
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
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSequentialReport;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class SequentialReport extends SequentialStatement {

	private Operation exp;

	private Operation severity;

	public SequentialReport(Operation exp_, Operation severity_, String label_, ASTObject parent_, long location_) {
		super(label_, parent_, location_);
		exp = exp_;
		exp.setParent(this);
		if (severity_ != null) {
			severity = severity_;
			severity.setParent(this);
		}
	}

	@Override
	public void dumpVHDL(int indent_, PrintStream out_) {
		// TODO Auto-generated method stub

	}

	@Override
	public ASTObject getChild(int idx_) {
		switch (idx_) {
		case 0:
			return exp;
		case 1:
			return severity;
		}
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumChildren() {
		return 2;
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
		exp.findReferences(id_, category_, RefType.Read, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		if (severity != null) {
			severity.findReferences(id_, category_, RefType.Read, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		}
	}

	@Override
	public void generateIG(IGSequenceOfStatements aSeq, IGContainer aContainer, IGElaborationEnv aEE) throws ZamiaException {
		IGOperation msg = exp.computeIGOperation(aContainer.findStringType(), aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);
		IGOperation sev = severity != null ? severity.computeIGOperation(aContainer.findSeverityLevelType(), aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null) : null;
		aSeq.add(new IGSequentialReport(msg, sev, getLabel(), getLocation(), aEE.getZDB()));
	}
}
