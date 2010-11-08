/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 24, 2009
 */
package org.zamia.instgraph.sim.ref;

import org.zamia.ZamiaException;

/**
 * 
 * @author Guenter Bartsch
 *
 */
public abstract class IGSimRequest {

	protected IGSimProcess fProcess;

	private boolean fCanceled = false;

	public IGSimRequest(IGSimProcess aProcess) {
		fProcess = aProcess;
	}

	public abstract void execute(IGSimRef aSim) throws ZamiaException;

	public void setCanceled(boolean aCanceled) {
		fCanceled = aCanceled;
	}

	public boolean isCanceled() {
		return fCanceled;
	}
	
	public IGSimProcess getProcess() {
		return fProcess;
	}
}
