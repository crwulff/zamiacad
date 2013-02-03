/* 
 * Copyright 2009,2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Aug 15, 2009
 */
package org.zamia.instgraph.sim.ref;

import java.math.BigInteger;
import java.util.Collection;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.interpreter.IGInterpreterObject;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.instgraph.interpreter.IGObjectDriver;
import org.zamia.util.PathName;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;


/**
 * @author Guenter Bartsch
 */

public class IGSimProcess extends IGInterpreterRuntimeEnv {

	private IGSimRef fSim;
	private PathName fPath;
	private PathName fParentPath;
	private boolean fEnableMSProof;

	public IGSimProcess(IGSimRef aSim, PathName aPath, PathName aParentPath, ZamiaProject aZPrj) {
		super(null, aZPrj);
		fSim = aSim;
		fPath = aPath;
		fParentPath = aParentPath;
	}

	@Override
	public void scheduleWakeup(BigInteger aT, SourceLocation aLocation) throws ZamiaException {
		fSim.scheduleWakeup(aT, this);
	}

	@Override
	public void scheduleWakeup(IGObjectDriver aDriver, SourceLocation aLocation) throws ZamiaException {
		if (!(aDriver instanceof IGSignalDriver)) {
			throw new ZamiaException("IGRefSim: wakeup scheduled on a non-signal driver: " + aDriver, aLocation);
		}
		((IGSignalDriver) aDriver).addListener(this);
	}

	@Override
	public void scheduleSignalChange(boolean aInertial, IGStaticValue aDelay, IGStaticValue aReject, IGStaticValue aValue, IGObjectDriver aIgObjectDriver, SourceLocation aLocation) throws ZamiaException {
		fSim.scheduleSignalChange(this, aInertial, aDelay, aReject, aValue, (IGSignalDriver) aIgObjectDriver, aLocation);
	}

	public void cancelAllWakeups(SourceLocation aLocation) throws ZamiaException {
		fSim.cancelAllWakeups(this, aLocation);
	}

	public IGStaticValue getObjectLastValue(PathName aSignalName) {
		return fSim.getLastValue(aSignalName);
	}

	public BigInteger getCurrentTime(SourceLocation aLocation) {
		return fSim.getEndTime();
	}

	public PathName getPath() {
		return fPath;
	}

	public IGSimContext pushContextFor(PathName aPath) {
		IGSimContext simContext = new IGSimContext(aPath);
		pushContext(simContext);
		return simContext;
	}

	public void enableMSProof(boolean enable) {
		fEnableMSProof = enable;
	}

	/**
	 * This method is a prerequisite for an elegant and straightforward 2-step solution of driver merging. <br/>
	 * This method resolves the conflict of drivers when multiple processes write to the same signal as a whole.
	 *
	 * <p/>
	 *
	 * For those signals driven from multiple processes as a whole (e.g. 'sigA <= "0010"', but not 'sigA(1 to 2)<= "0"')
	 * -- and for these signals only !!! -- this method adds an exact chained copy of signal's target driver to the
	 * context of the process, so that during execution this copy is used instead of the target driver
	 * (which resides one step deeper in the stack (in the architecture context) and thus will be hidden behind the copy).
	 *
	 * <p/>
	 *
	 * This way each driving process has a unique copy (functionally equivalent to the target driver), which allows us
	 * to avoid collisions of signal change events in
	 * {@link IGSignalDriver#setNextValue(org.zamia.instgraph.IGStaticValue, org.zamia.SourceLocation, IGSimProcess)}.
	 * This collision happens when different processes use the same driver (which is exactly the target driver)
	 * during execution of {@link IGSignalChangeRequest}-s and rewrite each others' nextValues they set to the
	 * {@link IGSignalDriver}. Hence the naming: make object Multiple Sources Proof.
	 *
	 * <p/>
	 * Note that there is no need for such a trick when a part of a signal is driven, for a range driver is already a
	 * unique driver. Also a chained (mapped) copy will not be created when there is only 1 'whole signal driver'.
	 * <p/>
	 * Since this method is only run during simulator initialization and hopefully for a very limited number of signals,
	 * this solution is expected to be faster than handling the conflict repetitively during interpreter code execution.
	 *
	 * @param aObject
	 * @param aSrc
	 * @throws ZamiaException
	 */
	public void makeObjectMSProof(IGObject aObject, SourceLocation aSrc) throws ZamiaException {

		if (!fEnableMSProof) {
			return;
		}

		long dbid = aObject.getDBID();

		// first process to come can use the target driver directly.
		// this way we save memory on drivers when there is only 1 'whole signal driver'.
		if (!fSim.isMSProofRequiredForProcess(dbid, fParentPath, this)) {
			fSim.registerMSProofProcess(dbid, fParentPath, this);
			return;
		}

		// we need at most 1 MSProof driver for each process
		IGSimContext currentContext = (IGSimContext) getCurrentContext();
		if (currentContext.hasDriver(dbid)) {
			return;
		}

		IGObjectDriver targetDriver = getDriver(aObject);

		IGTypeStatic type = aObject.getType().computeStaticType(this, VHDLNode.ASTErrorMode.EXCEPTION, null);
		IGObjectDriver msProofDriver = currentContext.createObject(new IGInterpreterObject(aObject, type), true, aSrc);

		msProofDriver.map(targetDriver, aSrc);
	}
	
	public void resume(Collection<IGSimPostponedProcess> postponed, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {
		super.resume(aErrorMode, aReport);
	}

}
