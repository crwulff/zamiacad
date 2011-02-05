/* 
 * Copyright 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 8, 2011
 */
package org.zamia.instgraph.synth.model;

import org.zamia.SourceLocation;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.rtlng.RTLType;
import org.zamia.rtlng.RTLValue;

/**
 * @author Guenter Bartsch
 *
 */

public abstract class IGSMExprNode {

	protected final RTLType fType;

	protected final SourceLocation fLocation;

	protected final IGSynth fSynth;

	protected IGSMExprNode(RTLType aType, SourceLocation aLocation, IGSynth aSynth) {
		fType = aType;
		fLocation = aLocation;
		fSynth = aSynth;
	}

	public RTLType getType() {
		return fType;
	}

	public SourceLocation getLocation() {
		return fLocation;
	}

	public IGSynth getSynth() {
		return fSynth;
	}

	public abstract RTLValue getStaticValue();

}
