/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Aug 14, 2009
 */
package org.zamia.instgraph.sim.ref;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.EventListenerList;

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
import org.zamia.instgraph.interpreter.IGInterpreterContext;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.instgraph.interpreter.IGObjectWriter;
import org.zamia.instgraph.sim.IGISimCursor;
import org.zamia.instgraph.sim.IGISimObserver;
import org.zamia.instgraph.sim.IGISimulator;
import org.zamia.util.Pair;
import org.zamia.util.PathName;
import org.zamia.vhdl.ast.OperationLiteral;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;

/**
 * Built-in reference simulator engine
 * 
 * @author Guenter Bartsch
 */

public class IGSimRef implements IGISimulator {

	protected final static ZamiaLogger logger = ZamiaLogger.getInstance();

	protected final static ExceptionLogger el = ExceptionLogger.getInstance();

	private static final BigInteger MLN_FS = new BigInteger("1000000");

	private ZamiaProject fZPrj;

	private ToplevelPath fTLP;

	private IGManager fIGM;

	private IGModule fToplevel;

	private IGInterpreterContext fGlobalPackageContext;

	private IGSimSchedule fSimSchedule;

	private BigInteger fSimulationTime;

	private HashMap<PathName, Map<Long, IGSignalWaveformGen>> fWaveformGenerators;

	private List<IGSignalChange> fChangeList;

	private HashMap<PathName, Map<Long, IGSignalChange>> fActiveSignalsList;

	private EventListenerList fObservers = new EventListenerList();

	private SimData fData;

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

		fGlobalPackageContext = fZPrj.getDUM().getGlobalPackageContext();

		init(fToplevel);

