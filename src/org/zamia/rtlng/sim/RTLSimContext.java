/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 17, 2010
 */
package org.zamia.rtlng.sim;

import java.util.HashMap;

import org.zamia.rtlng.RTLModule;
import org.zamia.rtlng.RTLPort;
import org.zamia.rtlng.RTLSignal;
import org.zamia.util.PathName;

/**
 * Basically we have one RTLSimContext per RTLModule _instantiation_ during a sim run
 * 
 * 
 * 
 * @author Guenter Bartsch
 *
 */

public class RTLSimContext {

	private final PathName fPath;

	private final RTLModule fModule;

	private final HashMap<String, RTLSignalSimAnnotation> fSIs;

	private final HashMap<RTLPort, RTLPortSimAnnotation> fPIs;

	public RTLSimContext(RTLModule aModule, PathName aPath, RTLSimulator aSim) {

		fPath = aPath;
		fModule = aModule;

		int n = fModule.getNumSignals();
		fSIs = new HashMap<String, RTLSignalSimAnnotation>(n);

		n = fModule.getNumPorts();
		fPIs = new HashMap<RTLPort, RTLPortSimAnnotation>(n);

	}

	void addSignal(RTLSignal aSignal, RTLSignalSimAnnotation aSI) {
		fSIs.put(aSignal.getId(), aSI);
	}

	void addPort(RTLPort aPort, RTLPortSimAnnotation aPI) {
		fPIs.put(aPort, aPI);
	}

	public PathName getPath() {
		return fPath;
	}

	public RTLSignalSimAnnotation findSignalSimAnnotation(RTLSignal aSignal) {
		return fSIs.get(aSignal.getId());
	}

	public RTLPortSimAnnotation findPortSimAnnotation(RTLPort aPort) {
		return fPIs.get(aPort);
	}

	public RTLModule getModule() {
		return fModule;
	}

}
