/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 23, 2010
 */
package org.zamia.instgraph.synth.adapters;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.synth.IGBindings;
import org.zamia.instgraph.synth.IGClock;
import org.zamia.instgraph.synth.IGObjectRemapping;
import org.zamia.instgraph.synth.IGOperationSynthAdapter;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.rtlng.RTLManager;
import org.zamia.rtlng.RTLModule;
import org.zamia.rtlng.RTLSignal;
import org.zamia.rtlng.RTLType;
import org.zamia.rtlng.RTLValue;
import org.zamia.rtlng.RTLValue.BitValue;
import org.zamia.rtlng.RTLValueBuilder;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSAStaticValue extends IGOperationSynthAdapter {

	@Override
	public IGOperation inlineSubprograms(IGOperation aOperation, IGObjectRemapping aOR, IGSequenceOfStatements aInlinedSOS, IGSynth aSynth) throws ZamiaException {
		return aOperation;
	}

	@Override
	public IGOperation resolveVariables(IGOperation aOperation, IGBindings aBindings, IGSequenceOfStatements aResolvedSOS, IGClock aClock, IGObjectRemapping aOR, IGSynth aSynth)
			throws ZamiaException {
		return aOperation;
	}

	@Override
	public RTLSignal synthesizeValue(IGOperation aOperation, IGSynth aSynth) throws ZamiaException {

		IGStaticValue sv = (IGStaticValue) aOperation;

		SourceLocation location = sv.computeSourceLocation();

		IGType igt = sv.getType();

		RTLType rtlt = aSynth.synthesizeType(igt);

		RTLValueBuilder b = new RTLValueBuilder(rtlt, location, aSynth.getZDB());

		switch (rtlt.getCat()) {
		case BIT:

			switch (sv.computeBit()) {
			case '0':
				b.setBit(BitValue.BV_0);
				break;
			case '1':
				b.setBit(BitValue.BV_1);
				break;
			case 'U':
				b.setBit(BitValue.BV_U);
				break;
			case 'X':
				b.setBit(BitValue.BV_X);
				break;
			case 'Z':
				b.setBit(BitValue.BV_Z);
				break;
			default:
				// FIXME: implement
				throw new ZamiaException("Sorry, not implemented yet.");
			}

			break;

		case ARRAY:

			switch (igt.getCat()) {
			case INTEGER:
				long l = sv.getOrd();

				int low = rtlt.getArrayLow();
				int high = rtlt.getArrayHigh();
				int c = high - low + 1;

				RTLManager rtlm = aSynth.getRTLM();

				RTLType bitType = rtlm.getBitType();

				RTLValueBuilder b2 = new RTLValueBuilder(bitType, location, aSynth.getZDB());
				b2.setBit(BitValue.BV_0);
				RTLValue bit0 = b2.buildValue();
				b2 = new RTLValueBuilder(bitType, location, aSynth.getZDB());
				b2.setBit(BitValue.BV_1);
				RTLValue bit1 = b2.buildValue();

				int mask = 1;
				for (int i = 0; i < c; i++) {

					b.set(i + low, (l & mask) != 0 ? bit1 : bit0, location);

					mask = mask << 1;

				}

				break;
			default:
				// FIXME: implement
				throw new ZamiaException("Sorry, not implemented yet.");
			}

			break;

		default:
			// FIXME: implement
			throw new ZamiaException("Sorry, not implemented yet.");
		}

		RTLValue rtlv = b.buildValue();

		RTLModule module = aSynth.getRTLModule();

		return module.createLiteral(rtlv, location);

	}

}
