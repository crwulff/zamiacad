/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 23, 2010
 */
package org.zamia.instgraph.synth.adapters;

import org.zamia.ZamiaException;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationIndex;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.synth.IGBindings;
import org.zamia.instgraph.synth.IGClock;
import org.zamia.instgraph.synth.IGObjectRemapping;
import org.zamia.instgraph.synth.IGOperationSynthAdapter;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.rtlng.RTLModule;
import org.zamia.rtlng.RTLSignal;
import org.zamia.rtlng.RTLType;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSAOperationIndex extends IGOperationSynthAdapter {

	@Override
	public IGOperation inlineSubprograms(IGOperation aOperation, IGObjectRemapping aOR, IGSequenceOfStatements aInlinedSOS, IGSynth aSynth) throws ZamiaException {
		
		IGOperationIndex oidx = (IGOperationIndex) aOperation;
		
		IGOperation op = oidx.getOperand();
		IGOperation idx = oidx.getIndex();
		
		IGOperation op2 = aSynth.getSynthAdapter(op).inlineSubprograms(op, aOR, aInlinedSOS, aSynth);
		IGOperation idx2 = aSynth.getSynthAdapter(idx).inlineSubprograms(idx, aOR, aInlinedSOS, aSynth);

		return new IGOperationIndex(idx2, op2, oidx.getType(), oidx.computeSourceLocation(), aSynth.getZDB());
	}

	@Override
	public IGOperation resolveVariables(IGOperation aOperation, IGBindings aBindings, IGSequenceOfStatements aResolvedSOS, IGClock aClock, IGObjectRemapping aOR, IGSynth aSynth)
			throws ZamiaException {
		
		IGOperationIndex oidx = (IGOperationIndex) aOperation;
		
		IGOperation op = oidx.getOperand();
		IGOperation idx = oidx.getIndex();
		
		IGOperation op2 = aSynth.getSynthAdapter(op).resolveVariables(op, aBindings, aResolvedSOS, aClock, aOR, aSynth);
		IGOperation idx2 = aSynth.getSynthAdapter(idx).resolveVariables(idx, aBindings, aResolvedSOS, aClock, aOR, aSynth);

		return new IGOperationIndex(idx2, op2, oidx.getType(), oidx.computeSourceLocation(), aSynth.getZDB());
	}

	@Override
	public RTLSignal synthesizeValue(IGOperation aOperation, IGSynth aSynth) throws ZamiaException {
		IGOperationIndex oidx = (IGOperationIndex) aOperation;
		
		IGOperation op = oidx.getOperand();
		IGOperation idx = oidx.getIndex();

		RTLSignal sop = aSynth.getSynthAdapter(op).synthesizeValue(op, aSynth);
		RTLSignal sidx = aSynth.getSynthAdapter(idx).synthesizeValue(idx, aSynth);

		RTLModule module = aSynth.getRTLModule();

		return module.createComponentArrayIdx(sop, sidx, oidx.computeSourceLocation());
	}

	@Override
	public RTLSignal synthesizeEnable(IGOperation aOperation, IGSynth aSynth) throws ZamiaException {
		IGOperationIndex oidx = (IGOperationIndex) aOperation;
		
		IGOperation op = oidx.getOperand();
		IGOperation idx = oidx.getIndex();

		RTLType t = aSynth.synthesizeType(op.getType());
		RTLType et = t.computeEnableType();
		
		RTLSignal sidx = aSynth.getSynthAdapter(idx).synthesizeValue(idx, aSynth);

		RTLModule module = aSynth.getRTLModule();

		return module.createComponentDecoder(et.computeCardinality(), sidx, oidx.computeSourceLocation());
	}

}
