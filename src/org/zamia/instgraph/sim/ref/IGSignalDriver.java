package org.zamia.instgraph.sim.ref;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.instgraph.interpreter.IGObjectDriver;
import org.zamia.util.PathName;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Anton Chepurov
 */
public class IGSignalDriver extends IGObjectDriver {

	private final static ZamiaLogger LOGGER = ZamiaLogger.getInstance();

	private PathName fPath;

	private IGStaticValue fNextValue;

	private SourceLocation fNextValueLocation;

	private IGStaticValue fLastValue;
	/**
	 * {@link #isEvent()} can be accessed during the whole delta cycle.
	 * We therefore need to retain the value of <tt>isEvent()</tt> for the whole
	 * delta cycle, i.e. from when the value is set and until it's released
	 * (manually).<p><p>
	 * Value is set when setting signal's value ({@link #drive()}).<p>
	 * Value is released manually at the end of each delta cycle.
	 * {@link org.zamia.instgraph.sim.ref.IGSignalDriver} knows nothing about
	 * delta cycles, so we really need to invalidate <tt>isEvent()</tt>
	 * manually, because <tt>isEvent()</tt> is only valid for 1 delta cycle.<p><p>
	 * <b>NB!</b><p>
	 * Clients must be aware that <tt>isEvent()</tt> value becomes valid only
	 * after <tt>drive()</tt> is called. This is done for (a small) performance
	 * gain. For a more robust alternative implementation see <i>Implementation
	 * details</i> section below.<p><p>
	 * <b>Implementation details</b><p>
	 * Alternatively, <tt>isEvent()</tt> value can be set from inside of
	 * {@link #setNextValue(org.zamia.instgraph.IGStaticValue, org.zamia.SourceLocation, IGSimProcess)}
	 * using {@link #updateEvent()}. In this case value will be updated with
	 * each new {@link #fNextValue} and will always be valid. This alternative,
	 * however, costs 2 extra value settings.
	 */
	private boolean fIsEvent;

	private ArrayList<IGSignalChangeRequest> fSchedule = new ArrayList<IGSignalChangeRequest>();

	private HashSet<IGSimProcess> fListeners = new HashSet<IGSimProcess>();

	private HashSet<IGSignalDriver> fSignalListeners;
	/**
	 * Here we store the parts of the signal (may easily be the whole signal) to which an assignment is made
	 * during a delta-cycle. These parts are further merged into a single {@link IGSignalChangeRequest} when processing
	 * delta in {@link IGSimRef#processDelta(IGRequestList)}. <b>Must be cleared after each delta-cycle/merging!</b>
	 */
	private HashMap<IGSimProcess, List<IGSignalDriver>> fToBeMerged = new HashMap<IGSimProcess, List<IGSignalDriver>>();

	public IGSignalDriver(String aId, IGObject.OIDir aDir, IGObject.IGObjectCat aCat, IGTypeStatic aType, boolean aIsLightweight, SourceLocation aLocation) throws ZamiaException {
		this(aId, aDir, aCat, null, aType, aLocation, aIsLightweight);
	}

	private IGSignalDriver(String aId, IGObject.OIDir aDir, IGObject.IGObjectCat aCat, IGObjectDriver aParent, IGTypeStatic aType, SourceLocation aLocation, boolean aIsLightweight) throws ZamiaException {
		super(aId, aDir, aCat, aParent, aType, aLocation, aIsLightweight);
		initSignalListeners();
	}

	private void initSignalListeners() {
		if (fSignalListeners == null) {
			fSignalListeners = new HashSet<IGSignalDriver>();
		}
	}

	@Override
	protected IGObjectDriver createChildDriver(String aId, IGObject.OIDir aDir, IGObject.IGObjectCat aCat, IGObjectDriver aParent, IGTypeStatic aType, SourceLocation aLocation, boolean aIsLightweight) throws ZamiaException {

		IGSignalDriver childDriver = new IGSignalDriver(aId, aDir, aCat, aParent, aType, aLocation, aIsLightweight);

		if (aParent != null && aParent instanceof IGSignalDriver) {

			IGSignalDriver parentDriver = (IGSignalDriver) aParent;

			parentDriver.initSignalListeners();

			listenToEachOther(parentDriver, childDriver);
		}
		return childDriver;
	}

	@Override
	protected boolean isEventInternal() {
		return fIsEvent;
	}

	static boolean hasValueChanged(IGStaticValue aOldValue, IGStaticValue aNewValue) throws ZamiaException {

		if (aOldValue == null) {
			return aNewValue != null;
		}

		return aNewValue == null || !aNewValue.equalsValue(aOldValue);
	}

	@Override
	public boolean isActive() {
		return fNextValue != null;
	}

