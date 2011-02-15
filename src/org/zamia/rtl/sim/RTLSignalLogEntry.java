/*
 * Copyright 2007-2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.rtl.sim;

import org.zamia.rtl.RTLValue;

/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class RTLSignalLogEntry {
	
	public final long fCycles;

	public final RTLValue fValue;
	
	public RTLSignalLogEntry fNext, fPrev;
	
	public boolean fIsEvent; // false -> just a transaction (value has not changed)

	public RTLSignalLogEntry(long aCycles, RTLValue aValue, boolean aIsEvent) {
		fCycles = aCycles;
		fValue = aValue;
		
		if (aValue == null) {
			System.out.println ("NULL valued signal log entry!");
		}
		
		fIsEvent = aIsEvent;
	}
	
	public RTLSignalLogEntry getNextEvent() {
		RTLSignalLogEntry entry = fNext;
		while (entry != null && !entry.fIsEvent) {
			entry = entry.fNext;
		}
		return entry;
	}

	public RTLSignalLogEntry getPrevEvent() {
		RTLSignalLogEntry entry = fPrev;
		while (entry != null && !entry.fIsEvent) {
			entry = entry.fPrev;
		}
		return entry;
	}
}