		simulate(BigInteger.ZERO);
	}

	private void simulate(BigInteger aTimeLimit) throws ZamiaException {

		while (true) {
			if (fSimSchedule.isEmpty()) {
				break;
			}

			IGRequestList rl = fSimSchedule.getFirst();

			if (rl.getTime().compareTo(aTimeLimit) > 0) {
				break;
			}

			fSimulationTime = rl.getTime();

			int nanos = fSimulationTime.divide(MLN_FS).intValue();
			
			logger.debug("IGSimRef: *************************************************");
			logger.debug("IGSimRef: ** Simulation time is now %5d ns             **", nanos);
			logger.debug("IGSimRef: *************************************************");

			fSimSchedule.removeFirst();

			rl.execute(this);

			// at this point signal delta values should already reside in fWaveformGenerators as next values.

			processDelta();

			propagateSignalChanges();

			logChanges();
		}

	}

	private void logChanges() throws ZamiaException {
		BigInteger currentTime = getEndTime();
		fData.logChanges(fChangeList, currentTime);
	}

	private void propagateSignalChanges() throws ZamiaException {

		for (IGSignalChange signalChange : fChangeList) {
			if (signalChange.isEvent()) {
				fWaveformGenerators.get(signalChange.getPath()).get(signalChange.getSignal()).notifyChange();
			}
		}

	}

	private void init(IGModule aToplevel) throws ZamiaException {

		fSimSchedule = new IGSimSchedule();
		fWaveformGenerators = new HashMap<PathName, Map<Long, IGSignalWaveformGen>>();

		PathName path = aToplevel.getStructure().getPath().getPath();

		IGSimContext moduleContext = new IGSimContext(this, path);
		IGInterpreterRuntimeEnv moduleEnv = new IGInterpreterRuntimeEnv(null, fZPrj);
		moduleEnv.pushContext(fGlobalPackageContext);
		moduleEnv.pushContext(moduleContext);

		initValues(aToplevel.getContainer(), moduleEnv, path);

		// FIXME: fill moduleContext

		initStructure(aToplevel.getStructure(), moduleContext, path);
	}

	private void initStructure(IGStructure aStructure, IGSimContext aParentContext, PathName aParentPath) throws ZamiaException {

		int n = aStructure.getNumStatements();
		for (int i = 0; i < n; i++) {

			IGConcurrentStatement stmt = aStructure.getStatement(i);

			if (stmt instanceof IGProcess) {

				IGProcess proc = (IGProcess) stmt;

				IGSimContext processContext = new IGSimContext(this, aParentPath);
				IGSimProcess processEnv = new IGSimProcess(this, aParentPath, null, fZPrj);
				processEnv.pushContext(fGlobalPackageContext);
				processEnv.pushContext(aParentContext);
				processEnv.pushContext(processContext);

				IGContainer pContainer = proc.getContainer();

				initValues(pContainer, processEnv, processEnv.getPath());

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
				IGSimContext instContext = new IGSimContext(this, instPath);
				IGSimProcess instEnv = new IGSimProcess(this, instPath, null, fZPrj);
				instEnv.pushContext(fGlobalPackageContext);
				instEnv.pushContext(aParentContext);
				instEnv.pushContext(instContext);

				// init inst. module
				initValues(iContainer, instEnv, instEnv.getPath());

				// pass generics
				initGenerics(inst, iContainer, instEnv);

				// mappings => processes
				int numMappings = inst.getNumMappings();
				for (int j = 0; j < numMappings; j++) {
					IGMapping mapping = inst.getMapping(j);

					// generate synchronization codes (in/out processes) out of mapping
					Collection<IGInterpreterCode> synchroCodes = mapping.generateSynchroCodes(inst.getLabel(), inst.computeSourceLocation());
					for (IGInterpreterCode code : synchroCodes) {
						// create a separate Runtime for mapping processes (use instContext in it!)
						IGSimProcess mappingEnv = new IGSimProcess(this, instPath, null, fZPrj);
						mappingEnv.pushContext(fGlobalPackageContext);
						mappingEnv.pushContext(aParentContext);
						mappingEnv.pushContext(instContext);

						// run code
						mappingEnv.call(code, ASTErrorMode.EXCEPTION, null);
						mappingEnv.resume(ASTErrorMode.EXCEPTION, null);
					}
				}

				// init structure of inst. module (concurrent statements)
				initStructure(instModule.getStructure(), instContext, instPath);

			} else if (stmt instanceof IGStructure) {

				IGStructure struct = (IGStructure) stmt;

				PathName structPath = aParentPath.clonePathName().append(struct.getLabel());

				// prepare environment for generated module
				IGSimProcess structEnv = new IGSimProcess(this, structPath, null, fZPrj);
				structEnv.pushContext(fGlobalPackageContext);
				structEnv.pushContext(aParentContext); // for-generate constant will be added to parent context
				// FIXME: TODO: the constant added to aParentContext will be visible everywhere aParentContext is used! It shouldn't be so. Use a new dedicated context here.  

				// init generated module
				IGContainer sContainer = struct.getContainer();
				initValues(sContainer, structEnv, structEnv.getPath());

				// init structure of generated module (concurrent statements)
				initStructure(struct, aParentContext, structPath);

			} else {
				throw new ZamiaException("IGSimRef: unsupported concurrent statement: " + stmt);
			}
		}
	}

	private void initGenerics(IGInstantiation aInst, IGContainer aInstContainer, IGInterpreterRuntimeEnv aRuntime) throws ZamiaException {
		ArrayList<Pair<String,IGStaticValue>> actualGenerics = aInst.getActualGenerics();
		
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

	private void processDelta() throws ZamiaException {

		// collect active and changed signals from delta
		fChangeList = new ArrayList<IGSignalChange>();
		fActiveSignalsList = new HashMap<PathName, Map<Long, IGSignalChange>>();

		//todo: Potential place for optimization. Iterating over all waveforms for the whole map is probably not the best idea out there.
		for (Map.Entry<PathName, Map<Long, IGSignalWaveformGen>> pathGenEntry : fWaveformGenerators.entrySet()) {
			PathName path = pathGenEntry.getKey();
			Map<Long, IGSignalWaveformGen> waveformBySignal = pathGenEntry.getValue();
			HashMap<Long, IGSignalChange> activeSignalsList = new HashMap<Long, IGSignalChange>();

			for (Map.Entry<Long, IGSignalWaveformGen> dbidGenEntry : waveformBySignal.entrySet()) {
				Long dbid = dbidGenEntry.getKey();
				IGSignalWaveformGen gen = dbidGenEntry.getValue();

				if (gen == null) {
					// occurs when correct place for waveform has been reserved with 'null' value in initValues(),
					// but no transitions have been scheduled, so the waveform remained null up to this point
					logger.debug("IGSimRef.processDelta(): Skipping missing waveformGen in path %s for %s", path, fZPrj.getZDB().load(dbid));
					continue;
				}
				if (gen.isActive()) {

					IGSignalChange newChange = new IGSignalChange(path, dbid, gen.getValue(), gen.isChanged());

					activeSignalsList.put(dbid, newChange);

					fChangeList.add(newChange);

					// assign new value
					// FIXME: TODO: here resolution functions will probably appear
					gen.commit();
				}

			}

			if (!activeSignalsList.isEmpty()) {
				fActiveSignalsList.put(path, activeSignalsList);
			}

		}
	}

	private void initValues(IGContainer aContainer, IGInterpreterRuntimeEnv aEnv, PathName aPath) throws ZamiaException {
		int n = aContainer.getNumLocalItems();
		for (int i = 0; i < n; i++) {
			IGContainerItem item = aContainer.getLocalItem(i);

			if (!(item instanceof IGObject)) {
				continue;
			}

			IGObject obj = (IGObject) item;

			aEnv.newObject(obj, obj.computeSourceLocation());

			long dbid = obj.getDBID();
			PathName signalPath = aPath.clonePathName().append(obj.getId());

			fData.registerContext(signalPath, new IGSignalContext(dbid, aEnv.findContext(dbid)));
			fData.registerContainer(signalPath, aContainer);

			// reserve place for a signal's waveform in correct path
			if (obj.getCat() == IGObject.IGObjectCat.SIGNAL) {
				Map<Long, IGSignalWaveformGen> waveformBySignal = getOrCreateWaveformGenHolder(dbid, aPath);
				waveformBySignal.put(dbid, null); // use null only to reserve the place
			}

			fData.addSignalValue(dbid, BigInteger.ZERO, aEnv.getObjectValue(obj), true);

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

		// snapshot before assigning
		boolean hasChangeNow = fData.hasChangeNow(aSignalName);
		fData.storeActiveSignals(aSignalName);

		BigInteger currentTime = getEndTime();

		fSimSchedule.schedule(currentTime, new IGAssignRequest(null, aSignalName, aValue));

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

		simulate(timeLimit);

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

	public void scheduleWakeup(IGObject aSignal, IGSimProcess aProcess) throws ZamiaException {
		IGSignalWaveformGen gen = getOrCreateWaveformGen(aSignal, aProcess);
		gen.addListener(aProcess);
	}

	private IGSignalWaveformGen getOrCreateWaveformGen(IGObject aSignal, IGSimProcess aProcess) throws ZamiaException {
		long dbid = aSignal.getDBID();

		Map<Long, IGSignalWaveformGen> waveformBySignal = getOrCreateWaveformGenHolder(dbid, aProcess.getPath());

		IGSignalWaveformGen gen = waveformBySignal.get(dbid);
		if (gen == null) {
			IGTypeStatic type = aSignal.getType().computeStaticType(aProcess, ASTErrorMode.EXCEPTION, null);
			gen = new IGSignalWaveformGen(aSignal, type, aProcess.findContext(dbid), this);
			waveformBySignal.put(dbid, gen);
		}
		return gen;
	}

	/**
	 * For specified signal, returns WaveformBySignal where signal's waveform should be put to or obtained from.
	 * <p>
	 * If such a WaveformBySignal is not found, the upmost one (of <code>aRequestPath</code>) is returned.
	 * It gets created if needed.
	 * <p>
	 * <b>Note:</b><br>
	 * This method relies on method
	 * {@link #initValues(org.zamia.instgraph.IGContainer, org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv, org.zamia.util.PathName)}
	 * in the following way. Prior to calling this method, the latter must be called for the {@link org.zamia.instgraph.IGContainer},
	 * holding the requested signal. This is to ensure signal's waveform is created at the right path.  
	 *
	 * FIXME: TODO: check the method against recursive instantiations (module A is instantiated inside module A)
	 *
	 * @param aDBID DBID of the signal to search for 
	 * @param aRequestPath where to start searching from and where to create WaveformBySignal if it is not found
	 * @return WaveformBySignal where signal's waveform should be put to or obtained from
	 */
	private Map<Long, IGSignalWaveformGen> getOrCreateWaveformGenHolder(long aDBID, PathName aRequestPath) {
		// locate the path where the signal's waveform resides.
		Map<Long, IGSignalWaveformGen> waveformBySignal = resolveSignal(aDBID, aRequestPath, fWaveformGenerators);

		if (waveformBySignal != null)
			return waveformBySignal;

		// if waveform is not found, return WaveformBySignal of aRequestPath.
		// create it if needed.
		waveformBySignal = fWaveformGenerators.get(aRequestPath);
		if (waveformBySignal == null) {
			waveformBySignal = new HashMap<Long, IGSignalWaveformGen>();
			fWaveformGenerators.put(aRequestPath, waveformBySignal);
		}
		return waveformBySignal;
	}

	public void scheduleSignalChange(IGSimProcess aProcess, boolean aInertial, IGStaticValue aDelay, IGStaticValue aReject, IGObjectWriter aObjectWriter, SourceLocation aLocation)
			throws ZamiaException {

		logger.debug("IGSimRef: scheduling signal change for %s, delay=%s, inertial=%b, reject=%s", aObjectWriter.getObject(), aDelay, aInertial, aReject);

		BigInteger reqT = aDelay != null ? getEndTime().add(aDelay.getNum()) : getEndTime();

		BigInteger rejectT = aReject != null ? reqT.subtract(aReject.getNum()) : reqT;

		// first handle transport / inertial delay mechanism

		IGObject signal = aObjectWriter.getObject();

		IGSignalWaveformGen gen = getOrCreateWaveformGen(signal, aProcess);

		IGSignalChangeRequest scr = new IGSignalChangeRequest(aProcess, signal.getDBID(), reqT, aObjectWriter, aLocation);

		gen.scheduleChange(aInertial, rejectT, scr);

		// now, schedule the request

		fSimSchedule.schedule(reqT, scr);
	}

	/**
	 * Sets specified delta value to specified signal.
	 * <p>
	 * Internal use only.
	 *
	 * @param aSignal
	 * @param aValue
	 * @param aProcess
	 * @throws ZamiaException if object is not a signal
	 */
	public void setSignalNextValue(IGObject aSignal, IGStaticValue aValue, IGSimProcess aProcess) throws ZamiaException {
		if (aSignal.getCat() != IGObject.IGObjectCat.SIGNAL) {
			throw new ZamiaException(IGSimRef.class.getSimpleName() + ": Signal expected for setting next value, received " + aSignal);
		}

		IGSignalWaveformGen gen = getOrCreateWaveformGen(aSignal, aProcess);

		gen.setNextValue(aValue);
	}

	public IGStaticValue getSignalNextValue(long aDBID, PathName aPath) {
		Map<Long, IGSignalWaveformGen> waveformBySignal = resolveSignal(aDBID, aPath, fWaveformGenerators);
		if (waveformBySignal == null) {
			return null;
		}
		IGSignalWaveformGen gen = waveformBySignal.get(aDBID);
		if (gen == null) {
			return null;
		}
		return gen.getValue();
	}

	public IGSignalChange getSignalActivity(IGObject aSignal, PathName aPath) {
		if (fActiveSignalsList == null) {
			return null; // return no activity when checking for it (e.g. signal_A'EVENT) before at least 0-cycle has been simulated   
		}
		Map<Long, IGSignalChange> activeSignalsMap = resolveSignal(aSignal.getDBID(), aPath, fActiveSignalsList);
		if (activeSignalsMap != null)
			return activeSignalsMap.get(aSignal.getDBID());

		logger.debug("IGSimRef: obtaining signal activity from non-active path. Signal: %s, Path: %s", aSignal, aPath);
		return null;
	}

	public void cancelAllWakeups(IGSimProcess aProcess, SourceLocation aLocation) {
		fSimSchedule.cancelAllWakeups(aProcess);
		for (Map<Long, IGSignalWaveformGen> waveformBySignal : resolve(0, aProcess.getPath(), fWaveformGenerators)) {
			for (IGSignalWaveformGen gen : waveformBySignal.values()) {
				if (gen == null) {
					continue;
				}
				gen.removeListener(aProcess);
			}			
		}
	}

	private <H extends Map<Long, ?>, T extends Map<PathName, H>>
	H resolveSignal(long aDBID, PathName aPath, T aSource) {
		ArrayList<H> mapsList = resolve(aDBID, aPath, aSource);
		return mapsList.size() > 0 ? mapsList.get(0) : null;
	}

	/**
	 * Filters out those maps from aSource that contain specified aDBID as a key.
	 * Filtering is performed during backtrack-traversal of the specified aPath.
	 * For aDBID == 0 all the maps met along the traversal of aPath are returned.
	 *
	 * @param aDBID object's DBID to filter out or 0 for a complete (hierarchical)
	 * 	set of maps of specified aPath
	 * @param aPath where to start backtracking filtering from
	 * @param aSource map of maps to filter from
	 * @return list of those maps within aPath that contain the specified DBID as a key,
	 * or a list of all the maps within aPath if 0-DBID is specified
	 */
	private  <H extends Map<Long, ?>, T extends Map<PathName, H>>
	ArrayList<H> resolve (long aDBID, PathName aPath, T aSource) {

		PathName path = aPath;
		ArrayList<H> retList = new ArrayList<H>(aDBID == 0 ? path.getNumSegments() : 1);
		
		while (true) {

			H map = aSource.get(path);
			if (map != null) {

				if (aDBID == 0) {
					retList.add(map);
				} else if (map.containsKey(aDBID)) {
					retList.add(map);
				}

			}

			if (path.getNumSegments() == 0) {
				break;
			}
			path = path.getParent();
		}

		return retList;
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
		elabEnv.setInterpreterEnv(runtimeEnv);


		ArrayList<IGOperation> igOpList = literal.computeIG(aType, container, elabEnv, null, ASTErrorMode.EXCEPTION, null);
		if (igOpList == null || igOpList.size() == 0) {
			return null;
		}

		IGOperation operation = igOpList.get(0);

		return operation.computeStaticValue(runtimeEnv, ASTErrorMode.EXCEPTION, null);

	}

	private class IGAssignRequest extends IGSimRequest {
		private final PathName fSignalName;
		private final IGStaticValue fValue;

		public IGAssignRequest(IGSimProcess aProcess, PathName aSignalName, IGStaticValue aValue) {
			super(aProcess);
			fSignalName = aSignalName;
			fValue = aValue;
		}

		@Override
		public void execute(IGSimRef aSim) throws ZamiaException {

			long dbid = fData.getSignal(fSignalName);
			if (dbid == 0) {
				logger.debug("IGAssignRequest: trying to assign value to unregistered signal: path=%s, value=%s", fSignalName, fValue);
				return;
			}

			Map<Long, IGSignalWaveformGen> waveformBySignal = getOrCreateWaveformGenHolder(dbid, fSignalName);
			IGSignalWaveformGen gen = waveformBySignal.get(dbid);
			if (gen == null) {
				throw new ZamiaException("IGAssignRequest: signal's waveform is uninitialized: path=" + fSignalName);
			}
			gen.setNextValue(fValue);

		}
	}
}
