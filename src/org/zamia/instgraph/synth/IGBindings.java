/*
 * Copyright 2005-2011 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.instgraph.synth;

import org.zamia.ZamiaLogger;
import org.zamia.rtlng.RTLSignal;
import org.zamia.util.HashMapArray;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class IGBindings {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private final HashMapArray<RTLSignal, IGBinding> fBindings;

	public IGBindings() {
		fBindings = new HashMapArray<RTLSignal, IGBinding>();
	}

	public int getNumBindings() {
		return fBindings.size();
	}

	public IGBinding getBinding(int aIdx) {
		return fBindings.get(aIdx);
	}

	public IGBinding getBinding(RTLSignal aSignal) {
		return fBindings.get(aSignal);
	}

	public void setBinding(RTLSignal aSignal, IGBinding aBinding) {
		fBindings.put(aSignal, aBinding);
	}

	public void dumpBindings() {

		logger.debug("Bindings dump for Bindings@" + hashCode());

		int n = getNumBindings();
		for (int i = 0; i < n; i++) {
			IGBinding b = getBinding(i);
			b.dump();
		}
	}

}