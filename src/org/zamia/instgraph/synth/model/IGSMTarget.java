/* 
 * Copyright 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 8, 2011
 */
package org.zamia.instgraph.synth.model;

import java.util.List;

import org.zamia.SourceLocation;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.rtl.RTLType;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public abstract class IGSMTarget {

	protected final RTLType fType;

	private final SourceLocation fLocation;

	protected final IGSynth fSynth;

	protected IGSMTarget(RTLType aType, SourceLocation aLocation, IGSynth aSynth) {
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
	
	protected abstract void computeTargets(List<IGSMConditionalTarget> aTargetList);

}
