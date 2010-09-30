/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 18, 2009
 */
package org.zamia;

/**
 * Wrapper for e.g. eclipse's IProgressMonitor
 * 
 * @author Guenter Bartsch
 *
 */

public interface IZamiaMonitor {

	/**
	 * Returns whether cancelation of current operation has been requested.
	 */
	public boolean isCanceled();

	public void worked(int aUnits);

	public void setTaskName(String aName);
	
}
