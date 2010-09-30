/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Oct 10, 2009
 */
package org.zamia.instgraph.sim.annotations;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.instgraph.interpreter.IGObjectWriter;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGAnnotationsEnv extends IGInterpreterRuntimeEnv {

	public IGAnnotationsEnv(IGInterpreterCode aCode, ZamiaProject aZPrj) {
		super(aCode, aZPrj);
	}

	public void scheduleSignalChange(boolean aInertial, IGStaticValue aDelay, IGStaticValue aReject, IGObjectWriter aObjectWriter, SourceLocation aLocation) throws ZamiaException {
		aObjectWriter.execute(this);
	}

}
