/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Aug 14, 2009
 */
package org.zamia.instgraph.sim.ref;

import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGConcurrentStatement;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGContainerItem;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGInstantiation;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGMapping;
import org.zamia.instgraph.IGModule;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGProcess;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGStructure;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.instgraph.interpreter.IGObjectDriver;
import org.zamia.instgraph.sim.IGISimCursor;
import org.zamia.instgraph.sim.IGISimObserver;
import org.zamia.instgraph.sim.IGISimulator;
import org.zamia.util.Pair;
import org.zamia.util.PathName;
import org.zamia.vhdl.ast.OperationLiteral;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;

import javax.swing.event.EventListenerList;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Built-in reference simulator engine
 *
 * @author Guenter Bartsch
 */

public class IGSimRef implements IGISimulator {

	protected final static ZamiaLogger logger = ZamiaLogger.getInstance();

	protected final static ExceptionLogger el = ExceptionLogger.getInstance();

	private static final BigInteger MLN_FS = new BigInteger("1000000");

	private static final int SIM_MAX_ITERATIONS = 1000;

	private ZamiaProject fZPrj;

	private ToplevelPath fTLP;

	private IGManager fIGM;

	private IGModule fToplevel;

	private IGSimSchedule fSimSchedule;

	private BigInteger fSimulationTime;

	private List<IGSignalChange> fChangeList;

	private List<IGSignalChange> fMappedChanges;

	private EventListenerList fObservers = new EventListenerList();

	private SimData fData;

	private Set<IGSimProcess> fProcesses;

	public IGSimRef() {

	}

	@Override
	public void open(ToplevelPath aToplevel, File aFile, PathName aPrefix, ZamiaProject aZPrj) throws IOException, ZamiaException {

		fZPrj = aZPrj;

		fTLP = aToplevel;

		fIGM = fZPrj.getIGM();

		fSimulationTime = BigInteger.ZERO;

		fData = new SimData(this);

		IGItem item = fIGM.findItem(fTLP.getToplevel(), fTLP.getPath());

		if (!(item instanceof IGModule)) {
			throw new ZamiaException("Module expected, " + fTLP + " resolved to " + item);
		}

		fToplevel = (IGModule) item;

		init(fToplevel);

		simulate(BigInteger.ZERO);
	}

	private void simulate(BigInteger aTimeLimit) throws ZamiaException {

		// break endless loops
		int counter = 0;
		BigInteger lastSimulationTime = fSimulationTime;

		while (true) {
			if (fSimSchedule.isEmpty()) {
				break;
			}

			IGRequestList rl = fSimSchedule.getFirst();

			BigInteger reqT = rl.getTime();

			if (reqT.compareTo(aTimeLimit) > 0) {
				break;
			}
			fSimulationTime = reqT;

			BigInteger nanos = fSimulationTime.divide(MLN_FS);

			logger.debug("IGSimRef: *************************************************");
			logger.debug("IGSimRef: ** Simulation time is now %5d ns             **", nanos);
			logger.debug("IGSimRef: *************************************************");

			// have we made progress in time?
			if (lastSimulationTime.compareTo(fSimulationTime) < 0) {
				lastSimulationTime = fSimulationTime;
				counter = 0;
			} else {
				counter++;
				if (counter >= SIM_MAX_ITERATIONS) {
					logger.error("IGRefSim: Error, max iteration limit exceeded at %d ns.", nanos);
					throw new ZamiaException("Simulator max iteration limit exceeded at " + lastSimulationTime + " fs.");
				}
			}

			fSimSchedule.removeFirst();

			rl.executeSignals(this);

			// at this point signal delta values should already reside in SignalDrivers as next values.

			processDelta(rl);

			propagateSignalChanges();

			logChanges();

			rl.executeWakeups(this);
		}

	}

	private void logChanges() throws ZamiaException {
		BigInteger currentTime = getEndTime();
		fData.logChanges(fChangeList, currentTime);
		fData.logChanges(fMappedChanges, currentTime);
	}

	private void propagateSignalChanges() throws ZamiaException {

		Collection<IGSignalDriver> eventDrivers = new LinkedList<IGSignalDriver>();

		for (IGSignalChange signalChange : fChangeList) {
			if (signalChange.isEvent()) {

				IGSignalDriver driver = signalChange.getDriver();

				driver.notifyChange();

				eventDrivers.add(driver);
			}
		}

		invalidateEvents(eventDrivers);
	}

	private void invalidateEvents(Collection<IGSignalDriver> eventDrivers) throws ZamiaException {

		for (IGSignalDriver driver : eventDrivers) {
			driver.resetEvent();
		}
	}

