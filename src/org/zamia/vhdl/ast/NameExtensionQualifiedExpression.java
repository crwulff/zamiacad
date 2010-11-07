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
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGOperationTypeQualification;
import org.zamia.instgraph.IGType;


/**
 * for high-speed parsing qualified expressions are represented as name
 * extensions in the tree
 * 
 * @author Guenter Bartsch
 */

@SuppressWarnings("serial")
public class NameExtensionQualifiedExpression extends NameExtension {

	private Operation fExp;

	public NameExtensionQualifiedExpression(Aggregate aAggregate, VHDLNode aParent, long aLocation) {
		super(aParent, aLocation);
		fExp = new OperationAggregate(aAggregate, this, aLocation);
	}

	public NameExtensionQualifiedExpression(Operation aExpr, VHDLNode aParent, long aLocation) {
		super(aParent, aLocation);
		fExp = aExpr;
		fExp.setParent(this);
	}

	@Override
	public int getNumChildren() {
		return 0;
	}

	@Override
	public VHDLNode getChild(int aIdx) {
		return null;
	}

	@Override
	public String toVHDL() {
		return "'(" + fExp + ")";
	}

	@Override
	public void computeIG(IGItem aItem, SourceLocation aPrevLocation, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ArrayList<IGItem> aResult, ErrorReport aReport) throws ZamiaException {

		if (!(aItem instanceof IGType)) {
			aReport.append("Type expected here.", getLocation());
			return;
		}

		IGType type = (IGType) aItem;

		IGOperation obj = fExp.computeIGOperation(type, aContainer, aEE, aCache, ASTErrorMode.RETURN_NULL, aReport);
		if (obj == null) {
			aReport.append("Failed to compute expression ", getLocation());
			return;
		}

		aResult.add(new IGOperationTypeQualification(obj, type, getLocation(), aEE.getZDB()));
	}

	@Override
	public String toString() {
		return toVHDL();
	}

	public Operation getExpression() {
		return fExp;
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache, ReferenceSearchResult aResult,
			ArrayList<SearchJob> aTODO) throws ZamiaException {
		fExp.findReferences(aId, aCat, aRefType, aDepth + 1, aZPrj, aContainer, aCache, aResult, aTODO);
	}

}
