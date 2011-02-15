/*
 * Copyright 2005-2011 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.instgraph.synth;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.synth.model.IGSMExprEngine;
import org.zamia.instgraph.synth.model.IGSMExprNode;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.RTLValue.BitValue;

/**
 * 
 * @author guenter bartsch
 * 
 */

public class IGBindingNodeValue extends IGBindingNode {

	public static final boolean dump = false;

	private final IGSMExprNode fValue;

	public IGBindingNodeValue(IGSMExprNode aValue, SourceLocation aLocation) {
		super(aLocation);
		fValue = aValue;
	}

	@Override
	public String toString() {
		return "IGBindingNodeValue(value=" + fValue + ")";
	}

	public IGSMExprNode getValue() {
		return fValue;
	}

	@Override
	public void dump(int aI) {
		logger.debug(aI, "IGBindingNodeValue");
		logger.debug(aI + 2, "value=" + fValue);
	}

	@Override
	public IGBindingNode replaceOmega(IGBindingNode aNode) {
		return this;
	}

	@Override
	public IGSMExprNode computeCombinedEnable(IGSynth aSynth) throws ZamiaException {
		IGSMExprEngine ee = IGSMExprEngine.getInstance();
		return ee.literal(aSynth.getBitValue(BitValue.BV_1), aSynth, fLocation);
	}

	@Override
	public RTLSignal synthesizeASyncData(IGSMExprNode aAE, RTLSignal aClk, IGSynth aSynth) throws ZamiaException {
		return fValue.synthesize(aSynth);
	}

	@Override
	public RTLSignal synthesizeSyncData(IGSMExprNode aAEn, RTLSignal aClk, IGSynth aSynth) throws ZamiaException {
		return fValue.synthesize(aSynth);
	}


}
