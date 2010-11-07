/*
 * Copyright 2005-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jul 23, 2005
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
public class FormalPart extends VHDLNode {
	
	private Name n, n2;
	
	public FormalPart (Name n_, VHDLNode parent_, long location_) {
		super(parent_, location_);
		setName(n_);
	}
	
	public void setName(Name n_) {
		n = n_;
		n.setParent(this);
	}
	public void setName2 (Name n_) {
		n2 = n_;
		n2.setParent(this);
	}
	
	public Name getName() {
		return n;
	}

	@Override
	public int getNumChildren() {
		return 2;
	}

	@Override
	public VHDLNode getChild(int idx_) {
		if (idx_ == 0)
			return n;
		return n2;
	}

	@Override
	public String toString() {
		if (n2 != null)
			return n.toString() + " "+n2;
		return n.toString();
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache, ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
	}
}
