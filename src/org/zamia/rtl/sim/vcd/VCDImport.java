/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 16, 2009
 */
package org.zamia.rtl.sim.vcd;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.zamia.FSCache;
import org.zamia.SourceFile;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.rtl.RTLGraph;
import org.zamia.rtl.sim.ISimulator;
import org.zamia.rtl.sim.SignalInfo;
import org.zamia.rtl.sim.SignalLogEntry;
import org.zamia.rtl.sim.SimObserver;
import org.zamia.util.PathName;
import org.zamia.zil.ZILType;
import org.zamia.zil.ZILValue;


/**
 * A (at the moment proof-of-concept) simulator plugin that reads simulation
 * data from a VCD file on disk
 * 
 * @author Guenter Bartsch
 * 
 */
public class VCDImport implements ISimulator {

	private static FSCache fsCache = FSCache.getInstance();

	private VCDData fData;

	private VCDParser fParser;

	private SignalInfo fCurSignalInfo;

	public VCDImport() {

		fParser = new VCDParser();

	}

	public void open(File aVCDFile) throws IOException, ZamiaException {

		SourceFile sf = new SourceFile(aVCDFile);

		Reader reader = fsCache.openFile(sf, false);

		fData = fParser.parse(reader, sf);

		reader.close();
	}
	
	public void open(RTLGraph aGraph) {
		throw new RuntimeException ("VCDImport: cannot handle RTL Graphs.");
	}
	
	public void shutdown() {
		// GC will take care of us.
	}

	public long getStartTime() {
		return fData.getStartTime();
	}

	public long getTimeScale() {
		return fData.getTimeScale();
	}

	public long getEndTime() {
		return fData.getEndTime();
	}

	public boolean isSimulator() {
		return false;
	}

	public int getInterfaceVersion() {
		return 1;
	}

	public ZILType getSignalType(int aSignalIdx) {
		SignalInfo si = fData.getSignalInfo(aSignalIdx);
		if (si == null) {
			return null;
		}
		return si.getType();
	}
	
	public int findSignalIdx(PathName aPath) {
		return fData.findSignalIdx(aPath);
	}

	public List<Integer> findSignalIdxRegexp(String aSearchString, int aLimit) {
		
		ArrayList<PathName> sl = fData.findSignalPaths(aSearchString, aLimit);
		int n = sl.size();
		ArrayList<Integer> res = new ArrayList<Integer>(n);
		for (int i = 0; i<n; i++) {
			res.add(findSignalIdx(sl.get(i)));
		}
		
		return res;
	}


	public void getValue(long aTimeOffset, int aSignalIdx, ZILValue aRetValue, SourceLocation aSourceLocation) throws ZamiaException {
		fCurSignalInfo = fData.getSignalInfo(aSignalIdx);

		SignalLogEntry entry = fCurSignalInfo.getEventEntry(aTimeOffset);

		aRetValue.modifyValue(entry.fValue);
	}

	public long getNextValue(long aTimeLimit, ZILValue aRetValue, SourceLocation aSourceLocation) throws ZamiaException {
		SignalLogEntry entry = fCurSignalInfo.getNextEventEntry();

		if (entry != null) {

			aRetValue.modifyValue(entry.fValue);
			fCurSignalInfo.setCurEntry(entry);

			return entry.fTime;
		}

		entry = fCurSignalInfo.getCurrentEntry();
		aRetValue.modifyValue(entry.fValue);
		return getEndTime();
	}

	public long getPrevValue(long aTimeLimit, ZILValue aRetValue, SourceLocation aSourceLocation) throws ZamiaException {
		SignalLogEntry entry = fCurSignalInfo.getPrevEventEntry();

		if (entry != null) {

			aRetValue.modifyValue(entry.fValue);
			fCurSignalInfo.setCurEntry(entry);

			return entry.fTime;
		}

		entry = fCurSignalInfo.getCurrentEntry();
		aRetValue.modifyValue(entry.fValue);
		return getEndTime();
	}

	public void reset() {
	}

	public void run(long aTime) {
	}

	public void trace(int aSignalIdx) {
	}

	public void unTrace(int aSignalIdx) {
	}

	public void assign(int aSignalIdx, ZILValue aValue) {
	}

	public void addObserver(SimObserver aO) {
	}

	public void removeObserver(SimObserver aO) {
	}

	public PathName getSignalName(int aIdx) {
		return fData.getSignalName(aIdx);
	}

	public int getNumSignals() {
		return fData.getNumSignals();
	}

}