	private void init(IGModule aToplevel) throws ZamiaException {

		fProcesses = new HashSet<IGSimProcess>();

		fSimSchedule = new IGSimSchedule();

		PathName path = aToplevel.getStructure().getPath().getPath();

		IGSimProcess moduleEnv = newProcess(path/* todo: or null? */);
		IGSimContext moduleContext = moduleEnv.pushContextFor(path);

		initObjects(aToplevel.getContainer(), moduleEnv, path);

		// FIXME: fill moduleContext

		initStructure(aToplevel.getStructure(), moduleContext, path);
	}

	private IGSimProcess newProcess(PathName aPath) {
		IGSimProcess process = new IGSimProcess(this, aPath, fZPrj);
		fProcesses.add(process);
		return process;
	}

	private void initStructure(IGStructure aStructure, IGSimContext aParentContext, PathName aParentPath) throws ZamiaException {

		int n = aStructure.getNumStatements();
		for (int i = 0; i < n; i++) {

			IGConcurrentStatement stmt = aStructure.getStatement(i);

			if (stmt instanceof IGProcess) {

				IGProcess proc = (IGProcess) stmt;

				IGSimProcess processEnv = newProcess(aParentPath);
				processEnv.pushContext(aParentContext);
				processEnv.pushContextFor(aParentPath);

				IGContainer pContainer = proc.getContainer();

				initObjects(pContainer, processEnv, processEnv.getPath());

				// get process code:

				IGSequenceOfStatements seq = proc.getSequenceOfStatements();

				IGInterpreterCode code = new IGInterpreterCode(proc.getLabel(), proc.computeSourceLocation());

				seq.generateCode(code);

				processEnv.call(code, ASTErrorMode.EXCEPTION, null);
				processEnv.resume(ASTErrorMode.EXCEPTION, null);

			} else if (stmt instanceof IGInstantiation) {

				IGInstantiation inst = (IGInstantiation) stmt;

				// get inst. module
				IGModule instModule = fIGM.findModule(inst.getSignature());
				IGContainer iContainer = instModule.getContainer();
				PathName instPath = aParentPath.clonePathName().append(inst.getLabel());

				// prepare environment for inst. module
				IGSimProcess instEnv = newProcess(instPath);
				instEnv.pushContext(aParentContext);
				IGSimContext instContext = instEnv.pushContextFor(instPath);

				// init inst. module
				initObjects(iContainer, instEnv, instEnv.getPath());

				// pass generics
				initGenerics(inst, iContainer, instEnv);

				// mappings => processes
				int numMappings = inst.getNumMappings();
				for (int j = 0; j < numMappings; j++) {
					IGMapping mapping = inst.getMapping(j);

					SourceLocation src = mapping.computeSourceLocation();

					IGInterpreterCode code = new IGInterpreterCode(inst.getLabel(), src);

					mapping.generateCode(code, src);

					instEnv.call(code, ASTErrorMode.EXCEPTION, null);
					instEnv.resume(ASTErrorMode.EXCEPTION, null);
				}

				// init structure of inst. module (concurrent statements)
				initStructure(instModule.getStructure(), instContext, instPath);

			} else if (stmt instanceof IGStructure) {

				IGStructure struct = (IGStructure) stmt;

				PathName structPath = aParentPath.clonePathName().append(struct.getLabel());

				// prepare environment for generated module
				IGSimProcess structEnv = newProcess(structPath);
				structEnv.pushContext(aParentContext); // for-generate constant will be added to parent context
				// FIXME: TODO: the constant added to aParentContext will be visible everywhere aParentContext is used! It shouldn't be so. Use a new dedicated context here.  

				// init generated module
				IGContainer sContainer = struct.getContainer();
				initObjects(sContainer, structEnv, structEnv.getPath());

				// init structure of generated module (concurrent statements)
				initStructure(struct, aParentContext, structPath);

			} else {
				throw new ZamiaException("IGSimRef: unsupported concurrent statement: " + stmt);
			}
		}
	}

	private void initGenerics(IGInstantiation aInst, IGContainer aInstContainer, IGInterpreterRuntimeEnv aRuntime) throws ZamiaException {
		ArrayList<Pair<String, IGStaticValue>> actualGenerics = aInst.getActualGenerics();

		for (Pair<String, IGStaticValue> actualGeneric : actualGenerics) {

			ArrayList<IGContainerItem> localItems = aInstContainer.findLocalItems(actualGeneric.getFirst());

			if (localItems.size() > 1) {
				logger.debug(getClass().getSimpleName() + ": 1 generic item expected for %s, found %s: %s", actualGeneric.getFirst(), localItems.size(), localItems);
			}

			IGContainerItem item = localItems.get(0);
			if (!(item instanceof IGObject)) {
				continue;
			}

			IGObject object = (IGObject) item;

			aRuntime.setObjectValue(object, actualGeneric.getSecond(), aInst.computeSourceLocation());
		}
	}

