package org.zamia.instgraph.sim.ref;


import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.util.PathName;
import org.zamia.util.SimpleRegexp;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stores simulation data.
 * <p/>
 * All signals' values are stored in fAllSignals.
 * Traced signals' values (the whole history) are stored in SignalLog-s (fTracedSignals)
 *
 * @author Anton Chepurov
 */
public class SimData {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private final HashMap<PathName, IGSignalLog> fTracedSignals;

	private final HashMap<PathName, IGSignalDriver> fAllSignals;

	/**
	 *  Drivers indexed by listening process
	 */
	private final HashMap<IGSimProcess, Set<IGSignalDriver> > fSignalDrivers;

	private HashMap<PathName, IGContainer> fContainers;
	/**
	 * Signals which had transitions (at the moment of forcing) before specified signal was forced a new value
	 */
	private Set<IGSignalLog> fActiveSignals;

	private IGSimRef fSimRef;

	public SimData(IGSimRef aSimRef) {
		fSimRef = aSimRef;
		fTracedSignals = new HashMap<PathName, IGSignalLog>();
		fAllSignals = new HashMap<PathName, IGSignalDriver>();
		fSignalDrivers = new HashMap<IGSimProcess, Set<IGSignalDriver>>();
		fContainers = new HashMap<PathName, IGContainer>();
	}

	public void registerDriver(IGSignalDriver aSignalDriver) {
		fAllSignals.put(aSignalDriver.getPath(), aSignalDriver);
	}

	public IGSignalDriver getDriver(PathName aSignalName) {
		return fAllSignals.get(aSignalName);
	}

	public void logChanges(List<IGSignalChange> aChangeList, BigInteger aTime) throws ZamiaException {
		for (IGSignalChange signalChange : aChangeList) {
			addSignalValue(signalChange.getName(), aTime, signalChange.getValue(), signalChange.isEvent());
		}
	}

	/**
	 * Trace listener was added to monitor events for purposes not limited to trace logging.
	 * This was particularly necessary to perform signal comparisons on clk events.
	 * */
	public static interface TraceAddListener {
		public void trace(PathName aSignalName, BigInteger aTime, IGStaticValue aValue, boolean aIsEvent);
	}
	
	public Collection<TraceAddListener> traceAddListeners = new ArrayList<>();
	
	public void addSignalValue(PathName aSignalName, BigInteger aTime, IGStaticValue aValue, boolean aIsEvent) throws ZamiaException {
		for (TraceAddListener l : traceAddListeners)
			l.trace(aSignalName, aTime, aValue, aIsEvent);
		
		IGSignalLog log = fTracedSignals.get(aSignalName);
		if (log != null) {
			log.add(aTime, aValue, aIsEvent);
		}
	}

	public void reset() {
		for (IGSignalLog log : fTracedSignals.values()) {
			log.flush();
		}
	}

	public Set<PathName> getTracedSignals() {
		return fTracedSignals.keySet();
	}

	public void trace(PathName aSignalName, BigInteger aCurrentTime) throws ZamiaException {

		if (fTracedSignals.containsKey(aSignalName)) {
			return;
		}

		IGSignalLog log = new IGSignalLog(aSignalName);

		IGStaticValue value = fAllSignals.get(aSignalName).getValue(null);

		log.add(aCurrentTime, value, true);

		fTracedSignals.put(aSignalName, log);
	}

	public IGSignalLog traceForcedly(PathName aSignalName) throws ZamiaException {
		// start tracing AND add the current value to SignalInfo
		trace(aSignalName, getEndTime()); // getEndTime() => take the value from the latest cycle where it is still available
		// fill the leading 'U'
		IGSignalLog log = getLog(aSignalName);
		log.fillLeadingU();

		return log;
	}

	public void untrace(PathName aSignalName) throws ZamiaException {
		fTracedSignals.remove(aSignalName);
	}

	public BigInteger getEndTime() {
		return fSimRef.getEndTime();
	}

	public IGSignalLog getLog(PathName aSignalName) throws ZamiaException {
		IGSignalLog log = fTracedSignals.get(aSignalName);
		if (log == null) {
			logger.debug("SimData: Trying to obtain simulation info for a signal not being traced: %s", aSignalName);
		}
		return log;
	}

