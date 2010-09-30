/*
 * Copyright 2008 by the authors indicated in the @author tags.
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
import org.zamia.rtl.RTLPort.PortDir;


/**
 * 
 * @author Guenter Bartsch
 *
 */
@SuppressWarnings("serial")
public class FileOpenInformation extends ASTObject {

	private Operation stringExpr; // other people would probably call this "fileName"...
	private PortDir m;

	public FileOpenInformation (Operation exp_, PortDir m_, Operation stringExpr_, ASTObject parent_, long location_) {
		super(parent_, location_);
		stringExpr = stringExpr_;
		stringExpr.setParent(this);
		m = m_;
	}
	
	public Operation getStringExpr() {
		return stringExpr;
	}
	
	public PortDir getMode() {
		return m;
	}
	
	@Override
	public ASTObject getChild(int idx_) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumChildren() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache, ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
	}

}
