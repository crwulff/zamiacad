/*
 * Copyright 2005-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.instgraph.interpreter;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeSet;

import org.zamia.ExceptionLogger;
import org.zamia.SourceFile;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.instgraph.interpreter.logger.IGHitCountLogger;
import org.zamia.instgraph.interpreter.logger.IGLogicalExpressionLogger;
import org.zamia.util.HashMapArray;


/**
 * @author Guenter Bartsch
 * 
 *         This class holds compiled code for a process which was not
 *         synthesizable and therefore is represented by a process interpreter
 *         component in the netlist.
 * 
 *         the interpreter is a stack machine
 * 
 */

@SuppressWarnings("serial")
public class IGInterpreterCode implements Serializable {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	private ArrayList<IGStmt> program = new ArrayList<IGStmt>();

	private Tracer muted;

	private Tracer assignmentTracer;

	private String fId;

	private HashMapArray<String, IGLabel> fLoopExitLabels = new HashMapArray<String, IGLabel>();

	private SourceLocation fLocation;

	public IGInterpreterCode(String aId, SourceLocation aLocation) {
		fId = aId;
		fLocation = aLocation;
	}

	public void add(IGStmt stmt_) {
		program.add(stmt_);
	}

	public int getNextAdr() {
		return program.size();
	}

	public void defineLabel(IGLabel label_) {
		label_.setAdr(getNextAdr());
	}

	public IGStmt get(int idx_) {
		return program.get(idx_);
	}

	public int size() {
		return program.size();
	}

	public String getId() {
		return fId;
	}

	public void dump() {

		ZamiaLogger zl = ZamiaLogger.getInstance();

		int n = program.size();
		for (int i = 0; i < n; i++) {

			IGStmt stmt = (IGStmt) program.get(i);
			zl.debug("%5d %s", i, stmt.toStringStat());

		}
	}

	public void dump(PrintStream out) {
		int n = program.size();
		for (int i = 0; i < n; i++) {

			IGStmt stmt = (IGStmt) program.get(i);
			out.printf("%5d %s\n", i, stmt.toStringStat());

		}
	}

	/**
	 * Advanced dump function
	 */

	public void dump(int aPC, PrintStream aOut) {
		int n = program.size();

		SourceLocation l = null;

		for (int i = 0; i < n; i++) {
			IGStmt stmt = (IGStmt) program.get(i);

			SourceLocation l2 = stmt.computeSourceLocation();
			if (l2 != null && (l == null || l.fLine != l2.fLine)) {
				l = l2;

				SourceFile sf = l.fSF;

				String line = sf.extractLine(l.fLine);

				aOut.printf("    \n");
				aOut.printf("    # %s: %s\n", l, line);
				aOut.printf("    \n");
			}

			if (i != aPC) {
				aOut.printf("    %5d %s\n", i, stmt.toString());
			} else {
				aOut.printf("==> %5d %s\n", i, stmt.toString());
			}

		}
	}

	public void dumpToLogger(int aPC) {
		int n = program.size();

		SourceLocation l = null;

		for (int i = 0; i < n; i++) {
			IGStmt stmt = (IGStmt) program.get(i);

			SourceLocation l2 = stmt.computeSourceLocation();
			if (l2 != null && (l == null || l.fLine != l2.fLine)) {
				l = l2;

				SourceFile sf = l.fSF;

				String line = sf.extractLine(l.fLine);

				logger.debug("InterpreterCode:    ");
				logger.debug("InterpreterCode:    # %s: %s", l, line);
				logger.debug("InterpreterCode:    ");
			}

			if (i != aPC) {
				logger.debug("InterpreterCode:    %5d %s", i, stmt.toString());
			} else {
				logger.debug("InterpreterCode:==> %5d %s", i, stmt.toString());
			}
		}
	}

	public int getNumStmts() {
		return program.size();
	}

	public IGStmt getStmt(int idx_) {
		return program.get(idx_);
	}

	public void addLoopExitLabel(String aLoopLabel, IGLabel aExitLabel) {
		fLoopExitLabels.put(aLoopLabel, aExitLabel);
	}

	public void removeLoopExitLabel(String aLoopLabel) {
		fLoopExitLabels.remove(aLoopLabel);
	}

	public IGLabel getLoopExitLabel(String aLoopLabel) {
		return fLoopExitLabels.get(aLoopLabel);
	}

	public IGLabel getLoopExitLabel() {
		int n = fLoopExitLabels.size();
		if (n == 0)
			return null;
		return fLoopExitLabels.get(n - 1);
	}

