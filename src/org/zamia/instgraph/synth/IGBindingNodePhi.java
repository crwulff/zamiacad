/* 
 * Copyright 2010,2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 27, 2010
 */
package org.zamia.instgraph.synth;

import org.zamia.instgraph.synth.model.IGSMExprNode;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGBindingNodePhi extends IGBindingNode {

	private final IGSMExprNode fCond;

	private final IGBindingNode fThenBinding, fElseBinding;

	public IGBindingNodePhi(IGSMExprNode aCond, IGBindingNode aThenBinding, IGBindingNode aElseBinding) {
		fCond = aCond;
		fThenBinding = aThenBinding;
		fElseBinding = aElseBinding;
	}

	public IGSMExprNode getCond() {
		return fCond;
	}

	public IGBindingNode getThenBinding() {
		return fThenBinding;
	}

	public IGBindingNode getElseBinding() {
		return fElseBinding;
	}

	@Override
	public String toString() {
		return "Φ(" + fCond + "," + fThenBinding + "," + fElseBinding + ")";
	}

	@Override
	public void dump(int aI) {
		logger.debug(aI, "IGBindingPhi cond=%s", fCond);
		logger.debug(aI + 1, "then=");
		if (fThenBinding != null) {
			fThenBinding.dump(aI + 2);
		} else {
			logger.debug(aI + 2, "Omega");
		}
		logger.debug(aI + 1, "else=");
		if (fElseBinding != null) {
			fElseBinding.dump(aI + 2);
		} else {
			logger.debug(aI + 2, "Omega");
		}
	}

}
