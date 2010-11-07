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
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSequentialLoop;
import org.zamia.instgraph.IGSequentialLoop.SeqLoopType;


/**
 * This represents an endless loop. Use SequentialFor and SequentialWhile to
 * represent for/while loops.
 * 
 * @author guenter bartsch
 * 
 */

@SuppressWarnings("serial")
public class SequentialLoop extends SequentialStatement {

	protected SequenceOfStatements body;

	public SequentialLoop(String label_, VHDLNode parent_, long location_) {
		super(label_, parent_, location_);
	}

	public void setBody(SequenceOfStatements body_) {
		body = body_;
		body.setParent(this);
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	@Override
	public VHDLNode getChild(int idx_) {
		return body;
	}

	@Override
	public void dumpVHDL(int indent_, PrintStream out_) {
		// FIXME: implement
		printlnIndented("--error: don't know how to dump " + this, indent_, out_);
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
		body.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
	}

	@Override
	public void generateIG(IGSequenceOfStatements aSeq, IGContainer aContainer, IGElaborationEnv aCache) throws ZamiaException {

		IGSequentialLoop zilLoop = new IGSequentialLoop(SeqLoopType.INFINITE, aContainer.getDBID(), getLabel(), getLocation(), aCache.getZDB());

		IGSequenceOfStatements zilBody = new IGSequenceOfStatements(null, getLocation(), aCache.getZDB());
		body.generateIG(zilBody, zilLoop.getContainer(), aCache);

		zilLoop.setBody(zilBody);

		aSeq.add(zilLoop);
	}

}
