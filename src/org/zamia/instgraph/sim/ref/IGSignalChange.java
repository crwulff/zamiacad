package org.zamia.instgraph.sim.ref;

import org.zamia.instgraph.IGStaticValue;
import org.zamia.util.PathName;

/**
 * @author Guenter Bartsch
 */
public class IGSignalChange {

	private IGStaticValue fValue;

	private boolean fIsEvent;

	private IGSignalDriver fDriver;

	public IGSignalChange(IGStaticValue aValue, boolean aIsEvent, IGSignalDriver aDriver) {
		fValue = aValue;
		fIsEvent = aIsEvent;
		fDriver = aDriver;
	}

	public PathName getName() {
		return fDriver.getPath();
	}

	public IGStaticValue getValue() {
		return fValue;
	}

	public boolean isEvent() {
		return fIsEvent;
	}

	public IGSignalDriver getDriver() {
		return fDriver;
	}

	@Override
	public String toString() {
		return getName() + " = " + fValue + (fIsEvent ? "(EVENT)" : "");
	}
}
