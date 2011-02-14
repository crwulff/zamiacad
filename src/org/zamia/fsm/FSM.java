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
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.OperationLiteral;

/**
 * @author guenter bartsch
 */
public class FSM {
	private String id;
	private RTLSignal clock;
	private HashMapArray<String, State> stg;
	private RTLSignal stateSignal, nextStateSignal;
	private HashSetArray<RTLSignal> outputs;

	FSM(String name) {
		this.id = name;
		stg = new HashMapArray<String,State>();
		outputs = new HashSetArray<RTLSignal>();
	}

	public RTLSignal getClock() {
		return clock;
	}

	public void setClock(RTLSignal clock) {
		this.clock = clock;
	}

	public String getId() {
		return id;
	}

	public RTLSignal getNextStateSignal() {
		return nextStateSignal;
	}

	public void setNextStateSignal(RTLSignal nextStateSignal_) {
		nextStateSignal = nextStateSignal_;
	}

	public RTLSignal getStateSignal() {
		return stateSignal;
	}

	void setStateSignal(RTLSignal stateSignal_) {
		stateSignal = stateSignal_;
	}

	public void addState(State s) {
		stg.put(s.getId(), s);
	}

	public int getNumOutputs () {
		return outputs.size();
	}
	
	public RTLSignal getOutput (int i_) {
		return outputs.get(i_);
	}

	public State getState(String id_) {
		return stg.get(id_);
	}

	public State createState(String stateId) {
		
		State state = stg.get(stateId);
		if (state == null) {
			state = new State (stateId);
			stg.put(stateId, state);
		}
		return state;
	}


	public int getNumStates() {
		return stg.size();
	}
	
	public State getState (int i_) {
		return stg.get(i_);
	}


	public void addOutput(State s, RTLSignal signal, OperationLiteral res) {
		outputs.add(signal);
		s.addOutput(signal, res);
	}
}
