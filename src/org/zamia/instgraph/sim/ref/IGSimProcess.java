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

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.instgraph.interpreter.IGObjectDriver;
import org.zamia.util.PathName;


/**
 * @author Guenter Bartsch
 */

public class IGSimProcess extends IGInterpreterRuntimeEnv {

	private IGSimRef fSim;
	private PathName fPath;

	public IGSimProcess(IGSimRef aSim, PathName aPath, ZamiaProject aZPrj) {
		super(null, aZPrj);
		fSim = aSim;
		fPath = aPath;
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
		if (!(aIgObjectDriver instanceof IGSignalDriver)) {
			throw new ZamiaException("IGSimProcess: non-signal driver scheduled for change: " + aIgObjectDriver, aLocation);
		}

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
}
