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
public class EntityAspect extends VHDLNode {

	public enum EntityAspectKind {
		Entity, Configuration, Open
	};

	private EntityAspectKind fEAK;

	private Name fName;

	private String fArchId;

	public EntityAspect(EntityAspectKind aEAK, Name aName, long aLocation) {
		super(aLocation);
		fEAK = aEAK;
		fName = aName;
		if (fName != null) {
			fName.setParent(this);
		}
	}

	public void setId(String aArchId) {
		fArchId = aArchId;
	}

	@Override
	public void findReferences(String aId, ObjectCat aCategory, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aResult, ArrayList<SearchJob> aTODO) throws ZamiaException {
		//FIXME: implement
		throw new ZamiaException("Sorry, not implemented.", this);
	}

	public EntityAspectKind getKind() {
		return fEAK;
	}
	
	public String getArchId() {
		return fArchId;
	}
	
	public Name getName() {
		return fName;
	}
	
	
	@Override
	public VHDLNode getChild(int aIdx) {
		return fName;
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	@Override
	public String toString() {
		switch (fEAK) {
		case Configuration:
			return "CONFIGURATION " + fName;
		case Open:
			return "OPEN";
		case Entity:
			return "ENTITY " + fName + "(" + fArchId + ")";
		}
		return "ENTITY ASPECT ? ";
	}
}
