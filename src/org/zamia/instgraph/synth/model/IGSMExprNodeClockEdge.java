/* 
 * Copyright 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 3, 2011
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

public class IGSMExprNodeClockEdge extends IGSMExprNode {

	private final RTLSignal fSignal;

	private final boolean fRisingEdge;

	public IGSMExprNodeClockEdge(RTLSignal aSignal, boolean aRisingEdge, SourceLocation aLocation, IGSynth aSynth) {
		super(aSignal.getType(), aLocation, aSynth);
		fSignal = aSignal;
		fRisingEdge = aRisingEdge;
	}

	public RTLSignal getSignal() {
		return fSignal;
	}

	public boolean isRisingEdge() {
		return fRisingEdge;
	}

	public String toString() {
		if (fRisingEdge)
			return "↑" + fSignal.getId();
		return "↓" + fSignal.getId();
	}

	@Override
	public RTLValue getStaticValue() {
		return null;
	}

	@Override
	public IGSMExprNode replaceClockEdge(RTLSignal aClockSignal, RTLValue aValue, IGSynth aSynth) throws ZamiaException {
		if (!aClockSignal.equals(fSignal))
			return this;
		
		return ee.literal(aValue, aSynth, getLocation());
	}
	
	@Override
	public void findClockEdges(Set<IGSMExprNodeClockEdge> aClockEdges) throws ZamiaException {
		aClockEdges.add(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (fRisingEdge ? 1231 : 1237);
		result = prime * result + ((fSignal == null) ? 0 : fSignal.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IGSMExprNodeClockEdge other = (IGSMExprNodeClockEdge) obj;
		if (fRisingEdge != other.fRisingEdge)
			return false;
		if (fSignal == null) {
			if (other.fSignal != null)
				return false;
		} else if (!fSignal.equals(other.fSignal))
			return false;
		return true;
	}

	@Override
	public RTLSignal synthesize(IGSynth aSynth) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException ("Sorry, not implemented.");
	}


}
