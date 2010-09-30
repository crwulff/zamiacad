/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
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
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class OperationCompare extends Operation {

	public enum CompareOp {
		EQUAL, GREATER, NEQUAL, LESS, LESSEQ, GREATEREQ
	}

	private CompareOp fOp;

	private Operation fA, fB;

	public OperationCompare(CompareOp aOp, Operation aA, Operation aB, ASTObject aParent, long aLocation) {
		super(aParent, aLocation);
		fOp = aOp;
		fA = aA;
		fA.setParent(this);
		fB = aB;
		fB.setParent(this);
	}

	public Operation getOperandA() {
		return fA;
	}

	public Operation getOperandB() {
		return fB;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("OperationCompare(");
		buf.append(fA);
		switch (fOp) {
		case EQUAL:
			buf.append(" = ");
			break;
		case GREATER:
			buf.append(" > ");
			break;
		case GREATEREQ:
			buf.append(" >= ");
			break;
		case LESS:
			buf.append(" < ");
			break;
		case LESSEQ:
			buf.append(" <= ");
			break;
		case NEQUAL:
			buf.append(" != ");
			break;
		default:
			buf.append(" ??? ");
		}
		buf.append(fB + ")");
		return buf.toString();
	}

	@Override
	public int getNumChildren() {
		return 2;
	}

	@Override
	public ASTObject getChild(int idx_) {
		switch (idx_) {
		case 0:
			return fA;
		case 1:
			return fB;
		}
		return null;
	}

	@Override
	public void dump(PrintStream aOut, int aIndent) {
		printSpaces(aOut, aIndent);
		aOut.print("OperationCompare, cnt=" + cnt + ", op=" + getCompareOpId());
		fA.dump(aOut, aIndent + 2);
		fB.dump(aOut, aIndent + 2);
	}

	public CompareOp getOp() {
		return fOp;
	}

	@Override
	public String toVHDL() {

		StringBuilder buf = new StringBuilder();

		buf.append(fA.toVHDL());

		buf.append(getCompareOpId());
		buf.append(fB.toVHDL());
		return buf.toString();
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
		fA.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		fB.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
	}

	private String getCompareOpId() {
		switch (fOp) {
		case EQUAL:
			return "\"=\"";
		case GREATER:
			return "\">\"";
		case GREATEREQ:
			return "\">=\"";
		case LESS:
			return "\"<\"";
		case LESSEQ:
			return "\"<=\"";
		case NEQUAL:
			return "\"/=\"";
		}
		return "";
	}

	@Override
	protected ArrayList<IGOperation> computeIGP(IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		ArrayList<IGOperation> res = new ArrayList<IGOperation>();
		
		String opId = getCompareOpId();

		IGOperationInvokeSubprogram inv = generateOperatorInvocation(opId, fA, fB, aTypeHint, aContainer, aEE, aCache, aErrorMode, aReport);
		if (inv != null) {
			res.add(inv);
		}

		return res;
	}

}