/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 15, 2009
 */
package org.zamia.instgraph.sim.ref;


import java.math.BigInteger;

import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.util.PathName;

/**
 * Information about a signal for which simulation data exists
 *
 * @author Guenter Bartsch
 *
 */

public class IGSignalLog {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private PathName fPath;

	private IGSignalLogEntry fFirstEntry, fLastEntry;

	private IGSignalLogEntry fCurEntry;

	public IGSignalLog(PathName aPath) {
		fPath = aPath;
	}

	public void add(BigInteger aTime, IGStaticValue aValue, boolean aIsEvent) {

		IGSignalLogEntry entry = new IGSignalLogEntry(aTime, aValue, aIsEvent);

		entry.fPrev = fLastEntry;

		if (fLastEntry != null) {
			fLastEntry.fNext = entry;
		}

		fLastEntry = entry;
		if (fFirstEntry == null) {
			fFirstEntry = entry;
		}
	}

	public IGSignalLogEntry getTransactionEntry(BigInteger aTime) {

		IGSignalLogEntry entry = fFirstEntry;

		IGSignalLogEntry nextEvent;
		while ((nextEvent = entry.getNextEvent()) != null && nextEvent.fTime.compareTo(aTime) <= 0) {
			entry = nextEvent;
		}

		return entry;
	}

	public IGSignalLogEntry getEventEntry(BigInteger aTime) {
		IGSignalLogEntry entry = getTransactionEntry(aTime);

		if (!entry.fIsEvent) {
			entry = entry.getPrevEvent();
		}

		return entry;
	}

	public IGSignalLogEntry getNextTransactionEntry() {
		return fCurEntry.fNext;
	}

	public IGSignalLogEntry getNextEventEntry() {
		return fCurEntry.getNextEvent();
	}

	public IGSignalLogEntry getPrevEventEntry() {
		return fCurEntry.getPrevEvent();
	}

	public PathName getPath() {
		return fPath;
	}

	public void setPath(PathName aPath) {
		fPath = aPath;
	}

	@Override
	public String toString() {
		return fPath.toString();
	}

	public BigInteger getStartTime() {
		return fFirstEntry.fTime;
	}

	public IGSignalLogEntry getStartEntry() {
		return fFirstEntry;
	}

	public void flush() {
		fFirstEntry = null;
		fLastEntry = null;
		fCurEntry = null;
	}

	public IGSignalLogEntry getCurrentEntry() {
		return fCurEntry;
	}

	public void setCurEntry(IGSignalLogEntry aEntry) {
		fCurEntry = aEntry;
	}

	public IGSignalLogEntry getLastEntry() {
		return fLastEntry;
	}

	public void fillLeadingU() throws ZamiaException {

		if (fFirstEntry != fLastEntry) {
			throw new ZamiaException("IGSignalLog: signal history with only 1 log entry can be filled with 'U'-s");
		}

		// create U value
		IGTypeStatic type = fLastEntry.fValue.getStaticType();
		IGStaticValue uValue = IGStaticValue.generateZ(type, null);
		// create and insert U entry
		fFirstEntry = new IGSignalLogEntry(BigInteger.ZERO, uValue, false);
		fFirstEntry.fNext = fLastEntry;
		fLastEntry.fPrev = fFirstEntry;
	}

	public IGSignalLogEntry removeLastEntry() {
		if (fLastEntry == null) { // no entries
			return null;
		}
		IGSignalLogEntry lastEntry = fLastEntry;
		if (fLastEntry.fPrev == null) { // only 1 entry
			fFirstEntry = null;
			fLastEntry = null;
			return lastEntry;
		}
		// more than 1 entry
		fLastEntry = fLastEntry.fPrev;
		fLastEntry.fNext = null;
		return lastEntry;
	}
}