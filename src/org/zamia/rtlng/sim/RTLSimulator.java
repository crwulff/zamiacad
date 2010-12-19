/*
 * Copyright 2004-2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.rtlng.sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.event.EventListenerList;

import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.rtlng.RTLManager;
import org.zamia.rtlng.RTLModule;
import org.zamia.rtlng.RTLNode;
import org.zamia.rtlng.RTLPort;
import org.zamia.rtlng.RTLSignal;
import org.zamia.rtlng.RTLType;
import org.zamia.rtlng.RTLValue;
import org.zamia.rtlng.RTLValue.BitValue;
import org.zamia.rtlng.RTLValueBuilder;
import org.zamia.rtlng.nodes.RTLNBinaryOp;
import org.zamia.rtlng.nodes.RTLNInstantiation;
import org.zamia.rtlng.nodes.RTLNLiteral;
import org.zamia.rtlng.nodes.RTLNRegister;
import org.zamia.rtlng.sim.behaviors.RTLBBinaryOp;
import org.zamia.rtlng.sim.behaviors.RTLBLiteral;
import org.zamia.rtlng.sim.behaviors.RTLBModule;
import org.zamia.rtlng.sim.behaviors.RTLBRegister;
import org.zamia.util.HashSetArray;
import org.zamia.util.PathName;
import org.zamia.util.ZStack;
import org.zamia.zdb.ZDB;

/**
 * Cycle-based simulator for pure RTL graphs
 *  
 * @author Guenter Bartsch
 * 
 */

public class RTLSimulator {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private static final int MAX_SIM_ITERATIONS = 50;

	@SuppressWarnings("rawtypes")
	private final HashMap<Class, RTLNodeBehavior> fBehaviorMap;

	private final ZamiaProject fZPrj;

	private final ZDB fZDB;

	private final RTLManager fRTLM;

	/*
	 * acces to signals from the public interface is through
	 * numeric indexes, so we compute a list of all signals here
	 */

	private ArrayList<RTLSignalSimAnnotation> fAllSignals;

	private HashMap<Integer, RTLSignalSimAnnotation> fSIMap;

	/*
	 * the simulated RTL model
	 */

	private RTLModule fRoot;

	/*
	 * keep track of ports which have delta values
	 */

	private HashSetArray<RTLPortSimAnnotation> fDeltaPorts;

	/*
	 * public sim interface data
	 */

	private long fCycles;

	private EventListenerList fObservers = new EventListenerList();

	private RTLSignalSimAnnotation fCurSignalInfo;

	private ArrayList<RTLSimContext> fContexts;

	@SuppressWarnings("rawtypes")
	public RTLSimulator(ZamiaProject aZPrj) throws ZamiaException {

		fZPrj = aZPrj;
		fRTLM = fZPrj.getRTLM();
		fZDB = fZPrj.getZDB();

		fBehaviorMap = new HashMap<Class, RTLNodeBehavior>();

		fBehaviorMap.put(RTLNBinaryOp.class, new RTLBBinaryOp());
		fBehaviorMap.put(RTLNRegister.class, new RTLBRegister());
		fBehaviorMap.put(RTLNLiteral.class, new RTLBLiteral());
		fBehaviorMap.put(RTLModule.class, new RTLBModule());
	}

	/*
	 * public interface starts here
	 */

	class InstJob {
		private final RTLModule fModule;

		private final PathName fPath;

		public InstJob(RTLModule aModule, PathName aPath) {
			fModule = aModule;
			fPath = aPath;
		}

		public RTLModule getModule() {
			return fModule;
		}

		public PathName getPath() {
			return fPath;
		}

	}

