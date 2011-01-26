package org.zamia.instgraph.sim.ref;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.interpreter.IGObjectDriver;
import org.zamia.util.PathName;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
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
	 * {@link #setNextValue(org.zamia.instgraph.IGStaticValue, org.zamia.SourceLocation)}
	 * using {@link #updateEvent()}. In this case value will be updated with
	 * each new {@link #fNextValue} and will always be valid. This alternative,
	 * however, costs 2 extra value settings.
	 */
	private boolean fIsEvent;

	private ArrayList<IGSignalChangeRequest> fSchedule = new ArrayList<IGSignalChangeRequest>();

	private HashSet<IGSimProcess> fListeners = new HashSet<IGSimProcess>();

	private HashSet<IGSignalDriver> fSignalListeners;

	private HashSet<IGSignalDriver> fToBeMerged = new HashSet<IGSignalDriver>();

	public IGSignalDriver(String aId, IGObject.OIDir aDir, IGObject.IGObjectCat aCat, IGObjectDriver aParent, IGTypeStatic aType, SourceLocation aLocation) throws ZamiaException {
		super(aId, aDir, aCat, aParent, aType, aLocation);
		initSignalListeners();
	}

	private void initSignalListeners() {
		if (fSignalListeners == null) {
			fSignalListeners = new HashSet<IGSignalDriver>();
		}
	}

	@Override
	protected IGObjectDriver createChildDriver(String aId, IGObject.OIDir aDir, IGObject.IGObjectCat aCat, IGObjectDriver aParent, IGTypeStatic aType, SourceLocation aLocation) throws ZamiaException {

		IGSignalDriver childDriver = new IGSignalDriver(aId, aDir, aCat, aParent, aType, aLocation);

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

	public void setNextValue(IGStaticValue aNextValue, SourceLocation aLocation) throws ZamiaException {

		fNextValue = aNextValue;
		fNextValueLocation = aLocation;

		scheduleForMerge();
	}

	private void scheduleForMerge() throws ZamiaException {

		IGSignalDriver targetDriver = getTargetSignalDriver();

		targetDriver.fToBeMerged.add(this);
	}

	public IGSignalDriver mergeDrivers(IGSimProcess aProcess) throws ZamiaException {

		IGSignalDriver targetDriver = getTargetSignalDriver();

		int numMerged = targetDriver.fToBeMerged.size();

		if (numMerged > 1) {

			IGStaticValue backupTarget = targetDriver.getValue(null);

			IGTypeStatic type = targetDriver.getCurrentType();
			type = type.computeStaticType(aProcess, ASTErrorMode.EXCEPTION, null);
			IGStaticValue defaultValue = IGStaticValue.generateZ(type, null, true);

			ArrayList<IGStaticValue> mergedValues = new ArrayList<IGStaticValue>(numMerged);
			for (IGSignalDriver driver : targetDriver.fToBeMerged) {

				targetDriver.setValue(defaultValue, null);

				driver.drive(); // note that driver becomes inactive after this call

				mergedValues.add(targetDriver.getValue(null));
			}

			IGStaticValue resolvedValue = null;
			for (IGStaticValue mergedValue : mergedValues) {
				if (resolvedValue == null) {
					resolvedValue = mergedValue;
					continue;
				}
				resolvedValue = IGStaticValue.resolveStdLogic(resolvedValue, mergedValue);
			}
			//todo: if some part of the target is not set by drivers from fToBeMerged, this part must be restored from backupTarget into resolvedValue.
			targetDriver.fNextValue = resolvedValue;

			targetDriver.setValue(backupTarget, null);

			targetDriver.resetEvent();

			targetDriver.fToBeMerged = new HashSet<IGSignalDriver>();

			return targetDriver;
		}
		return null;
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

		fIsEvent = hasValueChanged(oldValue, aValue);

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

		fIsEvent = hasValueChanged(oldValue, newValue);
	}

	public void drive() throws ZamiaException {

		if (fNextValue != null) {

			setValue(fNextValue, fNextValueLocation);

			LOGGER.debug("IGSimRef: IGSignalDriver: drive(): setting %s to %s", getId(), fNextValue);

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


	public void notifyChange() throws ZamiaException {

		HashSet<IGSimProcess> uniqueListeners = new HashSet<IGSimProcess>();

		collectUniqueListeners(new HashSet<IGSignalDriver>(), uniqueListeners);

		LOGGER.debug("IGSimRef: notifyChange(): collected %d unique listeners", uniqueListeners.size());

		for (IGSimProcess process : uniqueListeners) {
			process.resume(ASTErrorMode.EXCEPTION, null);
		}
	}

	private void collectUniqueListeners(HashSet<IGSignalDriver> aVisitedDrivers, HashSet<IGSimProcess> aUniqueListeners) throws ZamiaException {

		if (aVisitedDrivers.contains(this)) {
			return;
		}

		aUniqueListeners.addAll(fListeners);

		aVisitedDrivers.add(this); // avoid notification mirroring

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
	 * Method can be used to update {@link #fIsEvent} from inside of {@link #setNextValue(org.zamia.instgraph.IGStaticValue, org.zamia.SourceLocation)}
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

		IGStaticValue currentValue = getValue(null);

		setValue(currentValue, null);

		// value preserved, fIsEvent reset
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
