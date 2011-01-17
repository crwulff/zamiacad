/* 
 * Copyright 2010,2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 28, 2010
 */
package org.zamia.instgraph.synth;


import org.zamia.ZamiaLogger;
import org.zamia.rtlng.RTLSignal;

/**
 * 
 * @author Guenter Bartsch
 *
 */
public class IGBinding {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private final RTLSignal fTarget;

	private final IGClock fClock;

	private final IGBindingNode fSyncBinding;

	private final IGBindingNode fASyncBinding;

	public IGBinding(RTLSignal aTarget, IGClock aClock, IGBindingNode aSyncBinding, IGBindingNode aASyncBinding) {
		fTarget = aTarget;
		fClock = aClock;
		fSyncBinding = aSyncBinding;
		fASyncBinding = aASyncBinding;
	}

	public IGClock getClock() {
		return fClock;
	}

	public IGBindingNode getSyncBinding() {
		return fSyncBinding;
	}

	public IGBindingNode getASyncBinding() {
		return fASyncBinding;
	}

	public void dump() {
		logger.debug("  IGBinding target=%s clock=%s", fTarget, fClock);
		logger.debug("    sync binding=");
		if (fSyncBinding != null) {
			fSyncBinding.dump(6);
		}

		logger.debug("    async binding=");
		if (fASyncBinding != null) {
			fASyncBinding.dump(6);
		}
	}

	public RTLSignal getTarget() {
		return fTarget;
	}


}
