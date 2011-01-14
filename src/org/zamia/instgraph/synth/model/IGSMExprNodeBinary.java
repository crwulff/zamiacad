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
import org.zamia.instgraph.IGOperationBinary.BinOp;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.rtlng.RTLValue;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSMExprNodeBinary extends IGSMExprNode {

	private final IGSMExprNode fA;

	private final IGSMExprNode fB;

	private final BinOp fOp;

	IGSMExprNodeBinary(BinOp aOp, IGSMExprNode aA, IGSMExprNode aB, SourceLocation aLocation, IGSynth aSynth) {
		super(aA.getType(), aLocation, aSynth);

		fA = aA;
		fB = aB;
		fOp = aOp;
	}

	@Override
	public RTLValue getStaticValue() {
		return null;
	}

}
