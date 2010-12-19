/*
 * Copyright 2007,2009,2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.rtlng.sim;

import java.util.EventListener;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

public interface RTLSimObserver extends EventListener {
	
	public void notifyChanges(RTLSimulator aSim, long aTime);

	public void notifyReset(RTLSimulator aSim);
	
}
