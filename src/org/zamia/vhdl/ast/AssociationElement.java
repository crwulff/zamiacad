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
public class AssociationElement extends VHDLNode {

	private FormalPart fp;
	private Operation ap;
	
	public AssociationElement (Name n_, Operation op_, VHDLNode parent_, long location_) {
		super(parent_, location_);
		if (n_ != null)
			setFormalPart(new FormalPart (n_, this, location_));
		else
			fp = null;
		setActualPart(op_);
	}
	
	public AssociationElement (VHDLNode parent_, long location_) {
		super(parent_, location_);
	}

	public void setFormalPart (FormalPart fp_) {
		fp = fp_;
		fp.setParent(this);
	}
	
	public void setActualPart (Operation ap_) {
		ap = ap_;
		if (ap != null)
			ap.setParent(this);
	}
	
	public Operation getActualPart() {
		return ap;
	}
	public FormalPart getFormalPart () {
		return fp;
	}

	@Override
	public int getNumChildren() {
		return 2;
	}

	@Override
	public VHDLNode getChild(int idx_) {
		if (idx_ == 0)
			return ap;
		return fp;
	}
	
	@Override 
	public String toString() {
		if (fp != null) {
			return fp +" => " + ap;
		}
		return ap.toString();
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache, ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
		if (ap != null)
			ap.findReferences(id_, category_, refType_, depth_+1, zprj_, aContainer, aCache, result_, aTODO);
	}
}