	public void setNextValue(IGStaticValue aNextValue, SourceLocation aLocation, IGSimProcess aProcess) throws ZamiaException {

		fNextValue = aNextValue;
		fNextValueLocation = aLocation;

		scheduleForMerge(aProcess);
	}

	private void scheduleForMerge(IGSimProcess aProcess) throws ZamiaException {

		IGSignalDriver targetDriver = getTargetSignalDriver();

		List<IGSignalDriver> processDrivers = targetDriver.fToBeMerged.get(aProcess);
		if (processDrivers == null) {
			processDrivers = new LinkedList<IGSignalDriver>();
			targetDriver.fToBeMerged.put(aProcess, processDrivers);
		}

		processDrivers.add(this);
	}

	/**
	 * Merge all drivers of the underlying signal into a single driver and return it.
	 * If merge happens, all other drivers become inactive after this method call.
	 *
	 * <p/>
	 *
	 * Merge is a 2-step process:
	 * <br/> - drivers are merged processwise (within each process individually),
	 * <br/> - values of processwise merge are merged using resolution function.
	 *
	 * @throws ZamiaException
	 */
	public void mergeDrivers() throws ZamiaException {

		IGSignalDriver targetDriver = getTargetSignalDriver();

		if (targetDriver.isMergeRequired()) {

			IGStaticValue lastValue = targetDriver.getValue(null);

			DriversResolver driversResolver = targetDriver.mergeDriversProcesswise();

			targetDriver.resolveDrivers(driversResolver);

			//todo: if some part of the target is not set by drivers from fToBeMerged, this part must be restored from backupTarget into resolvedValue. ??? not needed anymore?

			IGStaticValue mergedValue = targetDriver.getValue(null);

			targetDriver.setValue(lastValue, null);	// restore broken last value
			targetDriver.setValue(mergedValue, null);

			targetDriver.resetEvent();

			if (targetDriver.isActive()) {
				throw new ZamiaException("IGSimRef: internal error: target driver remains active after merge: " + targetDriver.getIdInternal());
			}
		}

		targetDriver.fToBeMerged = new HashMap<IGSimProcess, List<IGSignalDriver>>();

		targetDriver.clearWasActive();
	}

	private void resolveDrivers(DriversResolver driversResolver) throws ZamiaException {

		if (getTargetDriver() != this) {
			throw new ZamiaException("IGSimRef: IGSignalDriver: merge(): only target driver should be merged");
		}

		IGTypeStatic type = getCurrentType();

		if (type.isArray()) {

			for (IGObjectDriver driver : getArrayElementDrivers()) {

				driversResolver.resolve(driver);
			}
		} else if (type.isRecord()) {

			for (IGObjectDriver driver : getRecordFieldDrivers()) {

				driversResolver.resolve(driver);
			}
		} else {

			driversResolver.resolve(this);
		}
	}

	/**
	 * @return 	if there are at least 2 processes which drive this signal or at least 2 drivers inside a single process
	 */
	private boolean isMergeRequired() {
		return fToBeMerged.size() > 1 || fToBeMerged.size() == 1 && fToBeMerged.values().iterator().next().size() > 1;
	}

	private DriversResolver mergeDriversProcesswise() throws ZamiaException {

		if (getTargetDriver() != this) {
			throw new ZamiaException("IGSimRef: IGSignalDriver: merge(): only target driver should be merged");
		}

		DriversResolver driversResolver = new DriversResolver(fToBeMerged.size());

		IGStaticValue currentValue = getValue(null);

		for (List<IGSignalDriver> processDrivers : fToBeMerged.values()) {

			setValue(currentValue, null);
			clearWasActive();

			for (IGSignalDriver driver : processDrivers) {
				driver.drive(); // note that driver becomes inactive after this call
			}

			driversResolver.add(getActiveElements());
		}
		return driversResolver;
	}

	private static class DriversResolver {

		private Collection<HashMap<IGObjectDriver, IGStaticValue>> activeElements;

		public DriversResolver(int size) {
			activeElements = new ArrayList<HashMap<IGObjectDriver, IGStaticValue>>(size);
		}

		public void add(HashMap<IGObjectDriver, IGStaticValue> activeElements) {
			this.activeElements.add(activeElements);
		}

		public void resolve(IGObjectDriver driver) throws ZamiaException {

			ArrayList<IGStaticValue> values = new ArrayList<IGStaticValue>();

			for (HashMap<IGObjectDriver, IGStaticValue> activeElement : activeElements) {
				if (activeElement.containsKey(driver)) {
					values.add(activeElement.get(driver));
				}
			}

			IGStaticValue mergedValue;

			if (values.isEmpty()) {

				return;

			} else if (values.size() == 1) {

				mergedValue = values.get(0);

			} else {

				if (!driver.getCurrentType().getId().equals("STD_LOGIC")) {
					throw new ZamiaException("Nonresolved signal '" + driver.getId() + "' has multiple sources");
				}

				mergedValue = IGStaticValue.resolveStdLogic(values);
			}

			driver.setValue(mergedValue, null);
		}
	}

