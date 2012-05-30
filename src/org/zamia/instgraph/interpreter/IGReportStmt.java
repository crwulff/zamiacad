package org.zamia.instgraph.interpreter;

import org.apache.log4j.Level;
import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.interpreter.IGStmt.ReturnStatus;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.zdb.ZDB;

public class IGReportStmt extends IGStmt {

	private boolean fHaveSeverity;
	
	public IGReportStmt(boolean aHaveSeverity, SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);
		fHaveSeverity = aHaveSeverity;
	}	
	
	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime,
			ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
		
		IGStaticValue severity = null;
		if (fHaveSeverity) {
			severity = aRuntime.pop().getValue();
		}

		IGStaticValue report = aRuntime.pop().getValue();
		
		// By spec, NOTE is default
		String severityString = (severity != null) ? severity.toString() : "NOTE";
		int severityLevel = (severity != null) ? severity.getEnumOrd() : 0;

		logger.log(severityLevel, severityString + ": " + report, computeSourceLocation());
		
		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "REPORT";
	}	
}
