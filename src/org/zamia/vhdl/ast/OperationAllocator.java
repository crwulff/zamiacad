/*
 * Copyright 2008-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.ErrorReport;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationAllocate;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGOperationTypeQualification;
import org.zamia.instgraph.IGResolveResult;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGType.TypeCat;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class OperationAllocator extends Operation {

	private TypeDefinitionSubType fTD;

	public OperationAllocator(TypeDefinitionSubType aTD, VHDLNode aParent, long aLocation) {
		super(aParent, aLocation);
		fTD = aTD;
		fTD.setParent(this);
	}

	@Override
	public String toVHDL() {
		return "NEW " + fTD;
	}

	@Override
	public VHDLNode getChild(int aIdx) {
		return null;
	}

	@Override
	public int getNumChildren() {
		return 0;
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
		// FIXME: implement: type search
	}

	@Override
	protected ArrayList<IGOperation> computeIGP(IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ASTErrorMode aErrorMode,
			ErrorReport aReport) throws ZamiaException {

		ArrayList<IGOperation> res = new ArrayList<IGOperation>();

		Name name = fTD.getName();
		IGResolveResult result = name.computeIG(aTypeHint, aContainer, aEE, aCache, aErrorMode, aReport);
		if (result.isEmpty()) {
			return res;
		}

		for (IGItem item : result) {

			if (item instanceof IGOperationTypeQualification) {

				IGOperationTypeQualification qual = (IGOperationTypeQualification) item;

				IGType t = qual.getType();
				IGType accessT = null;

				if (aTypeHint != null && aTypeHint.isAccess()) {
					IGType et = aTypeHint.getElementType();
					if (t.isAssignmentCompatible(et, getLocation())) {
						accessT = aTypeHint;
					}
				}

				if (accessT == null) {
					accessT = new IGType(TypeCat.ACCESS, null, null, null, t, null, false, getLocation(), aEE.getZDB());
				}

				IGOperation expr = qual.getOperation();

				res.add(new IGOperationAllocate(expr, accessT, getLocation(), aEE.getZDB()));
			} else {
				IGType t = fTD.computeIG(aContainer, aEE);

				t = new IGType(TypeCat.ACCESS, null, null, null, t, null, false, getLocation(), aEE.getZDB());

				res.add(new IGOperationAllocate(null, t, getLocation(), aEE.getZDB()));
			}
		}
		return res;
	}

	@Override
	public String toString() {
		return toVHDL();
	}
}
