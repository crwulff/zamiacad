/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jan 9, 2005
 */

package org.zamia.vhdl.ast;

import java.io.PrintStream;
import java.util.ArrayList;

import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGContainerItem;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGObject.IGObjectCat;
import org.zamia.instgraph.IGObject.OIDir;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class VariableDeclaration extends BlockDeclarativeItem {

	private TypeDefinition fTD;

	private Operation fInitialValue;

	public VariableDeclaration(String aId, TypeDefinition aTD, Operation aInitialValue, VHDLNode aParent, long aLocation) {
		super(aId, aParent, aLocation);
		fTD = aTD;
		fTD.setParent(this);
		fInitialValue = aInitialValue;
		if (fInitialValue != null) {
			fInitialValue.setParent(this);
		}
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	@Override
	public VHDLNode getChild(int aIdx) {
		return fTD;
	}

	@Override
	public void dump(PrintStream aOut) {
		// TODO Auto-generated method stub
	}

	@Override
	public String toString() {
		return "VARIABLE " + id + " : " + fTD + " := " + fInitialValue;
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aEE,
			ReferenceSearchResult aResult, ArrayList<SearchJob> aTODO) {
		if (aId.equals(getId())) {
			aResult.add(new ReferenceSite(this, RefType.Declaration));
		}
	}

	@Override
	public IGContainerItem computeIG(ArrayList<IGContainerItem> aSpecItems, IGContainer aContainer, IGElaborationEnv aEE) throws ZamiaException {

		IGType type = fTD.computeIG(aContainer, aEE);

		IGOperation iv = fInitialValue != null ? fInitialValue.computeIGOperation(type, aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null) : null;

		IGObject v = new IGObject(OIDir.NONE, iv, IGObjectCat.VARIABLE, type, getId(), getLocation(), aEE.getZDB());

		aContainer.add(v);

		return v;
	}

}
