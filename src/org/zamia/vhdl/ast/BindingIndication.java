/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jul 17, 2009
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
public class BindingIndication extends VHDLNode {

	private AssociationList fGMA;

	private AssociationList fPMA;

	private EntityAspect fEA;

	public BindingIndication(long aLocation) {
		super(aLocation);
	}

	@Override
	public void findReferences(String aId, ObjectCat aCategory, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aResult, ArrayList<SearchJob> aTODO) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Sorry, not implemented yet.");
	}

	public void setEntityAspect(EntityAspect aEA) {
		fEA = aEA;
		if (fEA != null) {
			fEA.setParent(this);
		}
	}

	public void setGenericMapAspect(AssociationList aGMA) {
		fGMA = aGMA;
		if (fGMA != null) {
			fGMA.setParent(this);
		}
	}

	public AssociationList getGenericMapAspect() {
		return fGMA;
	}
	
	public void setPortMapAspect(AssociationList aPMA) {
		fPMA = aPMA;
		if (fPMA != null) {
			fPMA.setParent(this);
		}
	}
	
	public AssociationList getPortMapAspect() {
		return fPMA;
	}
	
	public EntityAspect getEntityAspect() {
		return fEA;
	}

	@Override
	public VHDLNode getChild(int aIdx) {
		switch (aIdx) {
		case 0:
			return fEA;
		case 1:
			return fGMA;
		case 2:
			return fPMA;
		}
		return null;
	}

	@Override
	public int getNumChildren() {
		return 2;
	}

	@Override 
	public String toString() {
		StringBuilder buf = new StringBuilder();
		
		if (fEA != null) {
			buf.append("USE "+fEA+" ");
		}
		
		if (fGMA != null) {
			buf.append("GENERIC MAP"+fGMA+" ");
		}
		
		if (fPMA!= null) {
			buf.append("PORT MAP "+fPMA+" ");
		}
		
		return buf.toString();
	}

}
