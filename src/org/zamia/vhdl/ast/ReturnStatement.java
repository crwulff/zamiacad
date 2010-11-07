/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Aug 2, 2005
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
import org.zamia.instgraph.IGSequentialReturn;
import org.zamia.instgraph.IGType;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class ReturnStatement extends SequentialStatement {

	private Operation exp;

	public ReturnStatement(Operation exp_, VHDLNode parent_, long location_) {
		super(parent_, location_);
		exp = exp_;
		if (exp != null)
			exp.setParent(this);
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	@Override
	public VHDLNode getChild(int idx_) {
		return exp;
	}

	@Override
	public void dumpVHDL(int indent_, PrintStream out_) {
		printlnIndented("RETURN " + exp, indent_, out_);
	}

	@Override
	public String toString() {
		return "RETURN " + exp;
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {

		exp.findReferences(id_, category_, RefType.Read, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);

	}

	@Override
	public void generateIG(IGSequenceOfStatements aSeq, IGContainer aContainer, IGElaborationEnv aEE) throws ZamiaException {
		IGOperation igExp = null;
		if (exp != null) {
			IGType type = aContainer.getReturnType();
			if (type == null) {
				throw new ZamiaException("Subprogram has no return type.", getLocation());
			}

			igExp = exp.computeIGOperation(type, aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);
		}

		aSeq.add(new IGSequentialReturn(igExp, getLabel(), getLocation(), aEE.getZDB()));
	}
}
