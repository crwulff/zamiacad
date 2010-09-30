/* 
 * Copyright 2005-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */

package org.zamia.vhdl.ast;

import java.io.PrintStream;
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
 * Represents an aggregate expression.
 * 
 * 
 * @author Guenter Bartsch
 */

@SuppressWarnings("serial")
public class OperationAggregate extends Operation {

	private Aggregate fAggregate;

	public OperationAggregate(Aggregate aAggregate, ASTObject aParent, long aLocation) {
		super(aParent, aLocation);
		fAggregate = aAggregate;
		fAggregate.setParent(this);
	}

	public OperationAggregate(ASTObject aParent, long aLocation) {
		this(new Aggregate(aParent, aLocation), aParent, aLocation);
	}

	public OperationAggregate(Operation aOp, ASTObject aParent, long aLocation) throws ZamiaException {
		this(aParent, aLocation);
		add(new ElementAssociation(aOp, aParent, aLocation));
	}

	public void add(ElementAssociation aEA) throws ZamiaException {
		fAggregate.add(aEA);
		aEA.setParent(this);
	}

	public String toString() {
		// return "OperationAggregate("+aggregate+")"+"@" +
		// Integer.toHexString(hashCode());
		return "OperationAggregate(" + fAggregate.toVHDL() + ")" + "@" + Integer.toHexString(hashCode());
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	@Override
	public ASTObject getChild(int aIdx) {
		return fAggregate;
	}

	@Override
	public void dump(PrintStream aOut, int aIndent) {
		printSpaces(aOut, aIndent);
		aOut.println("OperationAggregate cnt=" + cnt);
		fAggregate.dump(aOut, aIndent + 2);
	}

	@Override
	public String toVHDL() {
		return fAggregate.toVHDL();
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aRSR, ArrayList<SearchJob> aTODO) throws ZamiaException {
		fAggregate.findReferences(aId, aCat, aRefType, aDepth + 1, aZPrj, aContainer, aCache, aRSR, aTODO);
	}

	@Override
	protected ArrayList<IGOperation> computeIGP(IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		if (!fAggregate.isAggregate()) {

			ElementAssociation ea = fAggregate.getElement(0);
			return ea.getExpression().computeIG(aTypeHint, aContainer, aEE, aCache, aErrorMode, aReport);

		} else {
			ArrayList<IGOperation> res = new ArrayList<IGOperation>(1);
			IGOperation op = fAggregate.computeIG(aTypeHint, aContainer, aEE, aCache, aErrorMode, aReport);
			if (op != null) {
				res.add(op);
			}
			return res;
		}
	}

}