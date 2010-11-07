/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 12, 2009
 */
package org.zamia.zil;

import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.VHDLNode;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class ZILAssertion extends ZILObject {

	private ZILOperation fOp, fReport, fSeverity;

	public ZILAssertion(ZILOperation aOp, ZILOperation aReport, ZILOperation aSeverity, ZILIContainer container, VHDLNode src) {
		super(ZILTypeVoid.getInstance(), container, src);
		fOp = aOp;
		fReport = aReport;
		fSeverity = aSeverity;
	}

	public void dump(int indent) {
		logger.debug(indent, "%s", toString());
	}

	@Override
	public String toString() {
		return "Assertion (op="+fOp+", report="+fReport+", severity="+fSeverity+")";
	}

	public ZILOperation getOp() {
		return fOp;
	}

	public ZILOperation getReport() {
		return fReport;
	}

	public ZILOperation getSeverity() {
		return fSeverity;
	}

	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		if (fOp != null) {
			fOp.computeReadSignals(aReadSignals);
		}
		if (fReport != null) {
			fReport.computeReadSignals(aReadSignals);
		}
		if (fSeverity != null) {
			fSeverity.computeReadSignals(aReadSignals);
		}
	}

	
	
//	@Override
//	protected Assertion inlineSubprograms(VariableRemapping vr_, SequenceOfStatements sos_, OperationCache cache_, String returnVarName_) throws ZamiaException {
//
//		Operation op = this.op.inlineSubprograms(vr_, sos_, cache_);
//		Operation report = this.report;
//		if (report != null)
//			report = report.inlineSubprograms(vr_, sos_, cache_);
//		Operation severity = this.severity;
//		if (severity != null)
//			severity = severity.inlineSubprograms(vr_, sos_, cache_);
//		
//		Assertion ass = new Assertion(op, sos_, location);
//		ass.setReport(report);
//		ass.setSeverity(severity);
//		return ass;
//	}

}
