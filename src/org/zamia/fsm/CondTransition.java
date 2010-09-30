/*
 * Copyright 2007 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Feb 22, 2007
 */

package org.zamia.fsm;

import org.zamia.vhdl.ast.Operation;

/**
 * @author guenter bartsch
 */
public class CondTransition {

	private State nextState;
	private Operation condition;
	private boolean isDefault = false;
		
	public CondTransition(State next_) {
		nextState = next_;
	}
		
	public State getNextState() {
		return nextState;
	}
		
	public boolean isDefault() {
		return isDefault;
	}
	
	public void setDefault (boolean isDefault_) {
		isDefault = isDefault_;
	}

	public void setCondition(Operation op) {
		condition = op;
	}

	public Operation getCondition() {
		return condition;
	}
}
