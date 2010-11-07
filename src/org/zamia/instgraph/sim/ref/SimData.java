package org.zamia.instgraph.sim.ref;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.math.BigInteger;

import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.util.PathName;
import org.zamia.util.SimpleRegexp;

/**
 * Stores simulation data.
 *
 * All signals' values are stored in contexts, which are mapped to signal paths (fSignalContextBySignalPath).
 * Traced signals' values (the whole history) are stored in SignalInfo-s (fTracedInfoBySignal)
 *
 * @author Anton Chepurov
 */
public class SimData {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private HashMap<PathName, IGSignalContext> fSignalContextBySignalPath;

	private final HashMap<Long, IGSignalInfo> fTracedInfoBySignal;

	private final HashMap<PathName, Long> fSignalByPath;

	private HashMap<PathName, IGContainer> fContainerBySignalPath;
	/**
	 * Signals which had transitions (at the moment of forcing) before specified signal was forced a new value
	 */
	private Set<IGSignalInfo> fActiveSignals;

	private IGSimRef fSimRef;

	public SimData(IGSimRef aSimRef) {
		fSimRef = aSimRef;
		fSignalContextBySignalPath = new HashMap<PathName, IGSignalContext>();
		fTracedInfoBySignal = new HashMap<Long, IGSignalInfo>();
		fSignalByPath = new HashMap<PathName, Long>();
		fContainerBySignalPath = new HashMap<PathName, IGContainer>();
	}

	public void registerContext(PathName signalPath, IGSignalContext signalContext) {
		// register context
		fSignalContextBySignalPath.put(signalPath, signalContext);
		// map paths to DBIDs
		fSignalByPath.put(signalPath, signalContext.getSignal());
	}

	public void logChanges(List<IGSignalChange> aChangeList, BigInteger aCurrentTime) {
		for (IGSignalChange signalChange : aChangeList) {
			IGSignalInfo si = fTracedInfoBySignal.get(signalChange.getSignal());
			if (si != null) {
				si.add(aCurrentTime, signalChange.getValue(), signalChange.isEvent());
			}

		}
	}

	public void addSignalValue(long aSignalDBID, BigInteger aTime, IGStaticValue aValue, boolean aIsEvent) {
		IGSignalInfo si = fTracedInfoBySignal.get(aSignalDBID);
		if (si != null) {
			si.add(aTime, aValue, aIsEvent);
		}
	}

	public void reset() {
		for (IGSignalInfo si : fTracedInfoBySignal.values()) {
			si.flush();
		}
		fSignalContextBySignalPath = new HashMap<PathName, IGSignalContext>();
	}


	public void trace(PathName aSignalName, BigInteger aCurrentTime) throws ZamiaException {
		IGSignalContext sigContext = getSignalContext(aSignalName);

		Long sigDBID = sigContext.getSignal();

		if (fTracedInfoBySignal.containsKey(sigDBID)) {
			return;
		}

		IGSignalInfo signalInfo = new IGSignalInfo(aSignalName);

		fTracedInfoBySignal.put(sigDBID, signalInfo);

		signalInfo.add(aCurrentTime, sigContext.getValue(), true);

	}

	public IGSignalInfo traceForcedly(PathName aSignalName) throws ZamiaException {
		// start tracing AND add the current value to SignalInfo
		trace(aSignalName, getEndTime()); // getEndTime() => take the value from the latest cycle where it is still available
		// fill the leading 'U'
		IGSignalInfo signalInfo = getSignalInfo(aSignalName);
		signalInfo.fillLeadingU();

		return signalInfo;
	}

	public void untrace(PathName aSignalName) throws ZamiaException {
		long sigDBID = getSignal(aSignalName);
		fTracedInfoBySignal.remove(sigDBID);
	}

	public BigInteger getEndTime() {
		return fSimRef.getEndTime();
	}

	private IGSignalContext getSignalContext(PathName aSignalName) throws ZamiaException {
		IGSignalContext sigContext = fSignalContextBySignalPath.get(aSignalName);

		if (sigContext == null) {
			String className = getClass().getSimpleName();
			throw new ZamiaException(className + ": Simulator not started yet (" + className + ".getSignalContext(PathName))");
		}
		return sigContext;
	}

	public IGSignalInfo getSignalInfo(PathName aSignalName) throws ZamiaException {
		long dbid = getSignal(aSignalName);
		IGSignalInfo signalInfo = fTracedInfoBySignal.get(dbid);
		if (signalInfo == null) {
			logger.debug("SimData: Trying to obtain simulation info for a signal not being traced: %s", aSignalName);
		}
		return signalInfo;
	}

