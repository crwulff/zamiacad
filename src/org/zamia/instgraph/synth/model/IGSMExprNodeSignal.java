/* 
 * Copyright 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 13, 2011
 */
package org.zamia.instgraph.synth.model;

import java.util.Set;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.rtlng.RTLSignal;
import org.zamia.rtlng.RTLValue;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSMExprNodeSignal extends IGSMExprNode {

	private final RTLSignal fSignal;

	public IGSMExprNodeSignal(RTLSignal aSignal, SourceLocation aLocation, IGSynth aSynth) {
		super(aSignal.getType(), aLocation, aSynth);
		fSignal = aSignal;
	}

	@Override
	public RTLValue getStaticValue() {
		return null;
	}

	@Override
	public String toString() {
		return fSignal.getId();
	}
	
	@Override
	public IGSMExprNode replaceClockEdge(RTLSignal aClockSignal, RTLValue aValue, IGSynth aSynth) throws ZamiaException {
		return this;
	}
	
	@Override
	public void findClockEdges(Set<IGSMExprNodeClockEdge> aClockEdges) throws ZamiaException {
	}


	@Override
	public RTLSignal synthesize(IGSynth aSynth) throws ZamiaException {
		return fSignal;
	}

}