	private IGSignalDriver getTargetSignalDriver() throws ZamiaException {
		return cast(getTargetDriver());
	}

	private IGSignalDriver cast(IGObjectDriver aIgObjectDriver) throws ZamiaException {
		if (aIgObjectDriver instanceof IGSignalDriver) {
			return (IGSignalDriver) aIgObjectDriver;
		}
		throw new ZamiaException("IGSignalDriver: internal error --- SIGNAL driver expected, actual is "
				+ aIgObjectDriver.getClass().getSimpleName() + ": " + aIgObjectDriver);
	}

	public IGStaticValue getNextValue() throws ZamiaException {
		return fNextValue;
	}

	@Override
	protected void setValueInternal(IGStaticValue aValue, SourceLocation aLocation) throws ZamiaException {

		IGStaticValue oldValue = getValue(aLocation);

		super.setValueInternal(aValue, aLocation);

		updateEvent(oldValue, aValue);

		if (fIsEvent) {
			if (oldValue == null) {
				oldValue = aValue; // "otherwise, S'LAST_VALUE returns the current value of S" (std.)
			}
			fLastValue = oldValue;
		}
	}

	@Override
	protected void updateValue(SourceLocation aLocation) throws ZamiaException {

		IGStaticValue oldValue = getValue(aLocation);

		super.updateValue(aLocation);

		IGStaticValue newValue = getValue(aLocation);

		updateEvent(oldValue, newValue);
	}

	private void updateEvent(IGStaticValue aOldValue, IGStaticValue aNewValue) throws ZamiaException {
		/* logical 'OR' covers the case when the signal is updated multiple times during a single delta cycle,
		* including the case when different bits of a signal are updated (the whole signal (fParent) gets then
		* updated multiple times)*/
		fIsEvent = fIsEvent || hasValueChanged(aOldValue, aNewValue);
	}

	public void drive() throws ZamiaException {

		if (fNextValue != null) {

			setValue(fNextValue, fNextValueLocation);

			if (IGSimRef.DEBUG) {
				LOGGER.debug("IGSimRef: IGSignalDriver: drive(): setting %s to %s", getIdInternal(), fNextValue);
			}

			fNextValue = null;
			fNextValueLocation = null;

		}
	}

	public IGStaticValue getLastValue() {
		return fLastValue;
	}

	public void scheduleChange(boolean aInertial, BigInteger aReject, IGSignalChangeRequest aReq, BigInteger aSimTime) throws ZamiaException {
		// cleanup / update schedule

		BigInteger reqT = aReq.getTime();

		int i = 0;
		while (i < fSchedule.size()) {

			IGSignalChangeRequest scr = fSchedule.get(i);

			BigInteger t = scr.getTime();

			// delete outdated entries
			if (t.compareTo(aSimTime) < 0) {
				fSchedule.remove(i);
				continue;
			}

			// delete later requests

			if (t.compareTo(reqT) > 0) {
				scr.setCanceled(true);
			}

			// if inertial, delete events in the reject time period

			if (aInertial) {
				if (t.compareTo(aReject) > 0) {
					scr.setCanceled(true);
				}
			}

			i++;
		}
		fSchedule.add(aReq);
	}

	@Override
	public void map(IGObjectDriver aActual, SourceLocation aLocation) throws ZamiaException {
		super.map(aActual, aLocation);

		listenToEachOther(this, aActual);
//		if (aActual instanceof IGSignalDriver) {
//			fSignalListeners.add((IGSignalDriver) aActual);
//		}

//		if (isReceiver()) {
//			if (aActual instanceof IGSignalDriver) {
//				IGSignalDriver actualDriver = (IGSignalDriver) aActual;
//
//				actualDriver.fSignalListeners.add(this);
//			}
//		}
	}

	private static void listenToEachOther(IGSignalDriver aFirst, IGObjectDriver aSecond) {

		if (aSecond instanceof IGSignalDriver) {

			IGSignalDriver second = (IGSignalDriver) aSecond;

			aFirst.listenTo(second);
			second.listenTo(aFirst);
		}
	}

	private void listenTo(IGSignalDriver aSignalDriver) {
		fSignalListeners.add(aSignalDriver);
	}

//	private boolean isReceiver() {
//		return fDir == IGObject.OIDir.IN || fDir == IGObject.OIDir.INOUT;
//	}

//	private boolean isTransmitter() {
//		return fDir == IGObject.OIDir.OUT || fDir == IGObject.OIDir.INOUT;
//	}

