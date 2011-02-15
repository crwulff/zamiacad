/*
 * Copyright 2007,2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.fsm;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;

import org.zamia.ZamiaException;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.rtl.RTLSignal;
import org.zamia.util.HashMapArray;
import org.zamia.util.HashSetArray;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class CondAssignmentsSet {

	HashMapArray<String, CondAssignments> assignments;

	public CondAssignmentsSet () {
		assignments = new HashMapArray<String, CondAssignments>();
	}
	
	public void set(RTLSignal bit, char l, CondNode cond_) {
		
		String id = bit.getId();
		
		CondAssignments ca = assignments.get(id);
		if (ca == null) {
			ca = new CondAssignments (id);
			assignments.put(id, ca);
		}
		
		ca.addAssignment (cond_, l);
		
	}

	public boolean containsKey(String id) {
		return assignments.containsKey(id);
	}

	public void dump(PrintStream out) {
		for (Iterator<String> i = assignments.keySet().iterator(); i.hasNext();) {
			String id = i.next();
			
			out.println("   Assignments for signal: "+id);
			
			CondAssignments ca = assignments.get(id);
			ca.dump(out);
		}
	}

//	public String getConstantAssignment(Signal s) throws ZamiaException {
//		
//		
//		if (s instanceof SignalBit) {
//
//			CondAssignments ca = assignments.get(s.getId());
//			if (ca == null)
//				throw new ZamiaException ("Not driven: "+s);
//
//			return ca.getConstantAssignment();
//			
//		} else {
//			SignalAggregate bus = (SignalAggregate) s;
//
//			StringBuffer buf = new StringBuffer(bus.getNumSignals());
//			
//			for (int i = bus.getNumSignals()-1; i>=0; i--) {
//				
//				SignalBit bit = bus.getSignal(i);
//				buf.append(getConstantAssignment(bit));
//			}
//			return buf.toString();
//		}
//	}

	public CondAssignments getCondAssignments(String id) {
		return assignments.get(id);
		
	}
	
	public int getNumCAs() {
		return assignments.size();
	}
	
	public CondAssignments getCA(int i) {
		return assignments.get(i);
	}

	public void add(String varId, CondNode cn, char l) {
		CondAssignments cas = assignments.get(varId);
		if (cas == null) {
			cas = new CondAssignments(varId);
			assignments.put (varId, cas);
		}
		cas.addAssignment(cn, l);
	}

	public HashSetArray<String> getOutputs() {
		HashSetArray<String> outputs = new HashSetArray<String>();

		for (Iterator<String> i = assignments.keySet().iterator(); i.hasNext();) {
			String id = i.next();
			outputs.add(id);
		}
		
		return outputs;
	}

	public HashSetArray<String> getInputs() {
		HashSetArray<String> inputs = new HashSetArray<String>();

		int n = assignments.size();
		for (int i = 0; i<n; i++) {
			CondAssignments cas = assignments.get(i);
			HashSetArray<String> inp = cas.getInputs ();
			inputs.addAll(inp);
		}
		
		
		return inputs;
	}

	public char getOutput(String output, HashMap<String, Character> bindings) throws ZamiaException {

		CondAssignments cas = assignments.get(output);
		
		if (cas == null)
			return IGStaticValue.BIT_U;
		
		return cas.getOutput (bindings);
	}
	
}
