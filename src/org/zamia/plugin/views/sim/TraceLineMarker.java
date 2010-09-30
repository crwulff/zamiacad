/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 3, 2009
 */
package org.zamia.plugin.views.sim;

import java.math.BigInteger;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class TraceLineMarker {

	private BigInteger fTime;

	private String fLabel;

	private int fWidth = 320, fX = Integer.MIN_VALUE;

	public TraceLineMarker(BigInteger aTime, String aLabel) {
		fTime = aTime;
		fLabel = aLabel;
	}

	public BigInteger getTime() {
		return fTime;
	}

	public void setTime(BigInteger aTime) {
		fTime = aTime;
	}

	public String getLabel() {
		return fLabel;
	}

	public void setLabel(String aLabel) {
		fLabel = aLabel;
	}

	public void setWidth(int aWidth) {
		fWidth = aWidth;
	}

	public int getWidth() {
		return fWidth;
	}

	public int getX() {
		return fX;
	}

	public void setX(int aX) {
		fX = aX;
	}

}
