/*
 * Copyright 2005-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Feb 5, 2005
 */

package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class ElementAssociation extends VHDLNode {

	private ArrayList<Range> choices;
	private Operation expression;

	public ElementAssociation(VHDLNode parent_, long location_) {
		this(null, parent_, location_);
	}
	public ElementAssociation(Operation expr_, VHDLNode parent_, long location_) {
		this(null, expr_, parent_, location_);
	}
	public ElementAssociation(ArrayList<Range> choices_, Operation expr_, VHDLNode parent_, long location_) {
		super(parent_, location_);
		setExpression(expr_);
		setChoices(choices_);
	}

	private void setChoices(ArrayList<Range> choices2) {
		choices = choices2;
		if (choices != null) {
			int n = choices.size();
			for (int i = 0; i<n; i++) {
				Range choice = choices.get(i);
				if (choice != null)
					choice.setParent(this);
			}
		}
	}
	public void setExpression(Operation op_) {
		expression = op_;
		if (op_ != null) {
			expression.setParent(this);
		}
	}

	public Operation getExpression() {
		return expression;
	}
	public ArrayList<Range> getChoices() {
		return choices;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("ElementAssociation(");

		if (choices != null) {
			int n = choices.size();
			for (int i = 0; i < n; i++) {
				buf.append(choices.get(i));
				if (i < (n - 1))
					buf.append(", ");
			}
			buf.append("=>");
		}
		buf.append(expression.toString());

		return buf.toString() + ")@" + Integer.toHexString(hashCode());
	}
	@Override
	public int getNumChildren() {
		if (choices == null)
			return 1;
		return choices.size()+1;
	}
	@Override
	public VHDLNode getChild(int idx_) {
		if (idx_ == 0)
			return expression;
		idx_--;
		return choices.get(idx_);
	}
	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache, ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
		expression.findReferences(id_, category_, refType_, depth_+1, zprj_, aContainer, aCache, result_, aTODO);
		if (choices != null) {
			int n = choices.size();
			for (int i = 0 ; i<n; i++) {
				Range choice = choices.get(i);
				choice.findReferences(id_, category_, refType_, depth_+1, zprj_, aContainer, aCache, result_, aTODO);
			}
		}
	}
	public boolean isImplicit() {
		return choices==null;
	}
}
