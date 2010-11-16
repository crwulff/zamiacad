/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Aug 15, 2009
 */
package org.zamia.instgraph.sim.ref;

import org.zamia.ZamiaException;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.interpreter.IGInterpreterContext;
import org.zamia.util.PathName;

/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class IGSimContext extends IGInterpreterContext {

	private IGSimRef fSim;
	private PathName fPath;

	public IGSimContext(IGSimRef aSim, PathName aPath) {
		fSim = aSim;
		fPath = aPath;
	}

	public IGStaticValue getCurrentValue(long aDBID) throws ZamiaException {
		return super.getObjectValue(aDBID);
	}

	@Override
	public IGStaticValue getObjectValue(long aDBID) throws ZamiaException {
		// For signals -- get delta
		// For others -- current value
		IGStaticValue v = fSim.getSignalNextValue(aDBID, fPath);
		if (v != null) {
			return v;
		}
		
		return getCurrentValue(aDBID);
	}

	
	public PathName getPath() {
		return fPath;
	}
}
