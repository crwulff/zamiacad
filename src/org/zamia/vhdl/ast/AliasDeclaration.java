/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jun 4, 2008
 */
package org.zamia.vhdl.ast;

import java.io.PrintStream;
import java.util.ArrayList;

import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGContainerItem;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationAlias;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGResolveResult;
import org.zamia.instgraph.IGType;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class AliasDeclaration extends BlockDeclarativeItem {

	private TypeDefinition type;

	private Name name;

	private Signature signature;

	public AliasDeclaration(String id_, TypeDefinition td_, Name n_, Signature s_, long location_) {
		super(id_, null, location_);

		setType(td_);
		name = n_;
		name.setParent(this);
		setSignature(s_);
	}

	private void setType(TypeDefinition td_) {
		if (td_ != null) {
			type = td_;
			type.setParent(this);
		}
	}

	private void setSignature(Signature s_) {
		if (s_ != null) {
			signature = s_;
			signature.setParent(this);
		}
	}

	@Override
	public VHDLNode getChild(int idx_) {

		switch (idx_) {
		case 0:
			return type;
		case 1:
			return name;
		case 2:
			return signature;
		}

		return null;
	}

	@Override
	public int getNumChildren() {
		return 3;
	}

	@Override
	public void dump(PrintStream out_) {
		// TODO Auto-generated method stub

	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
		if (id_.equals(getId())) {
			result_.add(new ReferenceSite(this, RefType.Declaration));
		}
	}

	@Override
	public IGContainerItem computeIG(ArrayList<IGContainerItem> aSpecItems, IGContainer aContainer, IGElaborationEnv aEE) throws ZamiaException {

		IGResolveResult result = name.computeIG(null, aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);

		int n = result.getNumResults();
		for (int i = 0; i < n; i++) {
			IGItem item = result.getResult(i);
			if (item instanceof IGOperation) {

				IGOperation op = (IGOperation) item;

				IGType t = null;
				if (type != null) {
					t = type.computeIG(aContainer, aEE);
				}

				IGOperationAlias alias = new IGOperationAlias(op, t, id, getLocation(), aEE.getZDB());
				aContainer.add(alias);
				return alias;
			}
		}
		throw new ZamiaException("Sorry, not implemented yet.");
	}

}
