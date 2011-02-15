/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 18, 2010
 */
package org.zamia.rtl.sim;

import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLValue;
import org.zamia.util.PathName;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class RTLPortSimAnnotation {

	private boolean fDriving = false;

	private final PathName fPath;

	private final RTLSimContext fContext;
	
	private final RTLPort fPort;

	private RTLValue fValue, fDeltaValue;

	public RTLPortSimAnnotation(PathName aPath, RTLPort aPort, RTLSimContext aContext) {
		fPath = aPath;
		fPort = aPort;
		fContext = aContext;
	}

	public void setDriving(boolean driving) {
		fDriving = driving;
	}

	public boolean isDriving() {
		return fDriving;
	}

	public PathName getPath() {
		return fPath;
	}

	public RTLPort getPort() {
		return fPort;
	}

	public void setValue(RTLValue value) {
		fValue = value;
	}

	public RTLValue getValue() {
		return fValue;
	}

	public void setDeltaValue(RTLValue deltaValue) {
		fDeltaValue = deltaValue;
	}

	public RTLValue getDeltaValue() {
		return fDeltaValue;
	}

	public RTLSimContext getContext() {
		return fContext;
	}

	@Override
	public String toString() {
		return fPath.toString();
	}
}