	public long getSignal(PathName aSignalName) {
		return fSignalByPath.containsKey(aSignalName) ? fSignalByPath.get(aSignalName) : 0;
	}

	public List<PathName> findSignalPaths(String aSearchString, int aLimit) {

		ArrayList<PathName> res = new ArrayList<PathName>();

		String regex = SimpleRegexp.convert(aSearchString);
		Pattern p = Pattern.compile(regex);

		Set<PathName> allSignalPaths = fSignalByPath.keySet();
		for (PathName signalPath : allSignalPaths) {
			Matcher matcher = p.matcher(signalPath.toString());
			if (matcher.matches()) {
				res.add(signalPath);
				if (res.size() > aLimit) {
					return res;
				}
			}
		}
		return res;
	}

	boolean hasChangeNow(PathName aSignalName) {

		Long dbid = fSignalByPath.get(aSignalName);

		IGSignalInfo sigInfo = fTracedInfoBySignal.get(dbid);

		if (sigInfo == null) {
			return false;
		}

		IGSignalLogEntry lastEntry = sigInfo.getLastEntry();

		return lastEntry != null && lastEntry.fTime.equals(getEndTime());

	}

	void storeActiveSignals(PathName aSignalName) {

		fActiveSignals = new HashSet<IGSignalInfo>();

		for (java.util.Map.Entry<Long, IGSignalInfo> entry : fTracedInfoBySignal.entrySet()) {
			IGSignalInfo signalInfo = entry.getValue();

			if (signalInfo == null || signalInfo.getPath().equals(aSignalName)) continue;

			IGSignalLogEntry lastEntry = signalInfo.getLastEntry();

			if (lastEntry != null && lastEntry.fTime.equals(getEndTime())) {
				fActiveSignals.add(signalInfo);
			}
		}
	}

	/**
	 * Clears multiple transitions from the end of SignalInfo.
	 *
	 * @param aSignalName the signal whose trace to repair
	 * @param aHadChangeNow if the signal had a transition at this moment before forcing
	 * @throws ZamiaException if comparing IGStaticValues fails
	 */
	void repairTrace(PathName aSignalName, boolean aHadChangeNow) throws ZamiaException {

		Long dbid = fSignalByPath.get(aSignalName);

		IGSignalInfo sigInfo = fTracedInfoBySignal.get(dbid);

		if (sigInfo == null) {
			return;
		}

		IGSignalLogEntry forcedEntry = sigInfo.removeLastEntry();
		if (aHadChangeNow) {
			sigInfo.removeLastEntry();
		}

		IGSignalLogEntry precedingEntry = sigInfo.getLastEntry();
		if (precedingEntry == null || IGSignalWaveformGen.isChanged(precedingEntry.fValue, forcedEntry.fValue)) {
			sigInfo.add(forcedEntry.fTime, forcedEntry.fValue, true);
		}
	}

	/**
	 * Removes overwritten value (entry) from SignalInfos affected by other signals' value forcing
	 */
	void repairActiveSignals() {

		for (IGSignalInfo activeSignalInfo : fActiveSignals) {

			IGSignalLogEntry potForcedEntry = activeSignalInfo.getLastEntry(); // potentially forced value

			IGSignalLogEntry potOverwrittenEntry = potForcedEntry.fPrev; // potentially overwritten value
			if (potOverwrittenEntry == null) {
				// no new entry has been added during forcing, since
				// potForcedEntry was already present before the forcing
				return;
			}

			if (potForcedEntry.fTime.equals(potOverwrittenEntry.fTime)) {
				// value was overwritten

				IGSignalLogEntry prevEntry = potOverwrittenEntry.fPrev;
				if (prevEntry != null && prevEntry.fTime.equals(potOverwrittenEntry.fTime)) {
					logger.debug("SimData: while repairing SignalInfos, twice overwritten value met in SignalInfo of %s", activeSignalInfo.getPath());
				}

				activeSignalInfo.removeLastEntry(); // remove forced
				activeSignalInfo.removeLastEntry(); // remove overwritten
				activeSignalInfo.add(potForcedEntry.fTime, potForcedEntry.fValue, true); // re-add forced
			}
		}
	}

	public void registerContainer(PathName aSignalPath, IGContainer aContainer) {
		fContainerBySignalPath.put(aSignalPath, aContainer);
	}

	public IGContainer getContainer(PathName aSignalPath) {
		return fContainerBySignalPath.get(aSignalPath);
	}
}
