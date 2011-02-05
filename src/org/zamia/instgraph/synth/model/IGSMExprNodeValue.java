/* 
 * Copyright 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 13, 2011
 */
package org.zamia.instgraph.synth.model;

import org.zamia.SourceLocation;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.rtlng.RTLValue;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSMExprNodeValue extends IGSMExprNode {

	private final RTLValue fValue;

	public IGSMExprNodeValue(RTLValue aValue, SourceLocation aLocation, IGSynth aSynth) {
		super(aValue.getType(), aLocation, aSynth);

		fValue = aValue;

	}

	@Override
	public String toString() {
		return "" + fValue;
	}

	@Override
	public RTLValue getStaticValue() {
		return fValue;
	}


}
