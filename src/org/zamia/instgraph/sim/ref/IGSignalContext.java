package org.zamia.instgraph.sim.ref;

import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.interpreter.IGInterpreterContext;

/**
 * @author Anton Chepurov
 */
public class IGSignalContext {

	private long fSignalDBID;
	private IGInterpreterContext fSignalContext;


	public IGSignalContext(long aSignalDBID, IGInterpreterContext aSignalContext) {
		fSignalDBID = aSignalDBID;
		fSignalContext = aSignalContext;
	}

	public long getSignal() {
		return fSignalDBID;
	}

	public IGInterpreterContext getContext() {
		return fSignalContext;
	}

	public IGStaticValue getValue() {
		return fSignalContext.getObjectValue(fSignalDBID);
	}
}
