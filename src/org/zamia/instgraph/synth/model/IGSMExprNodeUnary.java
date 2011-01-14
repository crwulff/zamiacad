/* 
 * Copyright 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 9, 2011
 */
package org.zamia.instgraph.synth.model;

import org.zamia.SourceLocation;
import org.zamia.instgraph.IGOperationUnary.UnaryOp;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.rtlng.RTLValue;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSMExprNodeUnary extends IGSMExprNode {

	IGSMExprNodeUnary(UnaryOp aOp, IGSMExprNode aA, SourceLocation aLocation, IGSynth aSynth) {
		super (aA.getType(), aLocation, aSynth);
	}

	@Override
	public RTLValue getStaticValue() {
		return null;
	}

}
