/*
 * Copyright 2005-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jun 27, 2005
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
public class Signature extends VHDLNode {
	
	private ArrayList<Name> names = new ArrayList<Name>(1);
	
	public Signature (VHDLNode parent_, long location_) {
		super (parent_, location_);
	}
	
	public void add (Name n_) {
		names.add (n_);
	}
	
	public void setReturn (Name n_) {
	}

	@Override
	public int getNumChildren() {
		return names.size();
	}

	@Override
	public VHDLNode getChild(int idx_) {
		return names.get(idx_);
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache, ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
	}
}
