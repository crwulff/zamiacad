/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 16, 2009
 */
package org.zamia.rtl.sim;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.rtl.RTLGraph;
import org.zamia.util.PathName;
import org.zamia.zil.ZILType;
import org.zamia.zil.ZILValue;


/**
 * Common interface for all simulator/waveform import implementations
 * 
 * @author Guenter Bartsch
 * 
 */
public interface ISimulator {

	/*
	 * Initialization / shutdown 
	 */

	/**
	 * This method will only be called for plugins which
	 * set isSimulator() to false (importer plugins)
	 * 
	 * open the given file, read headers/cache data if necessary
	 * @throws ZamiaException 
	 */
	
	public void open(File aFile) throws IOException, ZamiaException;

	/**
	 * This method will only be called for plugins which
	 * set isSimulator() to true (simulator engines)
	 * 
	 * launch simulator engine on the given design
	 */
	
	public void open(RTLGraph aRTLGraph) throws ZamiaException;
	
	
	/**
	 * Close all open files / shutdown the simulator engine
	 */
	
	public void shutdown();

	/*
	 * General / global information
	 * 
	 * all times are in picoseconds (ps)
	 */

	/**
	 * Returns the interface version the simulator supports
	 * 
	 * @return interface version
	 */
	public int getInterfaceVersion();

	/**
	 * Returns the first time offset for which simulation data is available
	 * 
	 * @return simulation start time in ps
	 */
	public long getStartTime();

	/**
	 * Returns the last time offset for which simulation data is available
	 * 
	 * @return simulation end time in ps
	 */
	public long getEndTime();

	/**
	 * Returns what timescale was used for the simulation
	 * 
	 * @return minimum step duration in ps
	 */
	public long getTimeScale();

	/**
	 * Returns true if this is an interactive simulator (that supports the
	 * trace/run/assign and reset methods) or false otherwise (typically a file
	 * reader for waveform data)
	 * 
	 * @return true if this is an interactive simulator, false otherwise
	 */
	public boolean isSimulator();

	/*
	 * Information about available signal traces
	 */

	/**
	 * Get the number of available signals.
	 * 
	 * @param aParentPath
	 */
	public int getNumSignals();

	/**
	 * Get the path name of signal #aIdx
	 * @param aIdx
	 * @return
	 */
	
	public PathName getSignalName(int aIdx);

	/**
	 * returns the type of the given signal
	 * 
	 * @param aPath
	 *            pathname of the signal
	 * @return the type of the signal
	 */
	public ZILType getSignalType(int aIdx);

	/**
	 * Find signal index corresponding to the given signal path
	 * @param aPath
	 * @return signal index if found, -1 otherwise
	 */
	public int findSignalIdx(PathName aPath);

	/**
	 * Find indices of signals that have path names matching the
	 * given regular expression.
	 * 
	 * @param aSearchString
	 * @param aLimit max number of results returned
	 * @return list of matching signal indices
	 */
	
	public List<Integer> findSignalIdxRegexp(String aSearchString, int aLimit);
	
	/*
	 * Retrieving actual simulation values
	 */

	/**
	 * Get simulation value of signal number aIdx at time offset aTimeOffset. If
	 * available the simulator will also supply a source location that points to
	 * a source instruction that has likely caused this signal transition.
	 * 
	 * This function can be slow since it may require the simulation data
	 * provider to do random file access to retrieve the requested value. For
	 * speed it is recommended you call this function once to select a signal
	 * and a start time offset and then use getNextValue() to retrieve
	 * information on subsequent signal transitions.
	 * 
	 * @param aTimeOffset
	 *            time offset the value is being requested for
	 * @param aSignalIdx
	 *            index of signal the value is being requested for
	 * @param aRetValue
	 *            this ZILValue will contain the simulation value requested when
	 *            the method returns
	 * @param aSourceLocation
	 *            this source location may contain information on what source
	 *            instruction has caused the signal transition (if available)
	 * @throws ZamiaException
	 */
	public void getValue(long aTimeOffset, int aSignalIdx, ZILValue aRetValue, SourceLocation aSourceLocation) throws ZamiaException;

	/**
	 * Retrieve value and source location information (if available) on the next
	 * logged transition of the current signal relative to the current time
	 * offset.
	 * 
	 * To select a different signal or request information on an arbitrary time
	 * offset, use getValue().
	 * 
	 * @param aTimeLimit
	 *            if > 0: search no further than this time limit
	 * @param aRetValue
	 *            this ZILValue will contain the simulation value requested when
	 *            the method returns
	 * @param aSourceLocation
	 *            this source location may contain information on what source
	 *            instruction has caused the signal transition (if available)
	 * @return time offset the reported transition happend, in ps or sim end
	 *         time, if no further events have been logged
	 */

	public long getNextValue(long aTimeLimit, ZILValue aRetValue, SourceLocation aSourceLocation) throws ZamiaException;

	/**
	 * Retrieve value and source location information (if available) on the previous
	 * logged transition of the current signal relative to the current time
	 * offset.
	 * 
	 * To select a different signal or request information on an arbitrary time
	 * offset, use getValue().
	 * 
	 * @param aRetValue
	 *            this ZILValue will contain the simulation value requested when
	 *            the method returns
	 * @param aSourceLocation
	 *            this source location may contain information on what source
	 *            instruction has caused the signal transition (if available)
	 * @return time offset the reported transition happend, in ps or sim end
	 *         time, if no further events have been logged
	 */

	public long getPrevValue(long aTimeLimit, ZILValue aRetValue, SourceLocation aSourceLocation) throws ZamiaException;

	/*
	 * Interactive simulator control
	 * 
	 * these methods will not perform any function if isSimulator() is false
	 */

	/**
	 * Trace this signal
	 * 
	 * @param aSignalIdx
	 *            signal idx to be traced
	 */
	public void trace(int aSignalIdx) throws ZamiaException;

	/**
	 * Do not trace this signal any longer
	 * 
	 * @param aSignalIdx
	 *            signal idx not to be traced
	 */
	public void unTrace(int aSignalIdx) throws ZamiaException;

	/**
	 * Simulate for (another) aTime ps
	 * 
	 * @param aTime
	 *            time to run the simulator for in ps
	 */
	public void run(long aTime) throws ZamiaException;

	/**
	 * Assign a value to the specified signal.
	 * 
	 * @param aSignalIdx
	 *            signal idx to be assigned
	 * @param aValue
	 *            value to be assigned to the signal
	 */
	public void assign(int aSignalIdx, ZILValue aValue) throws ZamiaException;

	/**
	 * Reset the simulator, next simulator steps will start from the beginning
	 */
	public void reset() throws ZamiaException;

	public void addObserver(SimObserver o);

	public void removeObserver(SimObserver o);


}
