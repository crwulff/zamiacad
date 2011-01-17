/* 
 * Copyright 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 17, 2011
 */
package org.zamia.instgraph.synth.model;

import org.zamia.rtlng.RTLSignal;

/**
 * Little helper class used in assignment bindings computation:
 * 
 * holds an atomic signal plus a condition, under which it is driven
 * in this statement 
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSMConditionalTarget {

	private final RTLSignal fTarget;

	private final IGSMExprNode fCond;

	public IGSMConditionalTarget(RTLSignal aTarget, IGSMExprNode aCond) {
		fTarget = aTarget;
		fCond = aCond;
	}

	public RTLSignal getTarget() {
		return fTarget;
	}

	public IGSMExprNode getCond() {
		return fCond;
	}

}
