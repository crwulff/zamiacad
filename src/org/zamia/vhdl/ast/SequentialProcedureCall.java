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
import org.zamia.instgraph.IGOperationInvokeSubprogram;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSequentialProcedureCall;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class SequentialProcedureCall extends SequentialStatement {

	private Name name;

	public SequentialProcedureCall(Name name_, ASTObject parent_, long location_) {
		super(parent_, location_);
		name = name_;
		name.setParent(this);
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	@Override
	public ASTObject getChild(int idx_) {
		return name;
	}

	@Override
	public void dumpVHDL(int indent_, PrintStream out_) {
		// FIXME: implement
		printlnIndented("--error: don't know how to dump " + this, indent_, out_);
	}

	@Override
	public String toString() {
		return name.toString();
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
		name.findReferences(id_, category_, RefType.Read, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
	}

	@Override
	public void generateIG(IGSequenceOfStatements aSeq, IGContainer aContainer, IGElaborationEnv aCache) throws ZamiaException {

		IGOperationInvokeSubprogram inv = name.computeIGAsProcedure(aContainer, aCache, ASTErrorMode.EXCEPTION, null);

		if (inv != null) {
			aSeq.add(new IGSequentialProcedureCall(inv, getLabel(), getLocation(), aCache.getZDB()));
		}
	}

}