/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
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
 * 
 * @author Guenter Bartsch
 *
 */
@SuppressWarnings("serial")
public class SignalList extends VHDLNode {

	public static final int KIND_SIGNAL_LIST = 0;
	public static final int KIND_OTHERS = 1;
	public static final int KIND_ALL = 2;
	
	//private int kind = KIND_SIGNAL_LIST;

	public SignalList(Name name_, VHDLNode parent_, long location_) {
		super (parent_, location_);
	}

	public SignalList(int kind_, VHDLNode parent_, long location_) {
		super (parent_, location_);
	}

	@Override
	public VHDLNode getChild(int idx_) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumChildren() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void add(Name n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache, ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) {
		
		// FIXME: todo
		
		logger.warn("%s: findReferences not implemented yet.", getClass());
		
	}

}
