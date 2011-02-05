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

	private final IGBindingNode fBindingNode;

	public IGBinding(RTLSignal aTarget, IGBindingNode aNode) {
		fTarget = aTarget;
		fBindingNode = aNode;
	}

	public IGBindingNode getBinding() {
		return fBindingNode;
	}

	public void dump() {
		logger.debug("  IGBinding target=%s", fTarget);
		if (fBindingNode != null) {
			fBindingNode.dump(6);
		}
	}

	public RTLSignal getTarget() {
		return fTarget;
	}

}