	/**
	 * Generate internal simulator annotation model for the given RTLModule,
	 * reset and prepare simulator for run
	 * 	
	 * @param aModule
	 * @throws ZamiaException
	 */
	public void open(RTLModule aModule) throws ZamiaException {
		fRoot = aModule;

		// compute list of signals

		fAllSignals = new ArrayList<RTLSignalSimAnnotation>();
		fSIMap = new HashMap<Integer, RTLSignalSimAnnotation>();

		ZStack<InstJob> todo = new ZStack<InstJob>();
		todo.push(new InstJob(fRoot, new PathName("")));

		fContexts = new ArrayList<RTLSimContext>();

		while (!todo.isEmpty()) {

			InstJob job = todo.pop();

			RTLModule module = job.getModule();
			PathName path = job.getPath();

			RTLSimContext context = new RTLSimContext(module, path, this);
			fContexts.add(context);

			int n = module.getNumSignals();
			for (int i = 0; i < n; i++) {
				RTLSignal signal = module.getSignal(i);

				int idx = fAllSignals.size();

				RTLSignalSimAnnotation si = new RTLSignalSimAnnotation(path.append(signal.getId()), signal, idx, context);

				context.addSignal(signal, si);

				fAllSignals.add(si);
				fSIMap.put(idx, si);
			}

			n = module.getNumPorts();
			for (int i = 0; i < n; i++) {
				RTLPort port = module.getPort(i);

				RTLPortSimAnnotation pi = new RTLPortSimAnnotation(path.append(port.getId()), port, context);

				context.addPort(port, pi);
			}

			n = module.getNumNodes();
			for (int i = 0; i < n; i++) {

				RTLNode node = module.getNode(i);

				PathName p = path.append(node.getInstanceName());

				if (node instanceof RTLNInstantiation) {

					RTLNInstantiation inst = (RTLNInstantiation) node;

					RTLModule m = fRTLM.loadRTLModule(inst.getSignature());

					if (m == null) {
						throw new ZamiaException("Incomplete design: " + inst + " missing RTL module.");
					}

					todo.push(new InstJob(m, p));
				}

				int m = node.getNumPorts();
				for (int j = 0; j < m; j++) {
					RTLPort port = node.getPort(j);

					RTLPortSimAnnotation pi = new RTLPortSimAnnotation(p.append(port.getId()), port, context);

					context.addPort(port, pi);
				}
			}
		}

		reset();
	}

	public void shutdown() {
		// GC will take care of us.
	}

	public RTLModule getRTLModule() {
		return fRoot;
	}

	/**
	 * reset the simulator, will clean trace logs but simulator will remember
	 * which signals/ports should be traced
	 */

	public void reset() throws ZamiaException {

		fDeltaPorts = new HashSetArray<RTLPortSimAnnotation>();

		int n = fAllSignals.size();
		for (int i = 0; i < n; i++) {
			RTLSignalSimAnnotation si = fAllSignals.get(i);
			si.flush();
		}

		notifyReset();

		n = fContexts.size();
		for (int i = 0; i < n; i++) {
			RTLSimContext context = fContexts.get(i);

			RTLModule module = context.getModule();

			reset(module, context);
		}

		simulate();

		fCycles = 0;

	}

	public void simulate() throws ZamiaException {

		int counter = 0;

		while (counter < MAX_SIM_ITERATIONS) {

			logger.debug("RTLSimulator: ***********************************************");
			logger.debug("RTLSimulator: ** simulate: cycle #%3d iteration #%3d       **", fCycles, counter);
			logger.debug("RTLSimulator: ***********************************************");

			// process delta produced by init

			HashSetArray<RTLSignalSimAnnotation> activeSignals = processDelta();

			if (activeSignals.size() == 0) {
				break;
			}

			propagateSignalChanges(activeSignals);

			counter++;
		}

		fCycles++;
		notifyChanges(fCycles);
	}

