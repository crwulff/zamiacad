/*
 * Copyright 2005-2011 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.instgraph.synth;

import org.zamia.instgraph.synth.model.IGSMExprNode;

/**
 * 
 * @author guenter bartsch
 * 
 */

public class IGBindingNodeValue extends IGBindingNode {

	public static final boolean dump = false;

	private final IGSMExprNode fValue;

	public IGBindingNodeValue(IGSMExprNode aValue) {
		fValue = aValue;
	}

	@Override
	public String toString() {
		return "IGBindingNodeValue(value=" + fValue + ")";
	}

	public IGSMExprNode getValue() {
		return fValue;
	}

	@Override
	public void dump(int aI) {
		logger.debug(aI, "IGBindingNodeValue");
		logger.debug(aI + 2, "value=" + fValue);
	}

}
