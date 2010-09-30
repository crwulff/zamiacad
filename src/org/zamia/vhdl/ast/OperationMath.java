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
public class OperationMath extends Operation {

	public enum MathOp {
		NEG, ADD, SUB, MUL, DIV, MOD, REM, POWER, ABS, POS
	}

	private MathOp fOp;

	private Operation fA, fB;

	public OperationMath(MathOp aOp, Operation aA, Operation aB, ASTObject aParent, long aLocation) {
		super(aParent, aLocation);
		fOp = aOp;
		setA(aA);
		setB(aB);
	}

	private void setB(Operation aB) {
		fB = aB;
		if (fB != null)
			fB.setParent(this);
	}

	private void setA(Operation aA) {
		fA = aA;
		if (fA != null)
			fA.setParent(this);
	}

	public OperationMath(MathOp aOp, Operation aA, ASTObject aParent, long aLocation) {
		this(aOp, aA, null, aParent, aLocation);
	}

	public Operation getOperandA() {
		return fA;
	}

	public Operation getOperandB() {
		return fB;
	}

	public MathOp getOp() {
		return fOp;
	}

	public String toString() {
		return toVHDL();
	}

	@Override
	public void dump(PrintStream aOut, int aIndent) {
		printSpaces(aOut, aIndent);
		aOut.print("OperationMath, cnt=" + cnt + ", op=" + getMathOpId());
		fA.dump(aOut, aIndent + 2);
		if (fB != null)
			fB.dump(aOut, aIndent + 2);
		else {
			printSpaces(aOut, aIndent + 2);
			aOut.println("null");
		}
	}

	@Override
	public int getNumChildren() {
		return 2;
	}

	@Override
	public ASTObject getChild(int aIdx) {
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

		if (fB == null) {
			buf.append(getMathOpId());
		}

		buf.append(fA.toVHDL());

		if (fB != null) {
			buf.append(getMathOpId());
			buf.append(fB.toVHDL());
		}
		return buf.toString();
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
		fA.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		if (fB != null)
			fB.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
	}

	private String getMathOpId() {
		switch (fOp) {
		case ABS:
			return "\"ABS\"";
		case ADD:
			return "\"+\"";
		case DIV:
			return "\"/\"";
		case MOD:
			return "\"MOD\"";
		case MUL:
			return "\"*\"";
		case NEG:
			return "\"-\"";
		case POS:
			return "\"+\"";
		case POWER:
			return "\"**\"";
		case REM:
			return "\"REM\"";
		case SUB:
			return "\"-\"";
		}
		return "";
	}

	@Override
	protected ArrayList<IGOperation> computeIGP(IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		ArrayList<IGOperation> res = new ArrayList<IGOperation>();

		String opId = getMathOpId();

		IGOperationInvokeSubprogram inv = generateOperatorInvocation(opId, fA, fB, aTypeHint, aContainer, aEE, aCache, aErrorMode, aReport);
		if (inv != null) {
			res.add(inv);
		}

		return res;
	}

}