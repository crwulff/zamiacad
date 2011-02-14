/* 
 * Copyright 2009,2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Aug 15, 2009
 */
package org.zamia.instgraph.sim.ref;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.instgraph.interpreter.IGObjectDriver;
import org.zamia.util.PathName;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;


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

	@Override
	public void setObjectValue(IGObject aObject, IGStaticValue aValue, SourceLocation aLocation) throws ZamiaException {
		if (aObject.getCat() == IGObject.IGObjectCat.FILE) {
			writeToFile(aObject, aValue, aLocation);
		} else {
			super.setObjectValue(aObject, aValue, aLocation);
		}
	}

	private void writeToFile(IGObject aObject, IGStaticValue aValue, SourceLocation aLocation) throws ZamiaException {

		IGStaticValue filePathValue = aObject.getInitialValue().computeStaticValue(this, null, null);
		String filePath = filePathValue.getId();

		File file = new File(getZPrj().getBasePath() + File.separator + filePath);

		BufferedWriter writer = null;
		try {

			writer = new BufferedWriter(new FileWriter(file, true));

			writer.append(aValue.toString()); // todo: toString() ???
			writer.newLine();

		} catch (FileNotFoundException e) {
			throw new ZamiaException("File to write to is not found: " + file.getAbsolutePath(), aLocation);
		} catch (IOException e) {
			throw new ZamiaException("Error while writing to file " + file.getAbsolutePath() + ":\n" + e.getMessage(), aLocation);
		} finally {
			try {
				if (writer != null) { // always close the writer
					writer.close();
				}
			} catch (IOException e) {
			}
		}

	}

	public IGStaticValue getObjectLastValue(PathName aSignalName) {
		return fSim.getLastValue(aSignalName);
	}

	public BigInteger getCurrentTime(SourceLocation aLocation) throws ZamiaException {
		return fSim.getEndTime();
	}

	public PathName getPath() {
		return fPath;
	}
}
