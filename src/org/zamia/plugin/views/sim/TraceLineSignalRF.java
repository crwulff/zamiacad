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
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.sim.IGISimCursor;
import org.zamia.plugin.views.sim.SimulatorView.TraceDisplayMode;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class TraceLineSignalRF extends TraceLineSignal {

	private String fField;
	private TraceLineSignal fParent;

	public TraceLineSignalRF(TraceLineSignal aParent, String aField, TraceDisplayMode aTDM, int aColor, IGTypeStatic aType) {
		fField = aField;
		fTDM = aTDM;
		fColor = aColor;
		fType = aType;
		fUID = aParent.getUID()+"."+fField;
		fLabel = "."+fField;
		fParent = aParent;
		fSignalPath = aParent.getSignalPath();
		if (fType.isArray()) {
			try {
				fLabel = fLabel+" ("+fType.getStaticIndexType(null)+")";
			} catch (ZamiaException e) {
				el.logException(e);
			}
		}
	}

	@Override
	IGStaticValue getValue(IGISimCursor aCursor, BigInteger aCursorTime) {
		
		IGStaticValue value = fParent.getValue(aCursor, aCursorTime);
		if (value != null) {
			try {
				value = value.getRecordFieldValue(fField, null);
			} catch (Throwable t) {
			}
		}
		
		return value;
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
		if (value != null) {
			try {
				value = value.getRecordFieldValue(fField, null);
			} catch (Throwable t) {
			}
		}
		return value;
	}
	

	@Override
	public void save(ArrayList<String> aTraces, int aNumChildren) {
		String str = "rf:" + fField + ":" + fColor+ ":" + fTDM.name() + ":" + aNumChildren;
		aTraces.add(str);
	}

	@Override
	public boolean isFullSignal() {
		return false;
	}

}
