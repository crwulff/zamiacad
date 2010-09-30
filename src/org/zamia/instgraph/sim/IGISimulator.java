/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Aug 8, 2009
 */
package org.zamia.instgraph.sim;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.zamia.SourceLocation;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.util.PathName;


/**
 * Common interface for all simulator/waveform import implementations
 * 
 * @author Guenter Bartsch
 * 
 */

public interface IGISimulator {

	/*
	 * Initialization / shutdown 
	 */

	/**
	 * Launch simulator engine on the given design or open the given file, read
	 * headers/cache data if necessary.<p/>
	 * 
	 * For plugins which set isSimulator() to false (importer plugins), a file
	 * to import will be provided (other plugins will see a null pointer here).<p/>
	 * 
	 * @param aToplevel Absolute path of the toplevel corresponding to this simulation
	 * @param aFile File to import (or null if this is a real simulator)
	 * @param aPrefix Prefix to prepend to path names when referring to the file (or null)
	 * @param aZPrj reference to the ZamiaProject this simulation is being performed on
	*
	 * @throws ZamiaException
	 */

	public void open(ToplevelPath aToplevel, File aFile, PathName aPrefix, ZamiaProject aZPrj) throws IOException, ZamiaException;

	/**
	 * Close all open files / shutdown the simulator engine
	 */

	public void shutdown();

	/*
	 * General / global information
	 * 
	 * all times are in femtoseconds (fs)
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
	 * @return simulation start time in fs
	 */
	public BigInteger getStartTime();

	/**
	 * Returns the last time offset for which simulation data is available
	 * 
	 * @return simulation end time in fs
	 */
	public BigInteger getEndTime();

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
	 * Find PathNames of signals that have path names matching the given regular
	 * expression.
	 * 
	 * @param aSearchString
	 * @param aLimit
	 *            max number of results returned
	 * @return list of matching signal PathNames
	 */

	public List<PathName> findSignalNamesRegexp(String aSearchString, int aLimit);

	/*
	 * Retrieving actual simulation values
	 */

	/**
	 * Create a new sim cursor to retrieve values
	 */
	
	public IGISimCursor createCursor();
	
	/**
	 * If available the simulator will give a source location that points to a
	 * source instruction that has likely caused the current signal transition.
	 * 
	 * @return information on what source instruction has caused the current
	 *         signal transition (if available), null otherwise
	 * @throws ZamiaException
	 */

	public SourceLocation getCurrentSourceLocation() throws ZamiaException;

	/*
	 * Interactive simulator control
	 * 
	 * these methods will not perform any function if isSimulator() is false
	 */

	/**
	 * Trace this signal
	 * 
	 * @param aSignalName
	 *            signal name to be traced
	 */
	public void trace(PathName aSignalName) throws ZamiaException;

	/**
	 * Do not trace this signal any longer
	 * 
	 * @param aSignalName
	 *            signal name not to be traced
	 */
	public void unTrace(PathName aSignalName) throws ZamiaException;

	/**
	 * Simulate for (another) aTime fs
	 * 
	 * @param aTime
	 *            time to run the simulator for in fs
	 */
	public void run(BigInteger aTime) throws ZamiaException;

	/**
	 * Assign a value to the specified signal.
	 * 
	 * @param aSignalName
	 *            signal name to be assigned
	 * @param aValue
	 *            value to be assigned to the signal
	 */
	public void assign(PathName aSignalName, IGStaticValue aValue) throws ZamiaException;

	/**
	 * Reset the simulator, next simulator steps will start from the beginning
	 */
	public void reset() throws ZamiaException;

	public void addObserver(IGISimObserver o);

	public void removeObserver(IGISimObserver o);

}
