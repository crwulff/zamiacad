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

public class WakeupRequest extends SimRequest {

	private int fId;

	public WakeupRequest(ZILInterpreterRuntimeEnv aRuntime, int aId) {
		super(aRuntime);
		fId = aId;
	}

	@Override
	public void execute(Simulator aSim) throws ZamiaException {
		fRuntime.resume(aSim, null, fId);
	}
}

