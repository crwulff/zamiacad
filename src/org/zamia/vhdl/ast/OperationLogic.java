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
public class OperationLogic extends Operation {

	public enum LogicOp {
		NOT, AND, OR, NAND, NOR, XNOR, XOR, BUF
	}

	private LogicOp fOp;

	private Operation fA, fB;

	public OperationLogic(LogicOp aOp, Operation aA, Operation aB, VHDLNode aParent, long aLocation) {
		super(aParent, aLocation);
		fOp = aOp;

		fA = aA;
		fB = aB;

		fA.setParent(this);
		if (fB != null) {
			fB.setParent(this);
		}
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("OperationLogic(");
		if (fB == null) {
			buf.append(getLogicOpId());
		}
		buf.append(fA);
		if (fB != null) {
			buf.append(getLogicOpId());
			buf.append(fB);
		}
		buf.append(")");
		return buf.toString();
	}

	@Override
	public void dump(PrintStream out, int i) {
		printSpaces(out, i);
		out.print("OperationLogic, cnt=" + getCnt() + " op = " + getLogicOpId());
		fA.dump(out, i + 2);
		if (fB != null)
			fB.dump(out, i + 2);
		else {
			printSpaces(out, i + 2);
			out.println("null");
		}
	}

	@Override
	public String toVHDL() {

		StringBuilder buf = new StringBuilder();

		if (fB == null) {
			buf.append(getLogicOpId());
		}
		buf.append(fA.toVHDL());
		if (fB != null) {
			buf.append(getLogicOpId());
			buf.append(fB.toVHDL());
		}
		return buf.toString();
	}

	public LogicOp getOp() {
		return fOp;
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
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
		fA.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		if (fB != null)
			fB.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
	}

	private String getLogicOpId() {
		switch (fOp) {
		case AND:
			return "\"AND\"";
		case BUF:
			return "\"BUF\""; // FIXME: remove
		case NAND:
			return "\"NAND\"";
		case NOR:
			return "\"NOR\"";
		case NOT:
			return "\"NOT\"";
		case OR:
			return "\"OR\"";
		case XNOR:
			return "\"XNOR\"";
		case XOR:
			return "\"XOR\"";
		}
		return "";
	}

	@Override
	protected ArrayList<IGOperation> computeIGP(IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		ArrayList<IGOperation> res = new ArrayList<IGOperation>();
		
		String opId = getLogicOpId();

		IGOperationInvokeSubprogram inv = generateOperatorInvocation(opId, fA, fB, aTypeHint, aContainer, aEE, aCache, aErrorMode, aReport);
		if (inv != null) {
			res.add(inv);
		}

		return res;
	}

}