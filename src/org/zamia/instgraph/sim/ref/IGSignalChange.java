package org.zamia.instgraph.sim.ref;

import org.zamia.instgraph.IGStaticValue;
import org.zamia.util.PathName;

/**
 * @author Guenter Bartsch
 */
public class IGSignalChange {

	private Long fSignalDBID;

	private IGStaticValue fValue;

	private boolean fIsEvent;

	private PathName fPath;

	public IGSignalChange(PathName aPath, Long aSignalDBID, IGStaticValue aValue, boolean aIsEvent) {
		fPath = aPath;
		fSignalDBID = aSignalDBID;
		fValue = aValue;
		fIsEvent = aIsEvent;
	}

	public PathName getPath() {
		return fPath;
	}

	public Long getSignal() {
		return fSignalDBID;
	}

	public IGStaticValue getValue() {
		return fValue;
	}

	public boolean isEvent() {
		return fIsEvent;
	}

	@Override
	public String toString() {
		return fSignalDBID + " = " + fValue + (fIsEvent ? "(EVENT)" : "");
	}
}
