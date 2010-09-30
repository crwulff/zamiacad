/* 

 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Oct 31, 2009
 */
package org.zamia.instgraph.sim.vcd;

import java.math.BigInteger;

import org.zamia.ZamiaException;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.sim.IGISimCursor;
import org.zamia.util.PathName;


/**
 * 
 * @author Guenter Bartsch
 *
 */
public class VCDSimCursor implements IGISimCursor {

	private VCDImport fVCDImport;

	private SignalInfo fCurSignalInfo;

	private VCDData fData;

	public VCDSimCursor(VCDImport aVCDImport) {
		fVCDImport = aVCDImport;
		fData = fVCDImport.getData();
	}

	@Override
	public boolean gotoTransition(PathName aSignalName, BigInteger aTimeOffset) throws ZamiaException {

		fCurSignalInfo = fData.getSignalInfo(aSignalName);
		if (fCurSignalInfo == null) {
			return false;
		}

		SignalLogEntry entry = fCurSignalInfo.getEventEntry(aTimeOffset);
		if (entry == null) {
			return false;
		}

		return true;
	}

	@Override
	public BigInteger getCurrentTime() throws ZamiaException {
		if (fCurSignalInfo == null) {
			return null;
		}
		SignalLogEntry entry = fCurSignalInfo.getCurrentEntry();
		if (entry == null) {
			return null;
		}
		return entry.fTime;
	}

	@Override
	public IGStaticValue getCurrentValue() throws ZamiaException {
		if (fCurSignalInfo == null) {
			return null;
		}
		SignalLogEntry entry = fCurSignalInfo.getCurrentEntry();
		if (entry == null) {
			return null;
		}
		return entry.fValue;
	}

	@Override
	public BigInteger gotoNextTransition(BigInteger aTimeLimit) throws ZamiaException {
		if (fCurSignalInfo == null) {
			return null;
		}
		SignalLogEntry entry = fCurSignalInfo.getNextEventEntry();
		if (entry == null) {
			return fData.getEndTime();
		}
		fCurSignalInfo.setCurEntry(entry);
		return entry.fTime;
	}

	@Override
	public BigInteger gotoPreviousTransition(BigInteger aTimeLimit) throws ZamiaException {
		if (fCurSignalInfo == null) {
			return null;
		}
		SignalLogEntry entry = fCurSignalInfo.getPrevEventEntry();
		if (entry == null) {
			return null;
		}
		fCurSignalInfo.setCurEntry(entry);
		return entry.fTime;
	}

	@Override
	public void dispose() {
	}

}
