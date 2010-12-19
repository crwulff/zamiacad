/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 28, 2010
 */
package org.zamia.instgraph.synth;

import org.zamia.ZamiaException;
import org.zamia.instgraph.IGObject;
import org.zamia.rtlng.RTLSignalAE;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGBindingNodeEMux extends IGBindingNode {

	private final IGBindingNode fBindingA;

	private final IGBindingNode fBindingB;

	public IGBindingNodeEMux(IGBindingNode aBindingA, IGBindingNode aBindingB, IGObject aObj) {
		super(aObj);
		fBindingA = aBindingA;
		fBindingB = aBindingB;
	}

	public IGBindingNode getBindingA() {
		return fBindingA;
	}

	public IGBindingNode getBindingB() {
		return fBindingB;
	}

	@Override
	public String toString() {
		return "IGBindingNodeEMux(a=" + fBindingA + ", b=" + fBindingB + ")";
	}
	
	@Override
	public void dump(int aI) {
		logger.debug(aI, "IGBindingNodeEMux");
		logger.debug(aI + 2, "bindingA=");
		fBindingA.dump(aI + 4);
		logger.debug(aI + 2, "bindingB=");
		fBindingB.dump(aI + 4);
	}

	@Override
	public RTLSignalAE synthesize(IGSynth aSynth) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException ("Sorry, not implemented.");
	}

}
