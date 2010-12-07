package org.zamia.instgraph.sim.ref;


import org.zamia.ZamiaException;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.sim.IGISimCursor;
import org.zamia.util.PathName;

import java.math.BigInteger;

/**
 * @author Anton Chepurov
 */
public class RefSimCursor implements IGISimCursor {

	private final SimData fData;

	private IGSignalLog fCurLog;

	public RefSimCursor(SimData aData) {
		fData = aData;
	}

	@Override
	public boolean gotoTransition(PathName aSignalName, BigInteger aTimeOffset) throws ZamiaException {
		fCurLog = fData.getLog(aSignalName);
		if (fCurLog == null) {
			/* If signal is not being traced, then start tracing it forcedly */
			fCurLog = fData.traceForcedly(aSignalName);
		}
		IGSignalLogEntry transactionEntry = fCurLog.getTransactionEntry(aTimeOffset);
		fCurLog.setCurEntry(transactionEntry);
		return transactionEntry != null;
	}

	@Override
	public BigInteger gotoNextTransition(BigInteger aTimeLimit) throws ZamiaException {
		if (fCurLog == null) {
			return null;
		}
		IGSignalLogEntry signalLogEntry = fCurLog.getNextEventEntry();
		if (signalLogEntry == null) {
			return fData.getEndTime();
		}
		fCurLog.setCurEntry(signalLogEntry);
		return signalLogEntry.fTime;
	}

	@Override
	public BigInteger gotoPreviousTransition(BigInteger aTimeLimit) throws ZamiaException {
		if (fCurLog == null) {
			return null;
		}
		IGSignalLogEntry signalLogEntry = fCurLog.getPrevEventEntry();
		if (signalLogEntry == null) {
			return null;
		}
		fCurLog.setCurEntry(signalLogEntry);
		return signalLogEntry.fTime;
	}

	@Override
	public BigInteger getCurrentTime() throws ZamiaException {
		if (fCurLog == null) {
			return null;
		}
		IGSignalLogEntry signalLogEntry = fCurLog.getCurrentEntry();
		if (signalLogEntry == null) {
			return null;
		}
		return signalLogEntry.fTime;
	}

	@Override
	public IGStaticValue getCurrentValue() throws ZamiaException {
		if (fCurLog == null) {
			return null;
		}
		IGSignalLogEntry signalLogEntry = fCurLog.getCurrentEntry();
		if (signalLogEntry == null) {
			return null;
		}
		return signalLogEntry.fValue;
	}

	@Override
	public void dispose() {
	}
}
