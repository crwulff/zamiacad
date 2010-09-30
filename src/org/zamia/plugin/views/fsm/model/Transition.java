/*
 * Copyright 2006-2007 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/



package org.zamia.plugin.views.fsm.model;

import org.zamia.vhdl.ast.Operation;
/**
 * @author guenter bartsch
 */


@SuppressWarnings("serial")
public class Transition extends FSMElement {

	public FSMState source, target;
	private Operation cond;
	private boolean isDefault;

	public Transition(FSMState source, FSMState target, Operation cond_, boolean isDefault_) {
		this.source = source;
		this.target = target;

		source.addOutput(this);
		target.addInput(this);
		cond = cond_;
		isDefault = isDefault_;
	}

	public String getConditionsAsString() {
		
		if (isDefault)
			return "default";
		
		if (cond != null) {
			return cond.toVHDL();
		}
		
		
		return "";
	}

}
