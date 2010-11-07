/*
 * Copyright 2007-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
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
public class LibraryClause extends VHDLNode {
	private String id;

	public LibraryClause(String id_, VHDLNode parent_, long location_) {
		super (parent_, location_);
		id = id_;
	}

	@Override
	public VHDLNode getChild(int idx_) {
		return null;
	}

	@Override
	public int getNumChildren() {
		return 0;
	}
	
	public String getId() {
		return id;
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache, ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
	}

	@Override
	public String toString() {
		return "library "+id+";";
	}

}