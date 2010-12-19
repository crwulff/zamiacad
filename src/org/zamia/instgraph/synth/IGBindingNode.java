/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 27, 2010
 */
package org.zamia.instgraph.synth;

import org.zamia.ExceptionLogger;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.instgraph.IGObject;
import org.zamia.rtlng.RTLSignalAE;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public abstract class IGBindingNode {

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	private final IGObject fObj;

	public IGBindingNode(IGObject aObj) {
		fObj = aObj;
	}

	public IGObject getObject() {
		return fObj;
	}

	public abstract void dump(int aI);

	public abstract RTLSignalAE synthesize(IGSynth aSynth) throws ZamiaException;

}
