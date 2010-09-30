/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
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
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSequentialAssert;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class SequentialAssert extends SequentialStatement {

	private Assertion assertion;
	
	public SequentialAssert (Assertion assertion_, String label_, ASTObject parent_, long location_) {
		super (label_, parent_, location_);
		assertion = assertion_;
		assertion.setParent(this);
	}


	@Override
	public int getNumChildren() {
		return 1;
	}

	@Override
	public ASTObject getChild(int idx_) {
		return assertion;
	}

	public void dumpVHDL(int indent_, PrintStream out_) {
		assertion.dumpVHDL(indent_, out_);
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache, ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
		assertion.findReferences(id_, category_, refType_, depth_+1, zprj_, aContainer, aCache, result_, aTODO);
	}

	@Override
	public void generateIG(IGSequenceOfStatements aSeq, IGContainer aContainer, IGElaborationEnv aEE) throws ZamiaException {
		aSeq.add(new IGSequentialAssert(assertion.computeIG(aContainer, aEE, new IGOperationCache()), getLabel(), getLocation(), aEE.getZDB()));
	}

	@Override
	public String toString() {
		return assertion.toVHDL();
	}
}
