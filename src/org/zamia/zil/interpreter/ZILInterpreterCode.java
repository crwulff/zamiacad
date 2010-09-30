/*
 * Copyright 2005-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.zil.interpreter;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.zamia.ZamiaLogger;


/**
 * @author Guenter Bartsch
 * 
 * This class holds compiled code for a process which was not synthesizable and
 * therefore is represented by a process interpreter component in the netlist.
 * 
 * the interpreter is a stack machine
 * 
 */

@SuppressWarnings("serial")
public class ZILInterpreterCode implements Serializable {

	private ArrayList<ZILStmt> program = new ArrayList<ZILStmt>();
	private String fId;

	public ZILInterpreterCode(String aId) {
		fId = aId;
	}

	public void add (ZILStmt stmt_) {
		program.add(stmt_);
	}

	public int getNextAdr() {
		return program.size();
	}

	public void defineLabel (ZILLabel label_) {
		label_.setAdr(getNextAdr());
	}	

	public ZILStmt get(int idx_) {
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

			ZILStmt stmt = (ZILStmt) program.get(i);
			zl.debug ("%5d %s", i, stmt.toString());
			
		}
	}

	public void dump(PrintStream out) {
		int n = program.size();
		for (int i = 0; i < n; i++) {

			ZILStmt stmt = (ZILStmt) program.get(i);
			out.printf ("%5d %s\n", i, stmt.toString());
			
		}
	}

	public int getNumStmts() {
		return program.size();
	}

	public ZILStmt getStmt(int idx_) {
		return program.get(idx_);
	}


}


