package org.zamia.instgraph.sim.ref;


import java.math.BigInteger;

import org.zamia.ZamiaException;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.sim.IGISimCursor;
import org.zamia.util.PathName;

/**
 * @author Anton Chepurov
 */
public class RefSimCursor implements IGISimCursor {

	private final SimData fData;

	private IGSignalInfo curSignalInfo;

	public RefSimCursor(SimData aData) {
		fData = aData;
	}

	@Override
	public boolean gotoTransition(PathName aSignalName, BigInteger aTimeOffset) throws ZamiaException {
		curSignalInfo = fData.getSignalInfo(aSignalName);
		if (curSignalInfo == null) {
			/* If signal is not being traced, then start tracing it forcedly */
			curSignalInfo = fData.traceForcedly(aSignalName);
		}
		return curSignalInfo.getTransactionEntry(aTimeOffset) != null;
	}

	@Override
	public BigInteger gotoNextTransition(BigInteger aTimeLimit) throws ZamiaException { 
		if (curSignalInfo == null) {
			return null;
		}
		IGSignalLogEntry signalLogEntry = curSignalInfo.getNextEventEntry();
		if (signalLogEntry == null) {
			return fData.getEndTime();
		}
		curSignalInfo.setCurEntry(signalLogEntry);
		return signalLogEntry.fTime;
	}

	@Override
	public BigInteger gotoPreviousTransition(BigInteger aTimeLimit) throws ZamiaException {
		if (curSignalInfo == null) {
			return null;
		}
		IGSignalLogEntry signalLogEntry = curSignalInfo.getPrevEventEntry();
		if (signalLogEntry == null) {
			return null;
		}
		curSignalInfo.setCurEntry(signalLogEntry);
		return signalLogEntry.fTime;
	}

	@Override
	public BigInteger getCurrentTime() throws ZamiaException {
		if (curSignalInfo == null) {
			return null;
		}
		IGSignalLogEntry signalLogEntry = curSignalInfo.getCurrentEntry();
		if (signalLogEntry == null) {
			return null;
		}
		return signalLogEntry.fTime;
	}

	@Override
	public IGStaticValue getCurrentValue() throws ZamiaException {
		if (curSignalInfo == null) {
			return null;
		}
		IGSignalLogEntry signalLogEntry = curSignalInfo.getCurrentEntry();
		if (signalLogEntry == null) {
			return null;
		}
		return signalLogEntry.fValue;
	}

	@Override
	public void dispose() {
	}
}
