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
public class OperationShift extends Operation {

	public enum ShiftOp {
		SLL, SRL, SLA, SRA, ROL, ROR
	};

	private ShiftOp fOp;

	private Operation fA, fB;

	public OperationShift(ShiftOp aOp, Operation aA, Operation aB, VHDLNode aParent, long aLocation) {
		super(aParent, aLocation);
		fOp = aOp;
		setOperand(aA, aB);
	}

	public void setOperand(Operation aA, Operation aB) {
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

	public ShiftOp getOp() {
		return fOp;
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

	public String toString() {
		StringBuffer buf = new StringBuffer("OperationShift(");
		buf.append(fA);
		buf.append(getShiftOpId());
		buf.append(fB + ")");
		return buf.toString();
	}

	@Override
	public void dump(PrintStream out, int i) {
		printSpaces(out, i);
		out.print("OperationShift, cnt=" + getCnt() + " op = " + getShiftOpId());
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

		buf.append(fA.toVHDL());
		buf.append(getShiftOpId());
		buf.append(fB.toVHDL());
		
		return buf.toString();
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
		fA.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		if (fB != null)
			fB.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
	}

	private String getShiftOpId() {
		switch (fOp) {
		case ROL:
			return "\"ROL\"";
		case ROR:
			return "\"ROR\"";
		case SLA:
			return "\"SLA\"";
		case SLL:
			return "\"SLL\"";
		case SRA:
			return "\"SRA\"";
		case SRL:
			return "\"SRL\"";
		}
		return "";
	}

	@Override
	protected ArrayList<IGOperation> computeIGP(IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		ArrayList<IGOperation> res = new ArrayList<IGOperation>();
		
		String opId = getShiftOpId();

		IGOperationInvokeSubprogram inv = generateOperatorInvocation(opId, fA, fB, aTypeHint, aContainer, aEE, aCache, aErrorMode, aReport);
		if (inv != null) {
			res.add(inv);
		}

		return res;
	}

}