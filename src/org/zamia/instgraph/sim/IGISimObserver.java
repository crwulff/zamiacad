/*
 * Copyright 2007,2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.instgraph.sim;

import java.math.BigInteger;
import java.util.EventListener;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

public interface IGISimObserver extends EventListener {
	public void notifyChanges(IGISimulator aSim, BigInteger aTime);

	public void notifyReset(IGISimulator aSim);
}