	private void processDelta(IGRequestList aRequestList) throws ZamiaException {

		// collect changed signals from delta
		fChangeList = new ArrayList<IGSignalChange>();
		fMappedChanges = new LinkedList<IGSignalChange>();

		Collection<IGSignalChangeRequest> signalChanges = aRequestList.filterSignalChanges();

		mergeDrivers(signalChanges);

		for (IGSignalChangeRequest scr : signalChanges) {

			IGSignalDriver driver = scr.getDriver();

			if (driver.isActive()) {

				driver.drive();

				IGSignalChange newChange = driver.createSignalChange();

				fChangeList.add(newChange);

				driver.collectChanges(fMappedChanges, fData.getTracedSignals());
			}
		}
	}

	private void mergeDrivers(Collection<IGSignalChangeRequest> aSignalChanges) throws ZamiaException {

		Collection<IGSignalChangeRequest> mergedRequests = new LinkedList<IGSignalChangeRequest>();

		for (IGSignalChangeRequest req : aSignalChanges) {

			IGSignalDriver driver = req.getDriver();

			if (driver.isActive()) {

				IGSignalDriver mergedDriver = driver.mergeDrivers(req.getProcess());

				if (mergedDriver != null) {

					IGSignalChangeRequest mergedRequest = new IGSignalChangeRequest(req.getProcess(), req.getTime(), mergedDriver.getNextValue(), mergedDriver, null);

					mergedRequests.add(mergedRequest);
				}
			}
		}

		for (IGSignalChangeRequest mergeRequest : mergedRequests) {
			aSignalChanges.add(mergeRequest);
		}
	}

	private void initObjects(IGContainer aContainer, IGInterpreterRuntimeEnv aEnv, PathName aPath) throws ZamiaException {
		int n = aContainer.getNumLocalItems();
		for (int i = 0; i < n; i++) {
			IGContainerItem item = aContainer.getLocalItem(i);

			if (!(item instanceof IGObject)) {
				continue;
			}

			IGObject obj = (IGObject) item;

			SourceLocation src = obj.computeSourceLocation();

			IGObjectDriver driver = aEnv.newObject(obj, ASTErrorMode.EXCEPTION, null, src);

			if (driver == null) {
				throw new ZamiaException("IGSimRef: initObject(): could not compute static type for new object: " + obj, src);
			}

			if (driver.getValue(src) == null) {
				IGStaticValue zValue = IGStaticValue.generateZ(obj.getType().computeStaticType(aEnv, ASTErrorMode.EXCEPTION, null), src);
				IGTypeStatic zT = zValue.getStaticType();
				if (!(zT.isArray() && zT.isUnconstrained())) {
					driver.setValue(zValue, src);
				}
			}

			if (driver instanceof IGSignalDriver) {

				IGSignalDriver sigDriver = (IGSignalDriver) driver;

				PathName signalPath = aPath.clonePathName().append(obj.getId());

				sigDriver.setPath(signalPath);

				fData.registerDriver(sigDriver);
				fData.registerContainer(signalPath, aContainer);
				fData.addSignalValue(signalPath, BigInteger.ZERO, driver.getValue(src), true);
			}

		}
	}

	@Override
	public void addObserver(IGISimObserver aO) {
		fObservers.add(IGISimObserver.class, aO);
	}

	@Override
	public void removeObserver(IGISimObserver aO) {
		fObservers.remove(IGISimObserver.class, aO);
	}

