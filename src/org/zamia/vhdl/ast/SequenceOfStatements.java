/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jan 8, 2005
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
import org.zamia.instgraph.IGSequenceOfStatements;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class SequenceOfStatements extends SequentialStatement {

	private ArrayList<SequentialStatement> statements; // of SequentialStatement
	
	public SequenceOfStatements (VHDLNode parent_, long location_) {
		super (parent_, location_);
		statements = new ArrayList<SequentialStatement>();
	}
	
	public void add (SequentialStatement stmt_) {
		statements.add (stmt_);
		stmt_.setParent(this);
	}

	public void append (SequenceOfStatements seq_) {
		int n = seq_.getNumStatements();
		for (int i = 0; i<n; i++) {
			SequentialStatement stmt = seq_.getStatement(i);
			add (stmt);
			stmt.setParent(this, true);
		}
	}
	
	public int getNumStatements () {
		return statements.size();
	}
	public SequentialStatement getStatement (int idx_) {
		return (SequentialStatement) statements.get(idx_);
	}
	

	@Override
	public int getNumChildren() {
		return statements.size();
	}

	@Override
	public VHDLNode getChild(int idx_) {
		return statements.get(idx_);
	}

	@Override
	public void dumpVHDL(int indent_, PrintStream out_) {
		int n = getNumStatements();
		for (int i = 0; i<n; i++) {
			SequentialStatement stmt = getStatement(i);
			stmt.dumpVHDL(indent_+2, out_);
		}
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache, ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
		int n = getNumStatements();
		for (int i = 0; i<n; i++) {
			SequentialStatement stmt = getStatement(i);
			stmt.findReferences(id_, category_, refType_, depth_+1, zprj_, aContainer, aCache, result_, aTODO);
		}
	}

	@Override
	public void generateIG(IGSequenceOfStatements aSeq, IGContainer aContainer, IGElaborationEnv aCache) throws ZamiaException {
		int n = getNumStatements();
		for (int i = 0; i<n; i++) {
			SequentialStatement statement = getStatement(i);
			
			statement.generateIG(aSeq, aContainer, aCache);
		}
	}

}