	public void assign(int aSignalIdx, RTLValue aValue) throws ZamiaException {

		RTLSignalSimAnnotation si = fAllSignals.get(aSignalIdx);

		RTLSimContext context = si.getContext();

		PathName path = si.getPath();

		RTLSignal signal = si.getSignal();

		logger.debug ("RTLSimulator: assign(): %s => %s", path, aValue);
		
		int n = signal.getNumConns();
		boolean foundDriver = false;
		for (int i = 0; i < n; i++) {
			RTLPort p = signal.getConn(i);

			RTLPortSimAnnotation pi = context.findPortSimAnnotation(p);

			if (!pi.isDriving()) {
				continue;
			}

			setDelta(pi, aValue);
			foundDriver = true;
		}

		if (!foundDriver) {

			throw new ZamiaException("Signal " + path + " has no driver.");
		}

	}

	public long getStartCycle() {
		return 0;
	}

	public long getEndCycle() {
		return fCycles;
	}

	public long gotoTransition(long aTimeOffset, int aSignalIdx, SourceLocation aSourceLocation) throws ZamiaException {

		fCurSignalInfo = fAllSignals.get(aSignalIdx);

		RTLSignalLogEntry entry = fCurSignalInfo.getEventEntry(aTimeOffset);

		return entry.fCycles;
	}

	public RTLValue getCurrentValue() {
		RTLSignalLogEntry entry = fCurSignalInfo.getCurrentEntry();

		return entry.fValue;
	}

	public long gotoNextTransition(long aCycleLimit, SourceLocation aSourceLocation) throws ZamiaException {
		RTLSignalLogEntry entry = fCurSignalInfo.getNextEventEntry();

		long time;

		if (entry == null) {
			time = fCycles;
			entry = fCurSignalInfo.getCurrentEntry();
		} else {
			time = entry.fCycles;
			fCurSignalInfo.setCurrentEntry(entry);
		}

		return time;
	}

	public long gotoPreviousTransition(long aTimeLimit, SourceLocation aSourceLocation) throws ZamiaException {
		RTLSignalLogEntry entry = fCurSignalInfo.getPrevEventEntry();

		long time;

		if (entry == null) {
			time = 0;
			entry = fCurSignalInfo.getCurrentEntry();
		} else {
			time = entry.fCycles;
			fCurSignalInfo.setCurrentEntry(entry);
		}

		return time;
	}

	public RTLType getSignalType(int aSignalIdx) {
		return fAllSignals.get(aSignalIdx).getType();
	}

	public int findSignalIdx(PathName aPath) {

		List<Integer> res = findSignalIdxRegexp(aPath.toString(), 1);

		if (res.size() < 1) {
			return -1;
		}

		return res.get(0);
	}

	public List<Integer> findSignalIdxRegexp(String aSearchString, int aLimit) {

		List<Integer> signalIdx = new ArrayList<Integer>();
		for (int idx = 0; idx < fAllSignals.size(); idx++) {
			RTLSignalSimAnnotation signalInfo = fAllSignals.get(idx);
			String path = signalInfo.getPath().getPath();
			// FIXME: implement more complicated path search: use SimpleRegexp?
			if (path.contains(aSearchString)) {
				signalIdx.add(idx);
			}
		}

		return signalIdx;
	}

	public PathName getSignalName(int aIdx) {
		RTLSignalSimAnnotation si = fAllSignals.get(aIdx);
		return si.getPath();
	}

	public int getNumSignals() {
		return fAllSignals.size();
	}

	public long getCurrentCycle() {
		return fCycles;
	}

	public void trace(int aSignalIdx) throws ZamiaException {

		RTLSignalSimAnnotation si = fAllSignals.get(aSignalIdx);

		si.setTrace(true);
	}

	public void unTrace(int aSignalIdx) throws ZamiaException {
		RTLSignalSimAnnotation si = fAllSignals.get(aSignalIdx);

		si.setTrace(false);
	}

	public void addObserver(RTLSimObserver o) {
		fObservers.add(RTLSimObserver.class, o);
	}

	public void removeObserver(RTLSimObserver o) {
		fObservers.remove(RTLSimObserver.class, o);
	}

	/*
	 * The following methods are public but are only to be called from
	 * sim.beaviors classes
	 */

