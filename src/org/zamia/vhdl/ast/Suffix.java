/*
 * Copyright 2007,2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;



/**
 * A suffix, used in Names.
 * 
 * @author Guenter Bartsch
 *
 */
@SuppressWarnings("serial")
public class Suffix extends VHDLNode {
	
	public enum SuffixType {ID, STRING_LITERAL, CHAR_LITERAL, ALL}
	
	private String id;
	private SuffixType type;
	
	
	public Suffix (long location_) {
		super (location_);
	}
	
	public Suffix (String id_, long location_) {
		this (location_);
		setId (id_);
	}
	
	public SuffixType getType() {
		return type;
	}
	
	public void setId (String id_) {
		id = id_;
		type = SuffixType.ID;
	}

	public void setCharLiteral (char ch_) {
		type = SuffixType.CHAR_LITERAL;
	}

	public void setStringLiteral (String id_) {
		id = id_;
		type = SuffixType.STRING_LITERAL;
	}
	
	public void setAll ()  {
		type = SuffixType.ALL;
	}
	
	public String getId() {
		if (type == SuffixType.ALL)
			return "ALL";
		return id;
	}
	
	public String toString () {
		return getId();
	}

	@Override
	public VHDLNode getChild(int idx_) {
		return null;
	}

	@Override
	public int getNumChildren() {
		return 0;
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache, ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) {
	}
}