	public void addListener(IGSimProcess aProcess) {
		fListeners.add(aProcess);
	}

	public void removeListener(IGSimProcess aProcess) {
		fListeners.remove(aProcess);
	}


	public HashSet<IGSimProcess> getListeningProcesses() throws ZamiaException {

		HashSet<IGSimProcess> uniqueListeners = new HashSet<IGSimProcess>();

		collectUniqueListeners(new HashSet<IGSignalDriver>(), uniqueListeners);

		if (IGSimRef.DEBUG) {
			LOGGER.debug("IGSimRef: getListeningProcesses(): collected %d unique listeners", uniqueListeners.size());
		}

		return uniqueListeners;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <D extends IGObjectDriver, R extends IGInterpreterRuntimeEnv>
	void collectUniqueListeners(Collection<D> aVisitedDrivers, Collection<R> aUniqueListeners) throws ZamiaException {

		super.collectUniqueListeners(aVisitedDrivers, aUniqueListeners);

		if (aVisitedDrivers.contains(this)) {
			return;
		}

		aUniqueListeners.addAll((Collection<? extends R>) fListeners);

		aVisitedDrivers.add((D) this); // avoid notification mirroring

//		if (isTransmitter()) {
//			IGSignalDriver mappedTo = getMappedTo();
//			if (mappedTo != null) {
//				mappedTo.collectUniqueListeners(aVisitedDrivers, aUniqueListeners);
//			}
//		}

		for (IGSignalDriver signalListener : fSignalListeners) {
			signalListener.collectUniqueListeners(aVisitedDrivers, aUniqueListeners);
		}
	}

	/**
	 * Method can be used to update {@link #fIsEvent} from inside of {@link #setNextValue(org.zamia.instgraph.IGStaticValue, org.zamia.SourceLocation, IGSimProcess)}
	 *
	 * @throws ZamiaException if driver cannot retrieve/set value
	 */
	private void updateEvent() throws ZamiaException {
		if (!isActive()) {
			return;
		}

		IGStaticValue currentValue = getValue(null);

		setValue(fNextValue, fNextValueLocation);

		setValue(currentValue, null);

		// value preserved, fIsEvent updated
	}

	public void resetEvent() throws ZamiaException {

		if (!isEvent()) {
			return;
		}
		if (IGSimRef.DEBUG) {
			LOGGER.debug("Resetting event on driver %s.", this);
		}

		resetEventP();

	}

	protected void resetEventInternal() throws ZamiaException {

		super.resetEventInternal();

		clearReset();
	}

	@Override
	protected void clearReset() {
		fIsEvent = false;
	}

//	private IGSignalDriver getMappedTo() throws ZamiaException {
//		IGObjectDriver mappedTo = fMappedTo;
//		if (mappedTo != null) {
//			return cast(mappedTo);
//		}
//		return null;
//	}


	public void setPath(PathName fPath) {
		this.fPath = fPath;

		if (debug) {
			for (IGObjectDriver driver : getArrayElementDrivers()) {
				((IGSignalDriver) driver).fPath = fPath;
			}
			for (IGObjectDriver driver : getRecordFieldDrivers()) {
				((IGSignalDriver) driver).fPath = fPath;
			}
		}
	}

	public PathName getPath() {
		return fPath;
	}

	@Override
	public String toString() {
		if (isActive()) {
			return super.toString() + "=>" + fNextValue;
		}
		return super.toString();
	}

	public void collectChanges(List<IGSignalChange> aMappedChanges, Set<PathName> aTracedSignals) throws ZamiaException {

		HashSet<IGSignalDriver> visitedDrivers = new HashSet<IGSignalDriver>();

		visitedDrivers.add(this);

		for (IGSignalDriver mappedDriver : fSignalListeners) {
			mappedDriver.collectChangesInternal(aMappedChanges, visitedDrivers, aTracedSignals);
		}
	}

	private void collectChangesInternal(List<IGSignalChange> aMappedChanges, HashSet<IGSignalDriver> aVisitedDrivers, Set<PathName> aTracedSignals) throws ZamiaException {

		if (aVisitedDrivers.contains(this)) {
			return;
		}

		if (aTracedSignals.contains(fPath)) {
			aMappedChanges.add(createSignalChange());
		}

		aVisitedDrivers.add(this);

		for (IGSignalDriver mappedDriver : fSignalListeners) {
			mappedDriver.collectChangesInternal(aMappedChanges, aVisitedDrivers, aTracedSignals);
		}
	}

	public IGSignalChange createSignalChange() throws ZamiaException {
		return new IGSignalChange(getValue(null), isEvent(), this);
	}
}
