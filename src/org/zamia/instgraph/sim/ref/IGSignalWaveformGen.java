/*
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Sep 2, 2009
 */
package org.zamia.instgraph.sim.ref;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;

import org.zamia.ExceptionLogger;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.interpreter.IGInterpreterContext;
import org.zamia.instgraph.interpreter.IGInterpreterObject;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;


/**
 * Stores current value of a signal, signal's upcoming requests (schedule) and listeners
 * 
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSignalWaveformGen {

	protected final static ZamiaLogger logger = ZamiaLogger.getInstance();

	protected final static ExceptionLogger el = ExceptionLogger.getInstance();

	private IGObject fSignal;

	private IGTypeStatic fType;

	private IGStaticValue fNextValue;

	private ArrayList<IGSignalChangeRequest> fSchedule = new ArrayList<IGSignalChangeRequest>();

	private IGInterpreterContext fContext;

	private IGInterpreterObject fIO;

	private IGSimRef fSim;

	private HashSet<IGSimProcess> fListeners = new HashSet<IGSimProcess>();

	public IGSignalWaveformGen(IGObject aSignal, IGTypeStatic aType, IGInterpreterContext aContext, IGSimRef aSim) {
		fSignal = aSignal;
		fContext = aContext;
		fNextValue = null;
		fSim = aSim;
		fType = aType;
		fIO = new IGInterpreterObject(fSignal, fType);
	}

	public IGStaticValue getValue() {
		return fNextValue;
	}

	public void commit() throws ZamiaException {
		if (fNextValue != null) {
			fContext.setObjectValue(fIO, fNextValue, null);
			
			String signalPath;
			
			if (fContext instanceof IGSimContext) {
				IGSimContext sc = (IGSimContext) fContext;
				signalPath = sc.getPath().toString() + "." + fSignal.getId();
			} else {
				signalPath = fSignal.toString();
			}
			
			logger.debug ("IGSimRef: IGSignalWaveformGen: commit(): setting %s to %s", signalPath, fNextValue);
			
			fNextValue = null;
		}
	}

	public boolean isActive() {
		return fNextValue != null;
	}

	public boolean isChanged() throws ZamiaException {
		return isChanged(getCurrentValue(), fNextValue);
	}

	public static boolean isChanged(IGStaticValue aOldValue, IGStaticValue aNewValue) throws ZamiaException {
		return aNewValue != null && !aNewValue.equalsValue(aOldValue);
	}

	private IGStaticValue getCurrentValue() throws ZamiaException {
		return ((IGSimContext) fContext).getCurrentValue(fSignal.getDBID());
	}

	public void setNextValue(IGStaticValue aValue) {
		fNextValue = aValue;
	}

	public void scheduleChange(boolean aInertial, BigInteger aReject, IGSignalChangeRequest aReq) throws ZamiaException {
		// cleanup / update schedule

		BigInteger reqT = aReq.getTime();

		BigInteger simT = fSim.getEndTime();

		int i = 0;
		while (i < fSchedule.size()) {

			IGSignalChangeRequest scr = fSchedule.get(i);

			BigInteger t = scr.getTime();

			// delete outdated entries
			if (t.compareTo(simT) < 0) {
				fSchedule.remove(i);
				continue;
			}

			// delete later requests

			if (t.compareTo(reqT) > 0) {
				scr.setCanceled(true);
			}

			// if inertial, delete events in the reject time period

			if (aInertial) {
				if (t.compareTo(aReject) > 0) {
					scr.setCanceled(true);
				}
			}

			i++;
		}
		fSchedule.add(aReq);
	}

	public void addListener(IGSimProcess aProcess) {
		fListeners.add(aProcess);
	}

	public void removeListener(IGSimProcess aProcess) {
		fListeners.remove(aProcess);
	}

	public void notifyChange() throws ZamiaException {
		// we need to iterate a copy of listeners set,
		// since the original set may be changed during iteration by resumed processes
		HashSet<IGSimProcess> oldListeners = new HashSet<IGSimProcess>(fListeners);

		for (IGSimProcess process : oldListeners) {
			process.resume(ASTErrorMode.EXCEPTION, null);
		}
	}

	@Override
	public String toString() {

		StringBuffer buf = new StringBuffer("WAVEFORM (");

		buf.append(fSchedule);

		buf.append(")");

		return buf.toString();
	}
}
