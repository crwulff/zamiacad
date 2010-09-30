/*
 * Copyright 2007,2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jul 15, 2007
 */

package org.zamia.rtl.sim;

import org.zamia.rtl.RTLSignal;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class SignalChange {
	private RTLSignal signal;

	private ZILValue value;

	private boolean fIsEvent;

	public SignalChange(RTLSignal aSignal, ZILValue aValue, boolean aIsEvent) {
		signal = aSignal;
		value = aValue;
		fIsEvent = aIsEvent;
	}

	public RTLSignal getSignal() {
		return signal;
	}

	public ZILValue getValue() {
		return value;
	}
	
	public boolean isEvent() {
		return fIsEvent;
	}

//	public void setSignal(RTLSignal aSignal) {
//		signal = aSignal;
//	}
//
//	public void setValue(ZILValue aValie) {
//		value = aValie;
//	}

}
