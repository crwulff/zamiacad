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
public class ComponentSpecification extends VHDLNode {

	private ArrayList<Identifier> fIL;
	private Name fName;

	public ComponentSpecification(ArrayList<Identifier> aIL, Name aName, long aLocation) {
		super(aLocation);
		fIL = aIL;
		if (fIL != null) {
			int n = fIL.size();
			for (int i = 0; i < n; i++) {
				fIL.get(i).setParent(this);
			}
		}
		fName = aName;
		if (fName != null) {
			fName.setParent(this);
		}
	}

	public Name getName() {
		return fName;
	}
	
	@Override
	public void findReferences(String aId, ObjectCat aCategory, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aResult, ArrayList<SearchJob> aTODO) throws ZamiaException {
		// FIXME ?
	}

	@Override
	public VHDLNode getChild(int aIdx) {
		return fIL.get(aIdx);
	}

	@Override
	public int getNumChildren() {
		return fIL.size();
	}
	
	public int getNumIds() {
		return fIL.size();
	}
	
	public Identifier getId(int aIdx) {
		return fIL.get(aIdx);
	}

}
