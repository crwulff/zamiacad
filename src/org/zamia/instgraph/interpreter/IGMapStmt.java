/*
 * Copyright 2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.instgraph.interpreter;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.sim.ref.IGSignalDriver;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class IGMapStmt extends IGStmt {

	public IGMapStmt(SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);
	}

	@Override
	public String toString() {
		return "MAP";
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		SourceLocation src = computeSourceLocation();

		IGStackFrame actualSF = aRuntime.pop();
		IGStackFrame formalSF = aRuntime.pop();

		IGObjectDriver actual = actualSF.getObjectDriver();
		IGObjectDriver formal = formalSF.getObjectDriver();

		if (formal == null) {
			throw new ZamiaException("IGMapStmt: Invalid formal", src);
		}
		
		if (actual == null) {
			
			IGStaticValue v = actualSF.getValue();

			if (v == null) {
				String msg = "IGMapStmt: actual is uninitialized.";
				if (aErrorMode == ASTErrorMode.RETURN_NULL) {
					if (aReport != null) {
						aReport.append(msg, src);
					}
					return ReturnStatus.ERROR;
				} else {
					throw new ZamiaException (msg, src);
				}
			}

			ensureHeavyweight(formal, src);

			formal.schedule(false, null, null, v, aRuntime, src);

		} else {
			formal.map(actual, src);
		}
		
		if (IGInterpreterRuntimeEnv.dump) {
			logger.debug ("Interpreter: mapping done. result: %s", formal);
		}

		return ReturnStatus.CONTINUE;
	}

	private void ensureHeavyweight(IGObjectDriver driver, SourceLocation src) throws ZamiaException {

		if (!driver.isInputPort()) {
			return;
		}
		driver.makeHeavyweight(src);

		if (driver instanceof IGSignalDriver) {
			IGSignalDriver signalDriver = (IGSignalDriver) driver;
			// restore broken missing path, last value and current value in children drivers
			signalDriver.setPath(signalDriver.getPath());
			IGStaticValue curValue = signalDriver.getValue(src);
			IGStaticValue lastValue = signalDriver.getLastValue();
			if (lastValue != null) {
				signalDriver.setValue(lastValue, src);
			}
			if (curValue != null) {
				signalDriver.setValue(curValue, src);
			}
		}
	}

}