	/**
	 * Init the given module
	 * 
	 * Internal use only
	 * 
	 * @param aModule
	 * @throws SimException
	 * @throws ZamiaException
	 */
	public void reset(RTLNode aNode, RTLSimContext aContext) throws ZamiaException {
		RTLNodeBehavior behavior = getModuleBehavior(aNode);

		int n = aNode.getNumPorts();
		for (int i = 0; i < n; i++) {

			RTLPort port = aNode.getPort(i);

			RTLPortSimAnnotation pa = aContext.findPortSimAnnotation(port);

			RTLValue v = port.getInitialValue();
			if (v == null) {
				v = RTLValueBuilder.generateUValue(port.getType(), null, fZDB);
			}
			pa.setValue(v);

		}

		behavior.reset(aNode, this, aContext);
	}

	public void setDelta(RTLPortSimAnnotation aPortSimAnnotation, RTLValue aValue) {
		
		logger.debug("RTLSimulator: setDelta(): %s => %s", aPortSimAnnotation.getPath(), aValue);
		
		aPortSimAnnotation.setDeltaValue(aValue);
		fDeltaPorts.add(aPortSimAnnotation);
	}

	/*
	 * internal, private simulator functions follow below this point
	 */

	private void propagateSignalChanges(HashSetArray<RTLSignalSimAnnotation> aActiveSignals) throws ZamiaException {

		int m = aActiveSignals.size();
		for (int j = 0; j < m; j++) {

			RTLSignalSimAnnotation sa = aActiveSignals.get(j);

			RTLSimContext context = sa.getContext();

			RTLSignal signal = sa.getSignal();

			RTLValue sv = sa.getCurrentValue();

			logger.debug("RTLSimulator: propagateSignalChanges(): %s => %s", sa.getPath(), sv);

			int n = signal.getNumConns();
			for (int i = 0; i < n; i++) {
				RTLPort port = signal.getConn(i);

				RTLPortSimAnnotation pa = context.findPortSimAnnotation(port);

				logger.debug("RTLSimulator: propagateSignalChanges():    %s => %s", pa.getPath(), sv);

				RTLNode node = port.getNode();

				RTLNodeBehavior behavior = getModuleBehavior(node);

				behavior.portChange(pa, sv, this);

				pa.setValue(sv);
				
			}
		}
	}

	private HashSetArray<RTLSignalSimAnnotation> processDelta() throws ZamiaException {
		HashSetArray<RTLSignalSimAnnotation> activeSignals = new HashSetArray<RTLSignalSimAnnotation>();

		int n = fDeltaPorts.size();

		logger.debug("RTLSimulator: processDelta(): %d delta ports", n);

		for (int i = 0; i < n; i++) {

			RTLPortSimAnnotation dp = fDeltaPorts.get(i);

			processDeltaPort(activeSignals, dp);

		}

		fDeltaPorts = new HashSetArray<RTLPortSimAnnotation>();

		logger.debug("RTLSimulator: processDelta() done, %d active signals", activeSignals.size());

		return activeSignals;

	}

	private void processDeltaPort(HashSetArray<RTLSignalSimAnnotation> aActiveSignals, RTLPortSimAnnotation aPortSA) throws ZamiaException {

		logger.debug("RTLSimulator: processDeltaPort(): driving port %s", aPortSA.getPath());

		RTLValue deltaValue = aPortSA.getDeltaValue();
		aPortSA.setValue(deltaValue);
		aPortSA.setDeltaValue(null);
		
		RTLPort port = aPortSA.getPort();

		RTLSignal signal = port.getSignal();

		if (signal == null)
			return;

		RTLSimContext context = aPortSA.getContext();

		RTLSignalSimAnnotation sa = context.findSignalSimAnnotation(signal);

		RTLValue oldValue = sa.getCurrentValue();

		RTLValue newValue = null;

		int n = signal.getNumConns();
		for (int i = 0; i < n; i++) {
			RTLPort p = signal.getConn(i);

			RTLPortSimAnnotation pa = context.findPortSimAnnotation(p);
			if (!pa.isDriving()) {
				continue;
			}

			RTLValue portValue = pa.getValue();

			if (newValue == null) {

				newValue = portValue;

			} else {

				SourceLocation location = port.computeSourceLocation();
				newValue = merge(newValue, portValue, location);

			}
		}

		logger.debug("RTLSimulator: processDeltaPort(): port %s ov=%s nv=%s", aPortSA.getPath(), oldValue, newValue);

		if (newValue == null) {
			throw new ZamiaException ("RTLSimulator: Internal error: nv==null");
		}
		
		// if value has changed, schedule change for all
		// connected ports
		if (newValue != null && !newValue.equals(oldValue)) {

			sa.add(fCycles, newValue, true);

			aActiveSignals.add(sa);

			logger.debug("RTLSimulator: processDeltaPort(): signal %s had an event", sa.getPath());

		} else {

			sa.add(fCycles, newValue, false);

			logger.debug("RTLSimulator: processDeltaPort(): signal %s had a transaction", sa.getPath());
		}
	}

