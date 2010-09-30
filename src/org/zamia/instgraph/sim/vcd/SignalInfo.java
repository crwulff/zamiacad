/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 15, 2009
 */
package org.zamia.instgraph.sim.vcd;

import java.math.BigInteger;

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

public class SignalInfo {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private PathName fPath;

	private IGTypeStatic fType;

	private SignalLogEntry fFirstEntry, fLastEntry;

	private SignalLogEntry fCurEntry;

	public SignalInfo(PathName aPath, IGTypeStatic aType) {
		fPath = aPath;
		fType = aType;
	}

	public void add(BigInteger aTime, IGStaticValue aValue, boolean aIsEvent) {

		SignalLogEntry entry = new SignalLogEntry(aTime, aValue, aIsEvent);

		entry.fPrev = fLastEntry;

		if (fLastEntry != null) {
			fLastEntry.fNext = entry;
		}

		fLastEntry = entry;
		if (fFirstEntry == null) {
			fFirstEntry = entry;
		}
	}

	public SignalLogEntry getTransactionEntry(BigInteger aTime) {

		fCurEntry = fFirstEntry;

		while (fCurEntry.fNext != null && fCurEntry.fNext.fTime.compareTo(aTime) <= 0) {
			fCurEntry = fCurEntry.fNext;
		}

		return fCurEntry;
	}

	public SignalLogEntry getEventEntry(BigInteger aTime) {
		SignalLogEntry entry = getTransactionEntry(aTime);

		if (!entry.fIsEvent) {
			entry = entry.getPrevEvent();
		}

		return entry;
	}

	public SignalLogEntry getNextTransactionEntry() {
		return fCurEntry.fNext;
	}

	public SignalLogEntry getNextEventEntry() {
		return fCurEntry.getNextEvent();
	}

	public SignalLogEntry getPrevEventEntry() {
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
		return fPath + ":" + fType;
	}

	public IGTypeStatic getType() {
		return fType;
	}

	public BigInteger getStartTime() {
		return fFirstEntry.fTime;
	}

	public SignalLogEntry getStartEntry() {
		return fFirstEntry;
	}

	public void flush() {
		fFirstEntry = null;
		fLastEntry = null;
		fCurEntry = null;
	}

	public SignalLogEntry getCurrentEntry() {
		return fCurEntry;
	}

	public void setCurEntry(SignalLogEntry aEntry) {
		fCurEntry = aEntry;
	}

	public SignalLogEntry getLastEntry() {
		return fLastEntry;
	}

}
