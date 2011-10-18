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
import java.util.Collection;
import java.util.LinkedList;

import org.zamia.ExceptionLogger;
import org.zamia.SourceFile;
import org.zamia.SourceLocation;
import org.zamia.ZamiaLogger;
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

	public void filterExecutedSource(Collection<SourceLocation> aExecuted) {
		if (aExecuted == null) {
			aExecuted = new LinkedList<SourceLocation>();
		}
		for (IGStmt stmt : program) {
			int count = stmt.getExecCount();
			if (count > 0) {
				aExecuted.add(stmt.computeSourceLocation());
			}
		}
	}
}