	private RTLValue merge(RTLValue aValueA, RTLValue aValueB, SourceLocation aLocation) throws ZamiaException {

		if (aValueA == null) {
			return aValueB;
		}

		if (aValueB == null) {
			return aValueA;
		}

		RTLType type = aValueB.getType();

		switch (type.getCat()) {
		case BIT:

			BitValue b1 = aValueA.getBit();
			BitValue b2 = aValueB.getBit();

			switch (b1) {

			case BV_0:

				if (b2 == BitValue.BV_0 || b2 == BitValue.BV_Z)
					return aValueA;

				return RTLValueBuilder.generateBit(type, BitValue.BV_X, aLocation, fZDB);

			case BV_1:

				if (b2 == BitValue.BV_1 || b2 == BitValue.BV_Z)
					return aValueA;

				return RTLValueBuilder.generateBit(type, BitValue.BV_X, aLocation, fZDB);

			case BV_U:
				if (b2 == BitValue.BV_U)
					return aValueA;
				return RTLValueBuilder.generateBit(type, BitValue.BV_X, aLocation, fZDB);

			case BV_Z:
				return aValueB;

			case BV_X:
				return aValueA;

			default:
				return RTLValueBuilder.generateBit(type, BitValue.BV_X, aLocation, fZDB);

			}

		}

		// FIXME: implement
		throw new ZamiaException("Internal error: Don't know how to merge value for type " + type);
	}

	private RTLNodeBehavior getModuleBehavior(RTLNode aNode) throws ZamiaException {
		@SuppressWarnings("rawtypes")
		Class c = aNode.getClass();
		RTLNodeBehavior behavior = fBehaviorMap.get(c);

		if (behavior == null)
			throw new ZamiaException("Internal error: no behavior for class " + c + " defined.");

		return behavior;
	}

	private void notifyChanges(long aTime) {
		Object[] listeners = fObservers.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == RTLSimObserver.class) {
				((RTLSimObserver) listeners[i + 1]).notifyChanges(this, aTime);
			}
		}
	}

	private void notifyReset() {
		Object[] listeners = fObservers.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == RTLSimObserver.class) {
				((RTLSimObserver) listeners[i + 1]).notifyReset(this);
			}
		}
	}

	public void assign(PathName aSignalPath, String aValue) throws ZamiaException {

		int idx = findSignalIdx(aSignalPath);

		RTLSignalSimAnnotation si = fAllSignals.get(idx);

		RTLSignal signal = si.getSignal();

		RTLType t = signal.getType();

		RTLValue v = RTLValueBuilder.generateValue(t, aValue, null, fZDB);

		assign(idx, v);
	}

	public RTLValue getCurrentValue(PathName aSignalPath) {
		int idx = findSignalIdx(aSignalPath);

		RTLSignalSimAnnotation si = fAllSignals.get(idx);
		
		return si.getCurrentValue();
	}

	public ZDB getZDB() {
		return fZDB;
	}

}
