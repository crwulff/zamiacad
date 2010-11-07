/* 
 * Copyright 2007-2009 by the authors indicated in the @author tags. 
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
import org.zamia.instgraph.IGOperationInvokeSubprogram;
import org.zamia.instgraph.IGType;


/**
 * Represents a VHDL concatenation operation, e.g.
 * 
 * a & b 
 * b & '0'
 * 
 * @author Guenter Bartsch
 */

@SuppressWarnings("serial")
public class OperationConcat extends Operation {

	private Operation fA, fB;

	public OperationConcat(Operation aA, Operation aB, VHDLNode aParent, long aLocation) {
		super(aParent, aLocation);

		if (aB == null) {
			System.out.println("Error: OperationConcat with b==null");
		}

		fA = aA;
		fB = aB;

		fA.setParent(this);
		if (fB != null)
			fB.setParent(this);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("OperationConcat(");
		buf.append(fA + ", ");
		buf.append(fB + ")");
		return buf.toString();
	}

	@Override
	public void dump(PrintStream aOut, int aIndent) {
		printSpaces(aOut, aIndent);
		aOut.print("OperationConcat, cnt=" + getCnt());
		fA.dump(aOut, aIndent + 2);
		if (fB != null)
			fB.dump(aOut, aIndent + 2);
		else {
			printSpaces(aOut, aIndent + 2);
			aOut.println("null");
		}
	}

	public Operation getOperandA() {
		return fA;
	}

	public Operation getOperandB() {
		return fB;
	}

	@Override
	public int getNumChildren() {
		return 2;
	}

	@Override
	public VHDLNode getChild(int aIdx) {
		switch (aIdx) {
		case 0:
			return fA;
		case 1:
			return fB;
		}
		return null;
	}

	@Override
	public String toVHDL() {

		StringBuilder buf = new StringBuilder();

		buf.append(fA.toVHDL());

		buf.append(" & ");
		buf.append(fB.toVHDL());
		return buf.toString();
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aResult, ArrayList<SearchJob> aTODO) throws ZamiaException {
		fA.findReferences(aId, aCat, aRefType, aDepth + 1, aZPrj, aContainer, aCache, aResult, aTODO);
		fB.findReferences(aId, aCat, aRefType, aDepth + 1, aZPrj, aContainer, aCache, aResult, aTODO);
	}

	@Override
	protected ArrayList<IGOperation> computeIGP(IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		ArrayList<IGOperation> res = new ArrayList<IGOperation>();

		String opId = "\"&\"";

		IGOperationInvokeSubprogram inv = generateOperatorInvocation(opId, fA, fB, aTypeHint, aContainer, aEE, aCache, aErrorMode, aReport);
		if (inv != null) {
			res.add(inv);
		}

		return res;
	}
	

}