	public int getLoopExitLabelNesting(IGLabel aNextLabel) {
		int n = fLoopExitLabels.size();
		for (int i = n - 1; i >= 0; i--) {
			if (fLoopExitLabels.get(i).equals(aNextLabel)) {
				return n - i;
			}
		}
		return 0;
	}

	@Override
	public String toString() {
		return "IGInterpreterCode(id=" + fId + " from " + fLocation + ")";
	}

	public void collectExecutedLines(IGHitCountLogger aLineLogger) {
		for (int i = 0, programSize = program.size(); i < programSize; i++) {
			if (muted != null && muted.traced.contains(i)) {
				continue;
			}
			IGStmt stmt = program.get(i);
			int count = stmt.getExecCount();
			if (count > 0) {
				aLineLogger.logHit(stmt.computeSourceLocation(), count);
			}
		}
	}

	public void collectAllAssignments(IGHitCountLogger aAssignmentLogger) {
		filterAssignments(aAssignmentLogger, -1);
	}

	public void collectExecutedAssignments(IGHitCountLogger aAssignmentLogger) {
		filterAssignments(aAssignmentLogger, 0);
	}

	public void filterAssignments(IGHitCountLogger aAssignmentLogger, int aThreshold) {

		if (assignmentTracer == null) {
			return;
		}
		for (Integer ass : assignmentTracer.traced) {

			IGStmt stmt = program.get(ass);

			int count = stmt.getExecCount();

			if (count > aThreshold) {
				aAssignmentLogger.logHit(stmt.computeSourceLocation(), count);
			}
		}
	}

	public void collectExecutedConditions(IGLogicalExpressionLogger aConditionLogger) {
		for (IGStmt stmt : program) {
			if (stmt instanceof IGCallStmt) {
				IGCallStmt callStmt = (IGCallStmt) stmt;

				if (!callStmt.isRelational()) {
					continue;
				}

				aConditionLogger.logExpr(callStmt.getOpLocation(), callStmt.hasTrueOccurred(), callStmt.hasFalseOccurred());

			} else if (stmt instanceof IGBinaryOpStmt) {
				IGBinaryOpStmt binaryOpStmt = (IGBinaryOpStmt) stmt;

				if (!binaryOpStmt.isRelational()) {
					continue;
				}

				aConditionLogger.logExpr(binaryOpStmt.computeSourceLocation(), binaryOpStmt.hasTrueOccurred(), binaryOpStmt.hasFalseOccurred());
			}
		}
	}

	public void collectExecutedBranches(IGLogicalExpressionLogger aBranchLogger) {
		for (IGStmt stmt : program) {
			if (stmt instanceof IGJumpNCStmt) {
				IGJumpNCStmt jmpNcStmt = (IGJumpNCStmt) stmt;

				aBranchLogger.logExpr(jmpNcStmt.computeSourceLocation(), jmpNcStmt.hasTrueOccurred(), jmpNcStmt.hasFalseOccurred());
			}
		}
	}

	public void muteExecCount() throws ZamiaException {
		getMuted().startTracing();
	}

	public void unmuteExecCount() throws ZamiaException {
		getMuted().stopTracing();
	}

	private Tracer getMuted() {
		if (muted == null) {
			muted = new IGInterpreterCode.Tracer("Muted statements");
		}
		return muted;
	}

	public void startTracingAssignments() throws ZamiaException {
		getAssignmentTracer().startTracing();
	}

	public void stopTracingAssignments() throws ZamiaException {
		getAssignmentTracer().stopTracing();
	}

	private Tracer getAssignmentTracer() {
		if (assignmentTracer == null) {
			assignmentTracer = new IGInterpreterCode.Tracer("Assignment statements");
		}
		return assignmentTracer;
	}

	class Tracer implements Serializable {

		boolean isBeingTraced;

		TreeSet<Integer> traced;

		String title;

		Tracer(String title) {
			this.title = title;
		}

		void startTracing() throws ZamiaException {

			if (isBeingTraced) {
				SourceLocation lastLoc = program.get(program.size() - 1).computeSourceLocation();
				throw new ZamiaException("IGInterpreterCode: tracing of " + title + " is already started. Last statement source: " + lastLoc, fLocation);
			}
			if (traced == null) {
				traced = new TreeSet<Integer>();
			}
			traced.add(program.size());
			isBeingTraced = true;
		}

		void stopTracing() throws ZamiaException {
			if (!isBeingTraced) {
				SourceLocation lastLoc = program.get(program.size() - 1).computeSourceLocation();
				throw new ZamiaException("IGInterpreterCode: tracing of " + title + " is not started yet. Last statement source: " + lastLoc, fLocation);
			}
			for (int i = traced.last() + 1; i < program.size(); i++) {
				traced.add(i);
			}
			isBeingTraced = false;
		}
	}
}
