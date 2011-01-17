/* 
 * Copyright 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 14, 2011
 */
package org.zamia.instgraph.synth.model;

import java.util.List;

import org.zamia.SourceLocation;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.rtlng.RTLSignal;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSMTargetSignal extends IGSMTarget {

	private RTLSignal fSignal;

	public IGSMTargetSignal(RTLSignal aSignal, SourceLocation aLocation, IGSynth aSynth) {
		super(aSignal.getType(), aLocation, aSynth);

		fSignal = aSignal;
	}

	@Override
	public String toString() {
		return fSignal.getId();
	}

	@Override
	protected void computeTargets(List<IGSMConditionalTarget> aTargetList) {
		aTargetList.add(new IGSMConditionalTarget(fSignal, null));
	}

}
