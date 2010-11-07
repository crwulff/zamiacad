/* 
 * Copyright 2008 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Aug 9, 2008
 */
package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite;
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
public class RecordElementDeclaration extends VHDLNode {

	private String id;

	private TypeDefinition td;

	public RecordElementDeclaration(String id_, TypeDefinition t_, VHDLNode parent_, long location_) {
		super(parent_, location_);
		id = id_;
		td = t_;
	}

	@Override
	public VHDLNode getChild(int idx_) {
		return td;
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache, ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) {
		if (id.equals(id_)) {
			result_.add(new ReferenceSite(this, RefType.Declaration));
		}
	}

	public String getId() {
		return id;
	}
	
	public TypeDefinition getTypeDefinition() {
		return td;
	}
}