	private void notifyChanges(BigInteger aTime) {
		Object[] listeners = fObservers.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IGISimObserver.class) {
				((IGISimObserver) listeners[i + 1]).notifyChanges(this, aTime);
			}
		}
	}

	@Override
	public void assign(PathName aSignalName, IGStaticValue aValue) throws ZamiaException {

		IGSignalDriver driver = fData.getDriver(aSignalName);
		if (driver == null) {
			logger.debug("IGSimRef: assign(): trying to assign value to unregistered signal: path=%s, value=%s", aSignalName, aValue);
			return;
		}

		// snapshot before assigning
		boolean hasChangeNow = fData.hasChangeNow(aSignalName);
		fData.storeActiveSignals(aSignalName);

		BigInteger currentTime = getEndTime();

		fSimSchedule.schedule(currentTime, new IGSignalChangeRequest(null, currentTime, aValue, driver, null));

		simulate(currentTime);

		// repair trace with the help of snapshot
		fData.repairTrace(aSignalName, hasChangeNow);
		fData.repairActiveSignals();
	}

	@Override
	public List<PathName> findSignalNamesRegexp(String aSearchString, int aLimit) {
		return fData.findSignalPaths(aSearchString, aLimit);
	}

	@Override
	public IGISimCursor createCursor() {
		return new RefSimCursor(fData);
	}

	@Override
	public SourceLocation getCurrentSourceLocation() throws ZamiaException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigInteger getEndTime() {
		return fSimulationTime;
	}

	@Override
	public int getInterfaceVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BigInteger getStartTime() {
		return BigInteger.ZERO;
	}

	@Override
	public boolean isSimulator() {
		return true;
	}

	@Override
	public void reset() throws ZamiaException {
		// Clear simulation data
		fData.reset();
		// Reset time
		fSimulationTime = BigInteger.ZERO;
		// Reinitialize
		init(fToplevel);
		simulate(BigInteger.ZERO);

		notifyChanges(fSimulationTime);
	}

	@Override
	public void run(BigInteger aTime) throws ZamiaException {

		BigInteger startTime = fSimulationTime;

		BigInteger timeLimit = startTime.add(aTime);

		try {
			simulate(timeLimit);
		} catch (ZamiaException e) {
			notifyChanges(fSimulationTime);
			throw e;
		}

		fSimulationTime = timeLimit;

		notifyChanges(fSimulationTime);
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void trace(PathName aSignalName) throws ZamiaException {
		fData.trace(aSignalName, getEndTime());
	}

	@Override
	public void unTrace(PathName aSignalName) throws ZamiaException {
		fData.untrace(aSignalName);
	}

	public void scheduleWakeup(BigInteger aT, IGSimProcess aProcess) throws ZamiaException {
		fSimSchedule.schedule(aT, new IGWakeupRequest(aProcess));
	}

	public void scheduleSignalChange(IGSimProcess aProcess, boolean aInertial, IGStaticValue aDelay, IGStaticValue aReject, IGStaticValue aValue, IGSignalDriver aSignalDriver, SourceLocation aLocation)
			throws ZamiaException {

		logger.debug("IGSimRef: scheduling signal change for %s, value=%s, delay=%s, inertial=%b, reject=%s", aSignalDriver.toString(), aValue, aDelay, aInertial, aReject);

		BigInteger reqT = aDelay != null ? getEndTime().add(aDelay.getNum()) : getEndTime();

		BigInteger rejectT = aReject != null ? reqT.subtract(aReject.getNum()) : reqT;

		// first handle transport / inertial delay mechanism

		IGSignalChangeRequest scr = new IGSignalChangeRequest(aProcess, reqT, aValue, aSignalDriver, aLocation);

		aSignalDriver.scheduleChange(aInertial, rejectT, scr, fSimulationTime);

		// now, schedule the request

		fSimSchedule.schedule(reqT, scr);
	}

	public void cancelAllWakeups(IGSimProcess aProcess, SourceLocation aLocation) {
		fSimSchedule.cancelAllWakeups(aProcess);
		fData.removeListener(aProcess);
	}

	public IGStaticValue parseValue(String aValueStr, IGTypeStatic aType, PathName aSignalPath) throws ZamiaException {

		//todo: different literals
		OperationLiteral literal;
		if (aType.isEnum()) {
			literal = new OperationLiteral(aValueStr, OperationLiteral.LiteralCat.ENUM, null, 0);
		} else if (aType.isArray()) {
			literal = new OperationLiteral(aValueStr, OperationLiteral.LiteralCat.BIT_STRING, null, 0);
		} else {
			logger.debug("IGSimRef: parsing (creating OperationLiteral from) user input of type %s", aType.toHRString());
			literal = new OperationLiteral(aValueStr, OperationLiteral.LiteralCat.DECIMAL, null, 0);
		}

		IGContainer container = fData.getContainer(aSignalPath);
		IGElaborationEnv elabEnv = new IGElaborationEnv(fZPrj);
		IGInterpreterRuntimeEnv runtimeEnv = new IGInterpreterRuntimeEnv(new IGInterpreterCode("UserInputParse", null), fZPrj);
		runtimeEnv.exitContext(); // drop global package context
		elabEnv.setInterpreterEnv(runtimeEnv);

		ArrayList<IGOperation> igOpList = literal.computeIG(aType, container, elabEnv, null, ASTErrorMode.EXCEPTION, null);
		if (igOpList == null || igOpList.size() == 0) {
			return null;
		}

		IGOperation operation = igOpList.get(0);

		return operation.computeStaticValue(runtimeEnv, ASTErrorMode.EXCEPTION, null);

	}

	public IGStaticValue getLastValue(PathName aSignalName) {

		IGSignalDriver driver = fData.getDriver(aSignalName);

		return driver.getLastValue();
	}

	public void filterExecutedSource(Collection<SourceLocation> aExecuted) {
		for (IGSimProcess process : fProcesses) {
			process.filterExecutedSource(aExecuted);
		}
	}
}
