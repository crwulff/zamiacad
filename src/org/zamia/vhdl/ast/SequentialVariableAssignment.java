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
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSequentialAssignment;
import org.zamia.instgraph.IGType;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class SequentialVariableAssignment extends SequentialStatement {

	public static final boolean dump = false;

	private Target target;

	private Operation value;

	public SequentialVariableAssignment(Target target_, Operation value_, VHDLNode parent_, long location_) {
		super(parent_, location_);
		target = target_;
		target.setParent(this);
		value = value_;
		value.setParent(this);
	}

	public Target getTarget() {
		return target;
	}

	public Operation getValue() {
		return value;
	}

	public String toString() {
		return "SequentialVariableAssignment " + target + " := " + value;
	}

	@Override
	public int getNumChildren() {
		return 2;
	}

	@Override
	public VHDLNode getChild(int idx_) {
		switch (idx_) {
		case 0:
			return target;
		case 1:
			return value;
		}
		return null;
	}

	@Override
	public void dumpVHDL(int indent_, PrintStream out_) {
		printlnIndented(target.toVHDL() + " := " + value.toVHDL() + ";", indent_, out_);
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {

		if (category_ == ObjectCat.Variable) {
			target.findReferences(id_, category_, RefType.Write, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		}

		value.findReferences(id_, category_, RefType.Read, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);

	}

	@Override
	public void generateIG(IGSequenceOfStatements aSeq, IGContainer aContainer, IGElaborationEnv aEE) throws ZamiaException {

		IGType typeHint = target.computeType(null, aContainer, aEE, ASTErrorMode.EXCEPTION, null);

		IGOperation igValue = value.computeIGOperation(typeHint, aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);

		IGOperation igTarget = target.computeIG(typeHint, aContainer, aEE, ASTErrorMode.EXCEPTION, null);

		IGSequentialAssignment assignment = new IGSequentialAssignment(igValue, igTarget, true, null, getLabel(), getLocation(), aEE.getZDB());

		aSeq.add(assignment);
	}

}