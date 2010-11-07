/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 1, 2010
 */
package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;

/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class UnitInfo extends VHDLNode {

	private final String fId;

	private final OperationLiteral fLiteral;

	public UnitInfo(String aId, OperationLiteral aLiteral, VHDLNode aParent, long aLocation) {
		super(aParent, aLocation);
		fId = aId;
		fLiteral = aLiteral;
	}

	public String getId() {
		return fId;
	}

	public OperationLiteral getLiteral() {
		return fLiteral;
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	@Override
	public VHDLNode getChild(int aIdx) {
		return fLiteral;
	}

	@Override
	public void findReferences(String aId, ObjectCat aCategory, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aResult, ArrayList<SearchJob> aTODO) throws ZamiaException {
	}

}
