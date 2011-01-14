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
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGObject.IGObjectCat;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationObject;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.synth.IGBinding;
import org.zamia.instgraph.synth.IGBindings;
import org.zamia.instgraph.synth.IGClock;
import org.zamia.instgraph.synth.IGObjectRemapping;
import org.zamia.instgraph.synth.IGOperationSynthAdapter;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.rtlng.RTLModule;
import org.zamia.rtlng.RTLSignal;
import org.zamia.rtlng.RTLType;
import org.zamia.rtlng.RTLValue.BitValue;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSAOperationObject extends IGOperationSynthAdapter {

	@Override
	public IGOperation inlineSubprograms(IGOperation aOperation, IGObjectRemapping aOR, IGSequenceOfStatements aInlinedSOS, IGSynth aSynth) throws ZamiaException {

		IGOperationObject oobj = (IGOperationObject) aOperation;

		IGObject obj = oobj.getObject();

		IGObject obj2 = aOR.get(obj);

		if (obj.store() == obj2.store()) {
			return oobj;
		}

		return new IGOperationObject(obj2, oobj.computeSourceLocation(), aSynth.getZDB());

	}

	@Override
	public IGOperation resolveVariables(IGOperation aOperation, IGBindings aBindings, IGSequenceOfStatements aResolvedSOS, IGClock aClock, IGObjectRemapping aOR, IGSynth aSynth)
			throws ZamiaException {
		IGOperationObject oobj = (IGOperationObject) aOperation;

		IGObject obj = oobj.getObject();

		IGObject obj2 = aOR.get(obj);

		IGBinding binding = aBindings.getBinding(obj2);

		IGOperationObject oobj2 = new IGOperationObject(obj2, oobj.computeSourceLocation(), aSynth.getZDB());
		aSynth.setVariableOperationBinding(oobj2, binding);

		return oobj2;
	}

	@Override
	public RTLSignal synthesizeValue(IGOperation aOperation, IGSynth aSynth) throws ZamiaException {
		IGOperationObject oobj = (IGOperationObject) aOperation;

		IGObject obj = oobj.getObject();

		if (obj.getCat() == IGObjectCat.VARIABLE) {
			IGBinding binding = aSynth.getVariableOperationBinding(oobj);

			if (binding == null) {
				throw new ZamiaException("Error: unbound variable detected.", oobj.computeSourceLocation());
			}

			return binding.synthesize(aSynth);
		}

		return aSynth.getOrCreateSignal(obj);
	}

	@Override
	public RTLSignal synthesizeEnable(IGOperation aOperation, IGSynth aSynth) throws ZamiaException {

		IGOperationObject oobj = (IGOperationObject) aOperation;

		IGObject obj = oobj.getObject();

		IGType type = obj.getType();

		RTLType t = aSynth.synthesizeType(type);

		RTLType et = t.computeEnableType();

		RTLModule module = aSynth.getRTLModule();

		return module.createBitLiteral(BitValue.BV_1, et, oobj.computeSourceLocation());
	}

}
