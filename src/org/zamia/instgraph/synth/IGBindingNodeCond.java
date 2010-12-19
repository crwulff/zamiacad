/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 27, 2010
 */
package org.zamia.instgraph.synth;

import org.zamia.ZamiaException;
import org.zamia.instgraph.IGOperation;
import org.zamia.rtlng.RTLSignalAE;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGBindingNodeCond extends IGBindingNode {

	private final IGOperation fCond;

	private final IGBindingNode fBinding;

	public IGBindingNodeCond(IGOperation aCond, IGBindingNode aBinding) {
		super(aBinding.getObject());
		fCond = aCond;
		fBinding = aBinding;
	}

	public IGOperation getCond() {
		return fCond;
	}

	public IGBindingNode getBinding() {
		return fBinding;
	}

	@Override
	public String toString() {
		return "IGBindingNodeCond(cond=" + fCond + ", binding=" + fBinding + ")";
	}

	@Override
	public void dump(int aI) {
		logger.debug(aI, "IGBindingCond");
		logger.debug(aI + 2, "cond=%s", fCond);
		logger.debug(aI + 2, "binding=");
		fBinding.dump(aI + 2);
	}

	@Override
	public RTLSignalAE synthesize(IGSynth aSynth) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException ("Sorry, not implemented.");
	}

}
