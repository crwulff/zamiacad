/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on May 28, 2007
 */

package org.zamia.vhdl.ast;

import java.io.PrintStream;
import java.util.ArrayList;

import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGAssertion;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGOperationLiteral;
import org.zamia.instgraph.IGType;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class Assertion extends VHDLNode {

	private Operation op, report;

	private Operation severity;

	public Assertion(Operation op_, VHDLNode parent_, long location_) {
		super(parent_, location_);
		op = op_;
		op.setParent(this);
	}

	public void setReport(Operation report_) {
		report = report_;
		if (report != null)
			report.setParent(this);
	}

	public void setSeverity(Operation severity_) {
		severity = severity_;
		if (severity != null)
			severity.setParent(this);
	}

	@Override
	public int getNumChildren() {
		return 2;
	}

	@Override
	public VHDLNode getChild(int idx_) {
		switch (idx_) {
		case 0:
			return op;
		case 1:
			return report;
		}
		return null;
	}

	public Operation getOp() {
		return op;
	}

	public void setOp(Operation op) {
		this.op = op;
	}

	public Operation getReport() {
		return report;
	}

	public Operation getSeverity() {
		return severity;
	}

	public void dumpVHDL(int indent_, PrintStream out_) {
		printIndented("ASSERT " + op.toVHDL(), indent_, out_);
		if (report != null)
			out_.print(" REPORT " + report.toVHDL());
		if (severity != null)
			out_.print(" SEVERITY " + report.toVHDL());
		out_.println(";");
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
		op.findReferences(id_, category_, RefType.Read, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		if (report != null) {
			report.findReferences(id_, category_, RefType.Read, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		}
		if (severity != null) {
			severity.findReferences(id_, category_, RefType.Read, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		}
	}

	public IGAssertion computeIG(IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache) throws ZamiaException {

		IGOperation zOp = op != null ? op.computeIGOperation(aContainer.findBoolType(), aContainer, aEE, aCache, ASTErrorMode.EXCEPTION, null) : null;

		IGType stringType = aContainer.findStringType();

		if (report == null) {
			report = new OperationLiteral("ASSERTION FAILED", OperationLiteral.LiteralCat.STRING, this, 0);
			report.setStartLine(fStartLine);
			report.setStartCol(fStartCol);
		}
		IGOperation zReport = report.computeIGOperation(stringType, aContainer, aEE, aCache, ASTErrorMode.EXCEPTION, null);

		IGType severityLevel = aContainer.findSeverityLevelType();

		IGOperation zSeverity = severity != null ? severity.computeIGOperation(severityLevel, aContainer, aEE, aCache, ASTErrorMode.EXCEPTION, null) : null;

		return new IGAssertion(zOp, zReport, zSeverity, getLocation(), aEE.getZDB());
	}

	public String toVHDL() {
		StringBuilder buf = new StringBuilder("ASSERT " + op.toVHDL());
		if (report != null) {
			buf.append(" REPORT ").append(report.toVHDL());
		}
		if (severity != null) {
			buf.append(" SEVERITY ").append(severity.toVHDL());
		}
		buf.append(";");
		return buf.toString();
	}

	@Override
	public String toString() {
		return toVHDL();
	}
}
