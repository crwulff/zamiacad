/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 25, 2009
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
public class OperationName extends Operation {

	private Name fName;

	public OperationName(Name aName, ASTObject aParent, long aLocation) {
		super(aParent, aLocation);

		fName = aName;
		fName.setParent(this);
	}

	@Override
	protected ArrayList<IGOperation> computeIGP(IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
		ArrayList<IGOperation> res = new ArrayList<IGOperation>(1);

		IGOperation op = fName.computeIGAsOperation(aTypeHint, aContainer, aEE, aCache, aErrorMode, aReport);

		if (op == null) {
			return res;
		}

		res.add(op);
		return res;
	}

	@Override
	public String toVHDL() {
		return fName.toVHDL();
	}

	@Override
	public String toString() {
		return fName.toString();
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
		fName.findReferences(id_, category_, refType_, depth_, zprj_, aContainer, aCache, result_, aTODO);
	}

	@Override
	public ASTObject getChild(int aIdx) {
		return fName;
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	public Name getName() {
		return fName;
	}

}
