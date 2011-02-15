/* 
 * Copyright 2009,2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 15, 2009
 */
package org.zamia.rtl.sim;

import org.zamia.ZamiaLogger;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.RTLType;
import org.zamia.rtl.RTLValue;
import org.zamia.util.PathName;

/**
 * Sim data annotation for a signal (path, current value, ...)
 * 
 * @author Guenter Bartsch
 * 
 */

public class RTLSignalSimAnnotation {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private final PathName fPath;

	private final RTLType fType;

	private final RTLSignal fSignal;

	private final int fIdx;

	private final RTLSimContext fContext;

	private RTLSignalLogEntry fFirstEntry, fLastEntry;

	private RTLSignalLogEntry fCurEntry;

	private boolean fDoTrace = false;

	public RTLSignalSimAnnotation(PathName aPath, RTLSignal aSignal, int aIdx, RTLSimContext aContext) {
		fPath = aPath;
		fSignal = aSignal;
		fIdx = aIdx;
		fType = aSignal.getType();
		fContext = aContext;
	}

	public void add(long aCycles, RTLValue aValue, boolean aIsEvent) {

		RTLSignalLogEntry entry = new RTLSignalLogEntry(aCycles, aValue, aIsEvent);

		if (fDoTrace) {

			entry.fPrev = fLastEntry;

			if (fLastEntry != null) {
				fLastEntry.fNext = entry;
			}

			fLastEntry = entry;
			if (fFirstEntry == null) {
				fFirstEntry = entry;
			}

		} else {
			fFirstEntry = entry;
			fLastEntry = entry;
			fCurEntry = entry;
		}
	}

	public RTLValue getCurrentValue() {
		return fLastEntry.fValue;
	}

	public RTLSignalLogEntry getTransactionEntry(long aTime) {

		fCurEntry = fFirstEntry;

		while (fCurEntry.fNext != null && fCurEntry.fNext.fCycles <= aTime) {
			fCurEntry = fCurEntry.fNext;
		}

		return fCurEntry;
	}

	public RTLSignalLogEntry getEventEntry(long aTime) {
		RTLSignalLogEntry entry = getTransactionEntry(aTime);

		if (!entry.fIsEvent) {
			entry = entry.getPrevEvent();
		}

		return entry;
	}

	public RTLSignalLogEntry getNextTransactionEntry() {
		return fCurEntry.fNext;
	}

	public RTLSignalLogEntry getNextEventEntry() {
		return fCurEntry.getNextEvent();
	}

	public RTLSignalLogEntry getPrevEventEntry() {
		return fCurEntry.getPrevEvent();
	}

	public PathName getPath() {
		return fPath;
	}

	@Override
	public String toString() {
		return fPath.toString();
	}

	public RTLType getType() {
		return fType;
	}

	public long getStartTime() {
		return fFirstEntry.fCycles;
	}

	public RTLSignalLogEntry getStartEntry() {
		return fFirstEntry;
	}

	public void flush() {
		fFirstEntry = null;
		fLastEntry = null;
		fCurEntry = null;
	}

	public RTLSignalLogEntry getCurrentEntry() {
		return fCurEntry;
	}

	public void setCurrentEntry(RTLSignalLogEntry aEntry) {
		fCurEntry = aEntry;
	}

	public RTLSignalLogEntry getLastEntry() {
		return fLastEntry;
	}

	public RTLSignal getSignal() {
		return fSignal;
	}

	public int getIdx() {
		return fIdx;
	}

	public RTLSimContext getContext() {
		return fContext;
	}

	public void setTrace(boolean aDoTrace) {
		fDoTrace = aDoTrace;
	}

}
