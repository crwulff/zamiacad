/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.instgraph.sim.ref;


import java.math.BigInteger;

import org.zamia.instgraph.IGStaticValue;

/**
 *
 * @author Guenter Bartsch
 *
 */
public class IGSignalLogEntry {
	public BigInteger fTime;

	public IGStaticValue fValue;

	public IGSignalLogEntry fNext, fPrev;

	public boolean fIsEvent; // false -> just a transaction (value has not changed)

	public IGSignalLogEntry(BigInteger aTime, IGStaticValue aValue, boolean aIsEvent) {
		fTime = aTime;
		fValue = aValue;

		if (aValue == null) {
			System.out.println ("NULL valued signal log entry!");
		}

		fIsEvent = aIsEvent;
	}

	public IGSignalLogEntry getNextEvent() {
		IGSignalLogEntry entry = fNext;
		while (entry != null && !entry.fIsEvent) {
			entry = entry.fNext;
		}
		return entry;
	}

	public IGSignalLogEntry getPrevEvent() {
		IGSignalLogEntry entry = fPrev;
		while (entry != null && !entry.fIsEvent) {
			entry = entry.fPrev;
		}
		return entry;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(fTime).append("(").append(fValue).append(")").append(fIsEvent ? " E" : "").append("\n");
		if (fNext != null) {
			sb.append(fNext);
		}
		return sb.toString();
	}
}