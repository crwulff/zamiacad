/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 15, 2009
 */
package org.zamia.rtl.sim;

import org.zamia.ZamiaLogger;
import org.zamia.util.PathName;
import org.zamia.zil.ZILRange;
import org.zamia.zil.ZILType;
import org.zamia.zil.ZILTypeArray;
import org.zamia.zil.ZILTypeInteger;
import org.zamia.zil.ZILValue;


/**
 * Information about a signal for which simulation data exists
 * 
 * @author Guenter Bartsch
 * 
 */

public class SignalInfo {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private PathName fPath;

	private int fWidth;

	private int fIdx1 = -1, fIdx2 = -1;
	private int fMinIdx;

	private ZILType fType;

	private SignalLogEntry fFirstEntry, fLastEntry;

	private SignalLogEntry fCurEntry;

	public SignalInfo() {
	}

	public SignalInfo(PathName aPath, int aWidth, int aIdx1, int aIdx2) {
		fPath = aPath;
		fWidth = aWidth;
		fIdx1 = aIdx1;
		fIdx2 = aIdx2;
	}

	public void add(long aTime, ZILValue aValue, boolean aIsEvent) {

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

	public SignalLogEntry getTransactionEntry(long aTime) {
		
		fCurEntry = fFirstEntry;
		
		while (fCurEntry.fNext != null && fCurEntry.fNext.fTime<=aTime) {
			fCurEntry = fCurEntry.fNext;
		}
		
		return fCurEntry;
	}
	
	public SignalLogEntry getEventEntry(long aTime) {
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

	public int getWidth() {
		return fWidth;
	}

	public void setWidth(int aWidth) {
		fWidth = aWidth;
	}

	public int getIdx1() {
		return fIdx1;
	}

	public void setIdx1(int aIdx1) {
		fIdx1 = aIdx1;
	}

	public int getIdx2() {
		return fIdx2;
	}

	public void setIdx2(int aIdx2) {
		fIdx2 = aIdx2;
	}

	@Override
	public String toString() {
		return fPath + ":" + fWidth;
	}

	public void setType(ZILType aType) {
		fType = aType;
	}

	public void guessType() {
		if (fWidth == 1) {

			fType = ZILType.bit;
			
		} else {

			int w = Math.abs(fIdx1 - fIdx2) + 1;

			if (w == fWidth) {

				long left = fIdx1, right = fIdx2;
				boolean ascending = left < right;

				fMinIdx = ascending ? fIdx1 : fIdx2; 
				
				ZILRange range = new ZILRange(left, right, ascending, null, null);
				
				ZILTypeInteger idxType = new ZILTypeInteger(range, ZILType.intType, null, null, null);

				fType = new ZILTypeArray(idxType, ZILType.bit, false, null, null, null);

			} else {
				
				// ignore idx

				logger.error("SignalInfo: guessType idx mismatch: width=%d, idx1=%d, idx2=%d", fWidth, fIdx1, fIdx2);

				long left = 0, right = fWidth - 1;
				boolean ascending = true;
				
				fMinIdx = 0;

				ZILRange range = new ZILRange(left, right, ascending, null, null);

				ZILTypeInteger idxType = new ZILTypeInteger(range, ZILType.intType, null, null, null);

				fType = new ZILTypeArray(idxType, ZILType.bit, false, null, null, null);

			}
		}
	}
	
	public ZILType getType() {
		return fType;
	}

	public long getStartTime() {
		return fFirstEntry.fTime;
	}

	public SignalLogEntry getStartEntry() {
		return fFirstEntry;
	}

	public int getMinIdx() {
		return fMinIdx;
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
