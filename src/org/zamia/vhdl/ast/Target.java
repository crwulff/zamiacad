/*
 * Copyright 2004-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on 26.06.2004
 */

package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.ErrorReport;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGType;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class Target extends ASTObject {

	private Name fName;

	private Aggregate fAggregate;

	public Target(Name aName, ASTObject aParent, long aLocation) {
		super(aParent, aLocation);
		setName(aName);
	}

	public Target(Aggregate aAggregate, ASTObject aParent, long aLocation) {
		super(aParent, aLocation);
		setAggregate(aAggregate);
	}

	public Target(ASTObject aParent, long aLocation) {
		super(aParent, aLocation);
	}

	public void setName(Name aName) {
		fName = aName;
		fName.setParent(this);
	}

	public void setAggregate(Aggregate aAggregate) {
		fAggregate = aAggregate;
		if (aAggregate != null) {
			aAggregate.setParent(this);
		}
		fName = null;
	}

	public String toString() {
		StringBuffer str = new StringBuffer("Target [");
		if (fName != null) {
			str.append(fName.toString() + "]");
		} else {
			str.append(fAggregate);
		}

		return str.toString();
	}

	public Name getName() {
		return fName;
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	@Override
	public ASTObject getChild(int aIdx) {
		return fName != null ? fName : fAggregate;
	}

	public String toVHDL() {
		return fName != null ? fName.toVHDL() : fAggregate.toVHDL();
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aRSR, ArrayList<SearchJob> aTODO) throws ZamiaException {
		if (fName != null) {
			fName.findReferences(aId, aCat, aRefType, aDepth + 1, aZPrj, aContainer, aCache, aRSR, aTODO);
		} else {
			fAggregate.findReferences(aId, aCat, aRefType, aDepth + 1, aZPrj, aContainer, aCache, aRSR, aTODO);
		}
	}

	public IGType computeType(IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
		if (fName != null) {
			IGOperation op = fName.computeIGAsOperation(aTypeHint, aContainer, aEE, new IGOperationCache(), aErrorMode, aReport);
			if (op == null) {
				return null;
			}
			return op.getType();
		} else {
			IGOperation op = fAggregate.computeIG(aTypeHint, aContainer, aEE, new IGOperationCache(), aErrorMode, aReport);
			if (op == null) {
				return null;
			}
			return op.getType();
		}
	}

	public IGOperation computeIG(IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
		if (fName != null) {
			return fName.computeIGAsOperation(aTypeHint, aContainer, aEE, new IGOperationCache(), aErrorMode, aReport);
		} else {
			return fAggregate.computeIG(aTypeHint, aContainer, aEE, new IGOperationCache(), aErrorMode, aReport);
		}
	}

}