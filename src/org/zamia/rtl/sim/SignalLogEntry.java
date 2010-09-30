/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.rtl.sim;

import org.zamia.zil.ZILValue;

/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class SignalLogEntry {
	public long fTime;

	public ZILValue fValue;
	
	public SignalLogEntry fNext, fPrev;
	
	public boolean fIsEvent; // false -> just a transaction (value has not changed)

	public SignalLogEntry(long aTime, ZILValue aValue, boolean aIsEvent) {
		fTime = aTime;
		fValue = aValue;
		
		if (aValue == null) {
			System.out.println ("NULL valued signal log entry!");
		}
		
		fIsEvent = aIsEvent;
	}
	
	public SignalLogEntry getNextEvent() {
		SignalLogEntry entry = fNext;
		while (entry != null && !entry.fIsEvent) {
			entry = entry.fNext;
		}
		return entry;
	}

	public SignalLogEntry getPrevEvent() {
		SignalLogEntry entry = fPrev;
		while (entry != null && !entry.fIsEvent) {
			entry = entry.fPrev;
		}
		return entry;
	}
}
