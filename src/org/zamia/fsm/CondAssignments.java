/*
 * Copyright 2007,2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.fsm;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.zamia.ZamiaException;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.util.HashSetArray;



/**
 * 
 * @author Guenter Bartsch
 *
 */

public class CondAssignments {

	static class CondAssignment {
		public CondAssignment(CondNode cond_, char l_) {
			cn = cond_;
			l = l_;
		}

		CondNode cn;

		char l;

		public CondNode getCondition() {
			return cn;
		}

		public char getAssignment() {
			return l;
		}

		public HashSetArray<String> getInputs() {
			return cn.getInputs();
		}
	}

	String var;

	ArrayList<CondAssignment> ass;

	public CondAssignments(String var_) {
		var = var_;
		ass = new ArrayList<CondAssignment>();
	}

	public void addAssignment(CondNode cond_, char l_) {
		ass.add(new CondAssignment(cond_, l_));
	}

	public void dump(PrintStream out) {

		for (int i = 0; i < ass.size(); i++) {
			CondAssignment ca = ass.get(i);
			out.println("      cond = " + ca.cn + " l=" + ca.l);
		}
	}

//	public String getConstantAssignment() throws ZamiaException {
//
//		char l = Value.BIT_U;
//		int n = ass.size();
//		for (int i = 0; i < n; i++) {
//
//			CondAssignment ca = ass.get(i);
//			if (ca.cn != null)
//				new ZamiaException("Non-constant assignment to " + var);
//
//			l = ca.l;
//		}
//		return "" + DumpKISS.convert(l);
//	}

	public int getNumAssignments() {
		return ass.size();
	}

	public CondNode getCondition(int i) {
		return (ass.get(i)).getCondition();
	}

	public char getAssignment(int i) {
		return (ass.get(i)).getAssignment();
	}

	public HashSetArray<String> getInputs() {
		HashSetArray<String> inputs = new HashSetArray<String>();

		int n = ass.size();
		for (int i = 0; i<n; i++) {
			CondAssignment ca = ass.get(i);
			HashSetArray<String> inp = ca.getInputs ();
			inputs.addAll(inp);
		}
		
		
		return inputs;
	}

	public char getOutput(HashMap<String, Character> bindings) throws ZamiaException {
		
		int n = ass.size();
		for (int i = 0; i<n; i++) {
			CondAssignment ca = ass.get(i);
			if (ca.cn.calc(bindings) == IGStaticValue.BIT_1)
				return ca.l;
		}
		return IGStaticValue.BIT_U;
	}
}
