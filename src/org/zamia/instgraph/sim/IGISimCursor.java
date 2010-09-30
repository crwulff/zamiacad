/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.instgraph.sim;

import java.math.BigInteger;

import org.zamia.ZamiaException;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.util.PathName;

/**
 * @author guenter bartsch
 */

public interface IGISimCursor {

	/**
	 * Set simulation cursor to the given signal and to the a time offset 
	 * of the next transition after aTimeOffset.
	 * 
	 * This function can be slow since it may require the simulation data
	 * provider to do random file access when you want to retrieve values for the new offset.
	 * 
	 * For speed it is recommended you call this function once to select a signal
	 * and a start time offset and then use gotoNextTransition() to retrieve
	 * information on subsequent signal transitions.
	 * 
	 * @param aSignalName
	 *            PathName of signal the value is being requested for
	 * @param aTimeOffset
	 *            time offset the value is being requested for
	 *            
	 * @return true if a corresponding transition was found, false otherwise (unknown signal)
	 * 
	 * @throws ZamiaException
	 */

	public boolean gotoTransition(PathName aSignalName, BigInteger aTimeOffset) throws ZamiaException;

	/**
	 * Place simulation cursor at the next logged transition of the current signal 
	 * relative to the current time offset.
	 * 
	 * To select a different signal or request information on an arbitrary time
	 * offset, use gotoTransition().
	 * 
	 * @param aTimeLimit: if != null: search no further than this time limit
	 * @return time offset the reported transition happend, in fs or sim end
	 *         time, if no further events have been logged
	 */

	public BigInteger gotoNextTransition(BigInteger aTimeLimit) throws ZamiaException;

	/**
	 * Place simulation cursor at the previous logged transition of the current signal 
	 * relative to the current time offset.
	 * 
	 * To select a different signal or request information on an arbitrary time
	 * offset, use gotoTransition().
	 * 
	 * @param aTimeLimit if != null: search no further than this time limit
	 * @return time offset the reported transition happened, in fs or sim start
	 *         time, if no further events have been logged
	 */

	public BigInteger gotoPreviousTransition(BigInteger aTimeLimit) throws ZamiaException;

	/**
	 * Get the current simulation time offset
	 * 
	 * @return current simulation time offset
	 * @throws ZamiaException
	 */
	
	public BigInteger getCurrentTime() throws ZamiaException;
	
	/**
	 * Get simulation value of current signal at the current time.
	 * 
	 * @return Simulation value requested if available, null otherwise
	 * 
	 * @throws ZamiaException
	 */
	public IGStaticValue getCurrentValue() throws ZamiaException;

	/**
	 * Call this method to free resources allocated to this sim cursor when 
	 * it is no longer needed
	 * 
	 */
	public void dispose();
	
}
