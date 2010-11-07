/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jun 19, 2005
 */

package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGSubProgram;


/**
 * 
 * @author Guenter Bartsch
 */

@SuppressWarnings("serial")
public class NameExtensionFunctionCall extends NameExtension {

	private AssociationList al;

	public NameExtensionFunctionCall(AssociationList al_, VHDLNode parent_, long location_) throws ZamiaException {
		super(parent_, location_);
		al = al_;
		al.setParent(this);
	}

	@Override
	public int getNumChildren() {
		return al.getNumAssociations();
	}

	@Override
	public VHDLNode getChild(int idx_) {
		return al.getAssociation(idx_);
	}

	@Override
	public String toVHDL() {
		return al.toString();
	}

	public int getNumParams() {
		return al.getNumAssociations();
	}

	public AssociationElement getParam(int idx_) {
		return al.getAssociation(idx_);
	}

	@Override
	public String toString() {
		return al.toString();
	}

	public AssociationList getAL() {
		return al;
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
		al.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
	}

	@Override
	public void computeIG(IGItem aItem, SourceLocation aPrevLocation, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ArrayList<IGItem> aResult, ErrorReport aReport) throws ZamiaException {

		if (!(aItem instanceof IGSubProgram)) {

			aReport.append("Subprogram expected here.", getLocation());

			return;
		}

		IGSubProgram sp = (IGSubProgram) aItem;

		aResult.add(sp.generateInvocation(al, aContainer, aEE, aCache, getLocation(), aReport));
	}

}
