/*
 * Copyright 2007 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Mar 12, 2007
 */

package org.zamia.rtl;
/**
 * @author guenter bartsch
 */

public class RTLSignalAE {
	
	private RTLSignal signal;
	private RTLSignal enable;

	public RTLSignalAE (RTLSignal signal_, RTLSignal enable_) {
		signal = signal_;
		enable = enable_;
	}

	public RTLSignalAE (RTLSignal signal_) {
		this (signal_, null);
	}

	public RTLSignal getEnable() {
		return enable;
	}

	public RTLSignal getSignal() {
		return signal;
	}

	public RTLSignalAE getCurrent() {
		signal = signal.getCurrent();
		enable = enable.getCurrent();
		return this;
	}
	
}
