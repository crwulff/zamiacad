/*
 * Copyright 2006-2007 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/


package org.zamia.fsm;


import org.zamia.rtlng.RTLSignal;
import org.zamia.util.HashMapArray;
import org.zamia.vhdl.ast.OperationLiteral;


/**
 * A state in our state machine model.
 * 
 * Basically has an identifier, outputs and conditial 
 * transitions to other states
 * 
 * @author Guenter Bartsch
 */
public class State {

	private String id;
	private HashMapArray<RTLSignal, OperationLiteral> outputs;
	private HashMapArray<State, CondTransition> condTransitions;
	//private CondAssignmentsSet condOutput ;
	
	public State (String id_) {
		id = id_;
		outputs = new HashMapArray<RTLSignal, OperationLiteral>();
		condTransitions = new HashMapArray<State, CondTransition>();
		//condOutput = new CondAssignmentsSet();
	}
	
	public CondTransition createCondTransition(State dest_) {
		CondTransition ct = condTransitions.get(dest_);
		if (ct == null) {
			ct = new CondTransition(dest_);
			condTransitions.put(dest_, ct);
		}
		return ct;
	}

//	public State getDefTransition() {
//		
//		// FIXME: faster way / caching?
//		
//		CondTransition ct = null;
//		int n = condTransitions.size();
//		for (int i = 0; i<n; i++) {
//			ct = condTransitions.get(i);
//			if (ct.isDefault())
//				return ct.getNextState();
//		}
//		return null;
//	}

//	public void addCondOutput(String varId, CondNode cn, char l) {
//		condOutput.add (varId, cn, l);
//	}
//
//	public HashSetArray<String> getOutputs() {
//		HashSetArray<String> outputs = new HashSetArray<String>();
//
//		for (Iterator<String> i = defOutput.keySet().iterator(); i.hasNext();) {
//			String id = i.next();
//			outputs.add(id);
//		}
//		
//		HashSetArray<String> o = condOutput.getOutputs();
//		outputs.addAll(o);
//		
//		return outputs;
//	}
	
//	public HashSetArray<String> getInputs() {
//		HashSetArray<String> inputs = new HashSetArray<String>();
//
//		HashSetArray<String> o = condOutput.getInputs();
//		inputs.addAll(o);
//		
//		int n = condTransitions.size();
//		for (int i = 0; i < n; i++) {
//			CondTransition c = condTransitions.get(i);
//			o = c.getInputs();
//			inputs.addAll(o);
//		}
//		
//		return inputs;
//	}

//	public State findMatchingNextState(HashMap<String, Character> bindings) throws ZamiaException {
//		
//		int n = condTransitions.size();
//		for (int i = 0; i<n; i++) {
//			CondTransition ct = condTransitions.get(i);
//			
//			if (ct.getConditions().calc(bindings)== Value.BIT_1)
//				return ct.getNextState();
//		}
//		
//		return null;
//	}
//
//	public char getOutput(String output, HashMap<String, Character> bindings) throws ZamiaException {
//
//		char res = condOutput.getOutput(output, bindings);
//		if (res == Value.BIT_U)
//			return defOutput.get(output);
//		
//		return res;
//	}
	
//	public void dump(FSM fsm_, State l_, HashSetArray<String> inputs, HashSetArray<String> outputs, PrintStream out_) throws ZamiaException {
//		HashSetArray<String> inp = this.getInputs();
//
//		int n = inp.size();
//		long max = (1 << n);
//		for (long j = 0; j < max; j++) {
//			String el = Value.convert(j, n);
//
//			int k = 0;
//			int m = inputs.size();
//
//			HashMap<String, Character> bindings = new HashMap<String, Character>();
//
//			for (int i = 0; i < m; i++) {
//
//				String input = inputs.get(i);
//
//				if (inp.contains(input)) {
//					out_.print(el.charAt(k));
//
//					bindings.put(input, el.charAt(k));
//
//					k++;
//
//				} else
//					out_.print("-");
//			}
//			out_.print(" " + l_);
//
//			State nextState = this.findMatchingNextState(bindings);
//
//			if (nextState == null) {
//				nextState = getDefTransition();
//				if (nextState == null) {
//					fsm_.getDefTransition();
//
//					if (nextState == null)
//						nextState = l_;
//
//				}
//			}
//
//			out_.print("\t" + nextState + "\t");
//
//			m = outputs.size();
//
//			for (int i = 0; i < m; i++) {
//
//				String output = outputs.get(i);
//
//				char ks = this.getOutput(output, bindings);
//
//				if (ks == Value.BIT_U)
//					ks = fsm_.getDefaultOutput(output);
//
//				if (ks == Value.BIT_U)
//					out_.print("-");
//				else
//					out_.print(DumpKISS.convert(ks));
//			}
//
//			out_.println();
//		}
//	}

	public int getNumCondTransitions() {
		return condTransitions.size();
	}

	public CondTransition getCondTransition(int j) {
		return condTransitions.get(j);
	}

	public String toString() {
		return id;
	}

	public void addOutput(RTLSignal signal_, OperationLiteral res_) {
		outputs.put(signal_, res_);
	}

	public String getId() {
		return id;
	}

}
