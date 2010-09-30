/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 27, 2009
 */
package org.zamia.plugin.views.sim;

import java.math.BigInteger;
import java.util.ArrayList;

import org.zamia.ZamiaException;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGStaticValueBuilder;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.sim.IGISimCursor;
import org.zamia.plugin.views.sim.SimulatorView.TraceDisplayMode;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class TraceLineSignalArraySlice extends TraceLineSignal {

	private TraceLineSignal fParent;

	private int fMin;

	private int fMax;

	public TraceLineSignalArraySlice(TraceLineSignal aParent, int aMin, int aMax, TraceDisplayMode aTDM, int aColor, IGTypeStatic aType) {
		fMin = aMin;
		fMax = aMax;
		fTDM = aTDM;
		fColor = aColor;
		fType = aType;
		fUID = aParent.getUID() + "." + fMin + ":" + fMax;
		fLabel = fMin != fMax ? "(" + fMin + ":" + fMax + ")" : "(" + fMin + ")";
		fParent = aParent;
		fSignalPath = aParent.getSignalPath();
	}

	private IGStaticValue apply(IGStaticValue aValue) {
		IGStaticValue value = aValue;
		if (value != null) {
			try {

				if (fMin == fMax) {
					value = value.getValue(fMin, null);
				} else {

					IGStaticValueBuilder builder = new IGStaticValueBuilder(fType, null, null);

					for (int i = fMin; i <= fMax; i++) {
						builder.set(i, value.getValue(i, null), null);
					}

					value = builder.buildConstant();
				}
			} catch (Throwable t) {
			}
		}
		return value;
	}

	@Override
	IGStaticValue getValue(IGISimCursor aCursor, BigInteger aCursorTime) {

		IGStaticValue value = fParent.getValue(aCursor, aCursorTime);

		return apply(value);
	}

	@Override
	public BigInteger gotoPreviousTransition(IGISimCursor aCursor, BigInteger aTimeLimit) throws ZamiaException {

		IGStaticValue currentValue = getCurrentValue(aCursor);
		if (currentValue == null) {
			return aTimeLimit;
		}

		IGStaticValue prevValue = currentValue;
		BigInteger time = aTimeLimit;
		while (currentValue.equalsValue(prevValue)) {
			time = aCursor.gotoPreviousTransition(aTimeLimit);
			if (time.compareTo(aTimeLimit) <= 0) {
				return time;
			}

			prevValue = getCurrentValue(aCursor);
			if (prevValue == null) {
				return time;
			}
		}
		return time;
	}

	@Override
	public BigInteger gotoNextTransition(IGISimCursor aCursor, BigInteger aTimeLimit) throws ZamiaException {

		IGStaticValue currentValue = getCurrentValue(aCursor);
		if (currentValue == null) {
			return aTimeLimit;
		}

		IGStaticValue nextValue = currentValue;
		BigInteger time = aTimeLimit;
		while (currentValue.equalsValue(nextValue)) {
			time = aCursor.gotoNextTransition(aTimeLimit);
			if (time.compareTo(aTimeLimit) >= 0) {
				return time;
			}

			nextValue = getCurrentValue(aCursor);
			if (nextValue == null) {
				return time;
			}
		}
		return time;
	}

	@Override
	public IGStaticValue getCurrentValue(IGISimCursor aCursor) throws ZamiaException {
		IGStaticValue value = fParent.getCurrentValue(aCursor);
		return apply(value);
	}

	@Override
	public void save(ArrayList<String> aTraces, int aNumChildren) {
		String str = "slice:" + fMin + ":" + fMax + ":" + fColor+ ":" + fTDM.name() + ":" + aNumChildren;
		aTraces.add(str);
	}

	@Override
	public boolean isFullSignal() {
		return false;
	}

}
