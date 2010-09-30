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
import org.zamia.zil.ZILValue;
import org.zamia.zil.interpreter.ZILInterpreterRuntimeEnv;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class PortChangeRequest extends SimRequest {

	private PortVarWriter fPVW;

	private long fT;

	private boolean fCanceled;

	public PortChangeRequest(long aT, PortVarWriter aPVW, ZILInterpreterRuntimeEnv aRuntime) {
		super(aRuntime);

		fPVW = aPVW;
		fT = aT;
	}

	@Override
	public void execute(Simulator aSim) throws ZamiaException {
		if (fCanceled)
			return;

		fPVW.execute(aSim, fRuntime);
	}

	@Override
	public String toString() {
		return "PortChangeRequest(pvw=" + fPVW + ", canceled=" + fCanceled + ")";
	}

	public long getTime() {
		return fT;
	}

	public void setCanceled(boolean aCanceled) {
		fCanceled = aCanceled;
	}

	public ZILValue getValue() {
		return fPVW.getValue();
	}
}
