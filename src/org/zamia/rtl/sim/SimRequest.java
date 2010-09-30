/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 24, 2009
 */
package org.zamia.rtl.sim;

import org.zamia.ZamiaException;
import org.zamia.zil.interpreter.ZILInterpreterRuntimeEnv;


/**
 * 
 * @author Guenter Bartsch
 *
 */
public abstract class SimRequest {

	protected ZILInterpreterRuntimeEnv fRuntime;

	public SimRequest (ZILInterpreterRuntimeEnv aRuntime) {
		fRuntime = aRuntime;
	}
	
	public abstract void execute (Simulator aSim) throws ZamiaException;
	
}