	public List<PathName> findSignalPaths(String aSearchString, int aLimit) {

		ArrayList<PathName> res = new ArrayList<PathName>();

		String regex = SimpleRegexp.convert(aSearchString);
		Pattern p = Pattern.compile(regex);

		Set<PathName> allSignalPaths = fAllSignals.keySet();
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

		IGSignalLog log = fTracedSignals.get(aSignalName);

		if (log == null) {
			return false;
		}

		IGSignalLogEntry lastEntry = log.getLastEntry();

		return lastEntry != null && lastEntry.fTime.equals(getEndTime());

	}

	void storeActiveSignals(PathName aSignalName) {

		fActiveSignals = new HashSet<IGSignalLog>();

		for (IGSignalLog log : fTracedSignals.values()) {

			if (log == null || log.getPath().equals(aSignalName)) continue;

			IGSignalLogEntry lastEntry = log.getLastEntry();

			if (lastEntry != null && lastEntry.fTime.equals(getEndTime())) {
				fActiveSignals.add(log);
			}
		}
	}

	/**
	 * Clears multiple transitions from the end of SignalInfo.
	 *
	 * @param aSignalName   the signal whose trace to repair
	 * @param aHadChangeNow if the signal had a transition at this moment before forcing
	 * @throws ZamiaException if comparing IGStaticValues fails
	 */
	void repairTrace(PathName aSignalName, boolean aHadChangeNow) throws ZamiaException {

		IGSignalLog log = fTracedSignals.get(aSignalName);

		if (log == null) {
			return;
		}

		IGSignalLogEntry forcedEntry = log.removeLastEntry();
		if (aHadChangeNow) {
			log.removeLastEntry();
		}

		IGSignalLogEntry precedingEntry = log.getLastEntry();
		if (precedingEntry == null || isValueChanged(precedingEntry.fValue, forcedEntry.fValue)) {
			log.add(forcedEntry.fTime, forcedEntry.fValue, true);
		}
	}

	private static boolean isValueChanged(IGStaticValue aOldValue, IGStaticValue aNewValue) throws ZamiaException {
		return aNewValue != null && !aNewValue.equalsValue(aOldValue);
	}

	/**
	 * Removes overwritten value (entry) from SignalInfos affected by other signals' value forcing
	 * @throws org.zamia.ZamiaException if cannot compare values to determine event status
	 */
	void repairActiveSignals() throws ZamiaException {

		for (IGSignalLog activeLog : fActiveSignals) {

			IGSignalLogEntry potForcedEntry = activeLog.getLastEntry(); // potentially forced value

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
					logger.debug("SimData: while repairing SignalInfos, twice overwritten value met in SignalInfo of %s", activeLog.getPath());
				}

				activeLog.removeLastEntry(); // remove forced
				activeLog.removeLastEntry(); // remove overwritten
				activeLog.add(potForcedEntry.fTime, potForcedEntry.fValue, true); // re-add forced
			}
		}
	}

	public void registerContainer(PathName aSignalPath, IGContainer aContainer) {
		fContainers.put(aSignalPath, aContainer);
	}

	public IGContainer getContainer(PathName aSignalPath) {
		return fContainers.get(aSignalPath);
	}

	public void addListener(IGSimProcess aProcess, IGSignalDriver aDriver) {
		Set<IGSignalDriver> drivers = fSignalDrivers.get(aProcess);
		if (drivers == null) {
			drivers = new HashSet<IGSignalDriver>();
			fSignalDrivers.put(aProcess, drivers);
		}
		drivers.add(aDriver);

		aDriver.addListener(aProcess);
	}

	public void removeListener(IGSimProcess aProcess) {
		Set<IGSignalDriver> drivers = fSignalDrivers.remove(aProcess);
		if (drivers != null) {
			for (IGSignalDriver driver : drivers) {
				driver.removeListener(aProcess);
			}
		}
	}

	public void invalidateEvents() throws ZamiaException {
		for (IGSignalDriver driver : fAllSignals.values()) {
			driver.resetEvent();
		}
	}
}
