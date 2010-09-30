/*
 * Copyright 2007,2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.rtl.sim;

import java.util.EventListener;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

public interface SimObserver extends EventListener {
	public void notifyChanges(ISimulator aSim, long aTime);

	public void notifyReset(ISimulator aSim);
}
