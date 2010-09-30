/*
 * Copyright 2004-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.rtl.sim;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.EventListenerList;

import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.rtl.RTLArrayCSel;
import org.zamia.rtl.RTLCE;
import org.zamia.rtl.RTLComparator;
import org.zamia.rtl.RTLGraph;
import org.zamia.rtl.RTLInputPort;
import org.zamia.rtl.RTLLiteral;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLMux;
import org.zamia.rtl.RTLOperationLogic;
import org.zamia.rtl.RTLOperationMath;
import org.zamia.rtl.RTLOutputPort;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLRegister;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.RTLTargetArray;
import org.zamia.rtl.RTLTargetCond;
import org.zamia.rtl.RTLTargetEMux;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.rtl.sim.behaviors.ArrayCSelBehavior;
import org.zamia.rtl.sim.behaviors.CEBehavior;
import org.zamia.rtl.sim.behaviors.GraphBehavior;
import org.zamia.rtl.sim.behaviors.InputPortBehavior;
import org.zamia.rtl.sim.behaviors.InterpreterBehavior;
import org.zamia.rtl.sim.behaviors.LiteralBehavior;
import org.zamia.rtl.sim.behaviors.MuxBehavior;
import org.zamia.rtl.sim.behaviors.OperationCompareBehavior;
import org.zamia.rtl.sim.behaviors.OperationLogicBehavior;
import org.zamia.rtl.sim.behaviors.OperationMathBehavior;
import org.zamia.rtl.sim.behaviors.OutputPortBehavior;
import org.zamia.rtl.sim.behaviors.RegisterBehavior;
import org.zamia.rtl.sim.behaviors.TargetArrayBehavior;
import org.zamia.rtl.sim.behaviors.TargetCondBehavior;
import org.zamia.rtl.sim.behaviors.TargetEMuxBehavior;
import org.zamia.util.HashMapArray;
import org.zamia.util.PathName;
import org.zamia.util.ZStack;
import org.zamia.zil.ZILType;
import org.zamia.zil.ZILValue;
import org.zamia.zil.interpreter.ZILInterpreter;
import org.zamia.zil.interpreter.ZILInterpreterRuntimeEnv;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class Simulator implements ISimulator {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public boolean dump = true;

	public static final int TIMEOUT = 5000;

	private ArrayList<SignalInfo> fAllSignals;

	private ArrayList<SignalChange> fChangeList;

	private HashMap<PathName, RTLSignal> fSignalMap;

	private HashMapArray<RTLSignal, SignalInfo> fSIs, fSIsInternal;

	private HashMap<RTLPort, ZILValue> fPortValues;

	private LinkedPortValueSet fDeltaPortValues;

	private HashMap<RTLSignal, ZILValue> fSignalValues;

	private SimSchedule fSimSchedule;

	private HashMap<RTLPort, ArrayList<PortChangeRequest>> fPortSchedules;

	@SuppressWarnings("unchecked")
	private HashMap<Class, IRTLModuleBehavior> fBehaviorMap;

	private RTLGraph fRoot;

	private EventListenerList fObservers = new EventListenerList();

	private long fSimulationTime = 0l;

	private SignalInfo fCurSignalInfo;

	private HashMap<RTLSignal, SignalChange> fActiveSignals;

	@SuppressWarnings("unchecked")
	public Simulator() throws ZamiaException {
		fBehaviorMap = new HashMap<Class, IRTLModuleBehavior>();

		fBehaviorMap.put(RTLInputPort.class, new InputPortBehavior());
		fBehaviorMap.put(RTLOutputPort.class, new OutputPortBehavior());
		fBehaviorMap.put(RTLLiteral.class, new LiteralBehavior());
		fBehaviorMap.put(RTLTargetCond.class, new TargetCondBehavior());
		fBehaviorMap.put(RTLOperationMath.class, new OperationMathBehavior());
		fBehaviorMap.put(RTLOperationLogic.class, new OperationLogicBehavior());
		fBehaviorMap.put(RTLComparator.class, new OperationCompareBehavior());
		fBehaviorMap.put(RTLTargetEMux.class, new TargetEMuxBehavior());
		fBehaviorMap.put(RTLRegister.class, new RegisterBehavior());
		fBehaviorMap.put(ZILInterpreter.class, new InterpreterBehavior());
		fBehaviorMap.put(RTLGraph.class, new GraphBehavior());
		fBehaviorMap.put(RTLArrayCSel.class, new ArrayCSelBehavior());
		fBehaviorMap.put(RTLTargetArray.class, new TargetArrayBehavior());
		fBehaviorMap.put(RTLMux.class, new MuxBehavior());
		fBehaviorMap.put(RTLCE.class, new CEBehavior());
	}

	/*
	 * public interface starts here
	 */

	public void open(RTLGraph aGraph) throws ZamiaException {
		fRoot = aGraph;

		fSIs = new HashMapArray<RTLSignal, SignalInfo>();
		fSIsInternal = new HashMapArray<RTLSignal, SignalInfo>();

		fSignalMap = new HashMap<PathName, RTLSignal>();

		// compute list of signals

		fAllSignals = new ArrayList<SignalInfo>();

		ZStack<RTLGraph> todo = new ZStack<RTLGraph>();
		todo.push(fRoot);

		while (!todo.isEmpty()) {

			RTLGraph graph = todo.pop();

			int n = graph.getNumSignals();

			for (int i = 0; i < n; i++) {
				RTLSignal signal = graph.getSignal(i);

				SignalInfo si = new SignalInfo();

				si.setPath(signal.getPath());
				si.setType(signal.getType());

				fAllSignals.add(si);
			}

			n = graph.getNumSubs();
			for (int i = 0; i < n; i++) {

				RTLModule sub = graph.getSub(i);

				if (sub instanceof RTLGraph) {
					todo.push((RTLGraph) sub);
				}
			}
		}

		reset();
	}

	public void open(File aFile) throws IOException, ZamiaException {
		throw new ZamiaException("Simulator: cannot read files.");
	}

	public void shutdown() {
		// GC will take care of us.
	}

	public RTLGraph getRTLGraph() {
		return fRoot;
	}

	/**
	 * reset the simulator, will clean trace logs but simulator will remember
	 * which signals/ports should be traced
	 */

	public void reset() throws ZamiaException {
		fChangeList = null;

		fPortValues = new HashMap<RTLPort, ZILValue>(1);
		fDeltaPortValues = new LinkedPortValueSet();
		fSignalValues = new HashMap<RTLSignal, ZILValue>(1);

		fSimSchedule = new SimSchedule();
		fPortSchedules = new HashMap<RTLPort, ArrayList<PortChangeRequest>>();

		fSimulationTime = 0;

		int n = fSIs.size();
		for (int i = 0; i < n; i++) {
			SignalInfo si = fSIs.get(i);
			si.flush();
		}
		n = fSIsInternal.size();
		for (int i = 0; i < n; i++) {
			SignalInfo si = fSIsInternal.get(i);
			si.flush();
		}

		notifyReset();

		fChangeList = new ArrayList<SignalChange>();

		if (fRoot != null) {
			if (dump) {
				logger.debug("Simulator: Setting initial signal values:");
			}
			// initial signal values, init modules and ports
			init(fRoot);
		}

		// process delta produced by init

		processDelta();

		computeActiveSignals();

		propagateSignalChanges();

		logChanges(fChangeList, getCurrentTime());

		// let processes initialize themselves

		simulate(0);
	}

	public void assign(int aSignalIdx, ZILValue aValue) throws ZamiaException {

		SignalInfo si = fAllSignals.get(aSignalIdx);

		PathName path = si.getPath();

		RTLSignal signal = findSignal(path);

		fChangeList = new ArrayList<SignalChange>();

		int n = signal.getNumConns();
		for (int i = 0; i < n; i++) {
			RTLPort p = signal.getConn(i);

			if (p.getDirection() == PortDir.IN) {
				continue;
			}

			setDelta(p, aValue);
		}

		processDelta();

		logChanges(fChangeList, getCurrentTime());

		notifyChanges(getCurrentTime());

	}

	public long getStartTime() {
		return 0;
	}

	public long getEndTime() {
		return fSimulationTime;
	}

	public long getTimeScale() {
		return 1;
	}

	public boolean isSimulator() {
		return true;
	}

	public int getInterfaceVersion() {
		return 1;
	}

	public void getValue(long aTimeOffset, int aSignalIdx, ZILValue aRetValue, SourceLocation aSourceLocation) throws ZamiaException {

		fCurSignalInfo = fAllSignals.get(aSignalIdx);

		SignalLogEntry entry = fCurSignalInfo.getEventEntry(aTimeOffset);

		aRetValue.modifyValue(entry.fValue);
	}

	public long getNextValue(long aTimeLimit, ZILValue aRetValue, SourceLocation aSourceLocation) throws ZamiaException {
		SignalLogEntry entry = fCurSignalInfo.getNextEventEntry();

		long time;

		if (entry == null) {
			time = fSimulationTime;
			entry = fCurSignalInfo.getCurrentEntry();
		} else {
			time = entry.fTime;
			fCurSignalInfo.setCurEntry(entry);
		}

		aRetValue.modifyValue(entry.fValue);

		return time;
	}

	public long getPrevValue(long aTimeLimit, ZILValue aRetValue, SourceLocation aSourceLocation) throws ZamiaException {
		SignalLogEntry entry = fCurSignalInfo.getPrevEventEntry();

		long time;

		if (entry == null) {
			time = 0;
			entry = fCurSignalInfo.getCurrentEntry();
		} else {
			time = entry.fTime;
			fCurSignalInfo.setCurEntry(entry);
		}

		aRetValue.modifyValue(entry.fValue);

		return time;
	}

	public ZILType getSignalType(int aSignalIdx) {
		return fAllSignals.get(aSignalIdx).getType();
	}

	public int findSignalIdx(PathName aPath) {
		// FIXME: implement
		throw new RuntimeException("Sorry, not implemented yet.");
	}

	public List<Integer> findSignalIdxRegexp(String aSearchString, int aLimit) {

		List<Integer> signalIdx = new ArrayList<Integer>();
		for (int idx = 0; idx < fAllSignals.size(); idx++) {
			SignalInfo signalInfo = fAllSignals.get(idx);
			String path = signalInfo.getPath().getPath();
			// FIXME: implement more complicated path search: use SimpleRegexp?
			if (path.contains(aSearchString)) {
				signalIdx.add(idx);
			}
		}

		return signalIdx;
	}

	public PathName getSignalName(int aIdx) {
		SignalInfo si = fAllSignals.get(aIdx);
		return si.getPath();
	}

	public int getNumSignals() {
		return fAllSignals.size();
	}

	/**
	 * Run the Simulator for aTime ps
	 * 
	 * @param aTime
	 * @throws SimException
	 * @throws ZamiaException
	 */

	public void run(long aTime) throws ZamiaException {

		long startTime = fSimulationTime;

		simulate(fSimulationTime + aTime);

		fSimulationTime = startTime + aTime;

		notifyChanges(fSimulationTime);
	}

	/**
	 * assigns a value to an input port
	 * 
	 * @param aPort
	 * @param aValue
	 * @throws SimException
	 * @throws ZamiaException
	 */
	public void assign(RTLPort aPort, ZILValue aValue) throws ZamiaException {
		fChangeList = new ArrayList<SignalChange>();

		setDelta(aPort, aValue);

		processDelta();

		logChanges(fChangeList, getCurrentTime());

		notifyChanges(getCurrentTime());
	}

	public long getCurrentTime() {
		return fSimulationTime;
	}

	public void trace(int aSignalIdx) throws ZamiaException {

		SignalInfo si = fAllSignals.get(aSignalIdx);

		PathName path = si.getPath();

		RTLSignal signal = findSignal(path);

		if (fSIs.containsKey(signal))
			return;

		fSIs.put(signal, si);

		si.add(getCurrentTime(), fSignalValues.get(signal), true);
	}

	public void unTrace(int aSignalIdx) throws ZamiaException {
		SignalInfo si = fAllSignals.get(aSignalIdx);

		PathName path = si.getPath();

		RTLSignal signal = findSignal(path);

		fSIs.remove(signal);
	}

	public void addObserver(SimObserver o) {
		fObservers.add(SimObserver.class, o);
	}

	public void removeObserver(SimObserver o) {
		fObservers.remove(SimObserver.class, o);
	}

	/*
	 * The following methods are public but are only to be called from
	 * sim.beaviors classes
	 */

	/**
	 * Register the given signal with the simulator
	 * 
	 * Internal use only
	 * 
	 * @throws ZamiaException
	 * 
	 */
	public void init(RTLSignal aSignal) throws ZamiaException {
		ZILValue iv = aSignal.getInitialValue();

		if (iv == null) {
			iv = ZILValue.generateUValue(aSignal.getType(), null, null);
		}

		fSignalValues.put(aSignal, iv);
		SignalInfo si = fSIs.get(aSignal);
		if (si != null) {
			si.add(0, iv, true);
		}
		si = fSIsInternal.get(aSignal);
		if (si != null) {
			si.add(0, fSignalValues.get(aSignal), true);
		}
	}

	/**
	 * Init the given module
	 * 
	 * Internal use only
	 * 
	 * @param aModule
	 * @throws SimException
	 * @throws ZamiaException
	 */
	public void init(RTLModule aModule) throws ZamiaException {
		IRTLModuleBehavior behavior = getModuleBehavior(aModule);
		behavior.init(aModule, this);
	}

	/**
	 * Schedule a delta diff for the given port
	 * 
	 * Internal use only
	 * 
	 * @param aPort
	 * @param aValue
	 */
	public void setDelta(RTLPort aPort, ZILValue aValue) {
		fDeltaPortValues.set(new PortValue(aPort, aValue));
	}

	/**
	 * Get delta value for the given port
	 * 
	 * Internal use only.
	 * 
	 * @param aPort
	 * @return
	 * @throws ZamiaException
	 */

	public ZILValue getDelta(RTLPort aPort) throws ZamiaException {
		aPort.setDriving(true);
		ZILValue v = fDeltaPortValues.get(aPort, getValue(aPort));

		logger.debug("Simulator: getDelta(%s) => %s", aPort, v);

		return v;
	}

	/**
	 * Get the given ports current value.
	 * 
	 * Internal use only.
	 * 
	 * @param aPort
	 * @return
	 * @throws ZamiaException
	 */

	public ZILValue getValue(RTLPort aPort) throws ZamiaException {
		ZILValue value = fPortValues.get(aPort);
		if (value == null) {

			value = ZILValue.generateUValue(aPort.getType(), null, null);

			fPortValues.put(aPort, value);
		}
		logger.debug("Simulator: getValue (%s) => %s", aPort, value);
		return value;
	}

	/**
	 * Get a signals current value.
	 * 
	 * Internal use only.
	 * 
	 * @param aSignal
	 * @return
	 * @throws ZamiaException
	 */
	public ZILValue getValue(RTLSignal aSignal) throws ZamiaException {
		if (fSignalValues == null)
			throw new ZamiaException("Simulator not started yet (Simulator.getValue)");

		RTLSignal s = aSignal.getCurrent();

		ZILValue value = fSignalValues.get(s);
		if (value == null) {
			value = ZILValue.generateUValue(s.getType(), null, null);
			fSignalValues.put(s, value);
		}
		logger.debug("Simulator: getValue (%s) => %s", aSignal, value);
		return value;
	}

	/**
	 * Request a wakeup to the given runtime at the given time
	 * 
	 * @param aT
	 * @param aInterpreterRuntimeEnv
	 */
	public void scheduleWakeup(long aT, int aId, ZILInterpreterRuntimeEnv aInterpreterRuntimeEnv) {

		fSimSchedule.schedule(aT, new WakeupRequest(aInterpreterRuntimeEnv, aId));
	}

	/**
	 * Request a port value schange to the given runtime at the given time
	 * 
	 * @param aPVW
	 * @param aInertial
	 * @param aDelay
	 * @param aReject
	 * @throws ZamiaException
	 */

	public void shedule(PortVarWriter aPVW, boolean aInertial, long aDelay, long aReject, ZILInterpreterRuntimeEnv aInterpreterRuntimeEnv) throws ZamiaException {
		logger.debug("Simulator: scheduling pvw %s, delay=%d, inertial=%b, reject=%d", aPVW, aDelay, aInertial, aReject);

		long reqT = getCurrentTime() + aDelay;

		long rejectT = reqT - aReject;

		// first handle transport / inertial delay mechanism

		RTLPort port = aPVW.getPort();
		ZILValue value = aPVW.getValue();

		ArrayList<PortChangeRequest> schedule = fPortSchedules.get(port);

		if (schedule == null) {
			schedule = new ArrayList<PortChangeRequest>();
			fPortSchedules.put(port, schedule);
		}

		// cleanup / update schedule

		int i = 0;
		while (i < schedule.size()) {

			PortChangeRequest pcr = schedule.get(i);

			long t = pcr.getTime();

			// delete outdated entries
			if (t < fSimulationTime) {
				schedule.remove(i);
				continue;
			}

			// delete later requests

			if (t >= reqT) {
				pcr.setCanceled(true);
			}

			// if inertial, delete events in the reject time period

			if (aInertial) {
				if (t > rejectT) {

					ZILValue v2 = pcr.getValue();

					if (!v2.equals(value)) {
						pcr.setCanceled(true);
					}
				}
			}

			i++;
		}

		// now, schedule the request

		PortChangeRequest pcr = new PortChangeRequest(reqT, aPVW, aInterpreterRuntimeEnv);

		fSimSchedule.schedule(reqT, pcr);
		schedule.add(pcr);
	}

	/*
	 * internal, private simulator functions follow below this point
	 */

	/**
	 * This is the main simulation routine.
	 * 
	 * @param aTimeLimit
	 * @throws SimException
	 * @throws ZamiaException
	 */

	private void simulate(long aTimeLimit) throws ZamiaException {

		int timer = 0;

		while (true) {
			if (fSimSchedule.isEmpty()) {
				break;
			}

			timer++;
			if (timer > TIMEOUT) {
				throw new ZamiaException("Simulator loop timeout.");
			}

			RequestList rl = fSimSchedule.getFirst();

			if (rl.getTime() > aTimeLimit) {
				break;
			}

			fSimulationTime = rl.getTime();

			logger.debug("Simulator: *************************************************");
			logger.debug("Simulator: ** Simulation time is now %5d ns            **", fSimulationTime / 1000);
			logger.debug("Simulator: *************************************************");

			fSimSchedule.removeFirst();

			fActiveSignals = null;

			rl.execute(this);

			while (!fDeltaPortValues.isEmpty()) {

				fChangeList = new ArrayList<SignalChange>();

				processDelta();

				computeActiveSignals();

				propagateSignalChanges();

				logChanges(fChangeList, getCurrentTime());
			}
		}
	}

	private void computeActiveSignals() {
		fActiveSignals = new HashMap<RTLSignal, SignalChange>();

		int n = fChangeList.size();
		for (int i = 0; i < n; i++) {
			SignalChange sc = fChangeList.get(i);
			fActiveSignals.put(sc.getSignal(), sc);
		}
	}

	private void propagateSignalChanges() throws ZamiaException {

		int m = fChangeList.size();
		for (int j = 0; j < m; j++) {

			SignalChange sc = fChangeList.get(j);

			if (!sc.isEvent())
				continue;

			RTLSignal signal = sc.getSignal();

			ZILValue sv = sc.getValue();

			logger.debug("Simulator: propagating signal change =>%s for signal %s", sv, signal.getPath());

			int n = signal.getNumConns();
			for (int i = 0; i < n; i++) {
				RTLPort port = signal.getConn(i);

				// ZILValue pv = getValue(port);

				// if (!sv.equals(pv)) {
				setPort(port, sv);
				// }
			}
		}
	}

	private void processDelta() throws ZamiaException {
		int timer = 0;
		while (!fDeltaPortValues.isEmpty()) {
			PortValue pv = fDeltaPortValues.removeFirst();

			fPortValues.put(pv.fPort, pv.fValue);

			drivePort(pv.fPort);

			timer++;
			if (timer > TIMEOUT) {
				logger.error("Simulator: ERROR: timeout occured while processing delta.");
			}
		}

		fDeltaPortValues.clear();
	}

	private void drivePort(RTLPort aOrigin) throws ZamiaException {

		RTLSignal signal = aOrigin.getSignal();

		if (signal == null)
			return;

		ZILValue oldValue = getValue(signal);

		ZILValue newValue = null;

		int n = signal.getNumConns();
		for (int i = 0; i < n; i++) {
			RTLPort port = signal.getConn(i);
			newValue = merge(newValue, port);
		}

		logger.debug("Simulator: driving port %s, ov=%s, nv=%s", aOrigin, oldValue, newValue);

		// if value has changed, schedule change for all
		// connected ports
		if (newValue != null && !newValue.equals(oldValue)) {

			fSignalValues.put(signal, newValue);

			fChangeList.add(new SignalChange(signal, newValue, true));
		} else {
			// log transaction
			fChangeList.add(new SignalChange(signal, oldValue, false));
		}
	}

	private void setPort(RTLPort aPort, ZILValue aValue) throws ZamiaException {

		logger.debug("Simulator: setting port %s to %s", aPort, aValue);

		RTLModule module = aPort.getModule();

		IRTLModuleBehavior behavior = getModuleBehavior(module);

		behavior.setPort(aPort, aValue, this);

		fPortValues.put(aPort, aValue);
	}

	private ZILValue merge(ZILValue aValue, RTLPort aPort) throws ZamiaException {

		try {
			if (aPort.getDirection() == PortDir.IN)
				return aValue;
			if (!aPort.isDriving())
				return aValue;

			ZILValue res = aValue;
			ZILValue pv;

			pv = getValue(aPort);

			if (res == null)
				return pv;

			if (pv == null)
				return res;

			return res.merge(pv);
		} catch (ZamiaException e) {
			el.logException(e);
			throw new ZamiaException(e.toString());
		}

	}

	@SuppressWarnings("unchecked")
	private IRTLModuleBehavior getModuleBehavior(RTLModule aModule) throws ZamiaException {
		Class c = aModule.getClass();
		IRTLModuleBehavior behavior = fBehaviorMap.get(c);

		if (behavior == null)
			throw new ZamiaException("Internal error: no behavior for class " + c + " defined.");

		return behavior;
	}

	// private void untrace(String signalPath_) throws SimException {
	// RTLGraph rtlg = parsePath(signalPath_);
	//
	// String pes[] = signalPath_.split("/");
	// int n = pes.length;
	// String regexp = SimpleRegexp.convert(pes[n - 1]);
	// n = rtlg.getNumSignals();
	// for (int i = 0; i < n; i++) {
	// RTLSignal s = rtlg.getSignal(i);
	// if (s.getId().matches(regexp))
	// untrace(s);
	// }
	// }

	private void logChanges(ArrayList<SignalChange> aChanges, long aTime) {
		for (Iterator<SignalChange> i = aChanges.iterator(); i.hasNext();) {
			SignalChange change = i.next();

			SignalInfo si = fSIs.get(change.getSignal());
			if (si != null)
				si.add(aTime, change.getValue(), change.isEvent());
			si = fSIsInternal.get(change.getSignal());
			if (si != null)
				si.add(aTime, change.getValue(), change.isEvent());
		}
	}

	private RTLSignal findSignal(PathName aSignalPathName) throws ZamiaException {

		RTLSignal s = fSignalMap.get(aSignalPathName);
		if (s != null) {
			return s;
		}

		s = fRoot.findSignal(aSignalPathName, 0);
		fSignalMap.put(aSignalPathName, s);

		return s;
	}

	private void notifyChanges(long aTime) {
		Object[] listeners = fObservers.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == SimObserver.class) {
				((SimObserver) listeners[i + 1]).notifyChanges(this, aTime);
			}
		}
	}

	private void notifyReset() {
		Object[] listeners = fObservers.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == SimObserver.class) {
				((SimObserver) listeners[i + 1]).notifyReset(this);
			}
		}
	}

	/*
	 * following methods are used by the interpreter commands to implement
	 * signal attributes (e.g. 'QUIET, 'DELAYED)
	 */

	public void traceInternal(RTLSignal aSignal) {
		if (fSIsInternal.containsKey(aSignal))
			return;

		SignalInfo si = new SignalInfo();

		si.setType(aSignal.getType());

		fSIsInternal.put(aSignal, si);

		si.add(getCurrentTime(), fSignalValues.get(aSignal), true);
	}

	public SignalLogEntry getLastEntryInternal(RTLSignal aSignal) throws ZamiaException {

		SignalInfo si = fSIsInternal.get(aSignal);
		if (si == null) {
			throw new ZamiaException("Internal error: signal is not traced!");
		}

		return si.getLastEntry();
	}

	public SignalInfo getSignalInfoInternal(RTLSignal aSignal) {
		return fSIsInternal.get(aSignal);
	}

	public SignalChange getSignalActivity(RTLSignal aSignal) {
		if (fActiveSignals == null)
			return null;
		return fActiveSignals.get(aSignal);
	}

	//	private RTLGraph findGraph(PathName aPath) throws ZamiaException {
	//		RTLGraph graph = fRoot;
	//		int n = aPath.getNumSegments();
	//		for (int i = 0; i < n; i++) {
	//
	//			String segment = aPath.getSegment(i);
	//
	//			RTLModule module = graph.findSub(segment);
	//
	//			if (!(module instanceof RTLGraph)) {
	//				throw new ZamiaException("Invalid path '" + aPath + "': '" + segment + "' is not a subgraph.");
	//			}
	//
	//			graph = (RTLGraph) module;
	//		}
	//		return graph;
	//	}

	// sets all values of all ports linked to the actual port by signal
	// to the new value
	// public void setValue(RTLPort port_, ZILValue value_) throws SimException,
	// ZamiaException {
	// ZILValue oldValue = getValue(port_);
	// // Gate gate = port_.getGate();
	//
	// if (oldValue == value_)
	// return;
	//
	// portValues.put(port_, value_);
	//
	// // drive connected signal
	// calcValue(port_);
	// }

	// public void setInternalValue(RTLPort port_, ZILValue value_) throws
	// SimException, ZamiaException {
	//
	// ZILValue oldValue = getValue(port_);
	//
	// if (oldValue == value_)
	// return;
	//
	// portValues.put(port_, value_);
	//
	// }

}
