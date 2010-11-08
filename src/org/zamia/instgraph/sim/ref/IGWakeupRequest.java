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
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGWakeupRequest extends IGSimRequest {

	public IGWakeupRequest(IGSimProcess aProcess) {
		super(aProcess);
	}

	@Override
	public void execute(IGSimRef aSim) throws ZamiaException {
		fProcess.resume(ASTErrorMode.EXCEPTION, null);
	}

	@Override
	public String toString() {
		return "WAKEUP REQUEST " + fProcess.toString(); 
	}
}

