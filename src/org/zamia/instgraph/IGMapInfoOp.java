/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 22, 2010
 */
package org.zamia.instgraph;

import java.io.Serializable;

/**
 * Simple, static operations for map info
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class IGMapInfoOp implements Serializable {

	public enum MapInfoOp {
		INDEX, RECORDFIELD
	}

	private final MapInfoOp fOp;

	private final int fMin, fMax;

	private final String fFieldId;

	public IGMapInfoOp(MapInfoOp aOp, int aMin, int aMax, String aFieldId) {
		fOp = aOp;
		fMin = aMin;
		fMax = aMax;
		fFieldId = aFieldId;
	}

	public MapInfoOp getOp() {
		return fOp;
	}

	public int getMin() {
		return fMin;
	}

	public int getMax() {
		return fMax;
	}

	public String getFieldId() {
		return fFieldId;
	}

}
