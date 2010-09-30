/*
 * Copyright 2004,2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.util;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class Position {

	private final int fX, fY;

	public Position(int aX, int aY) {
		fX = aX;
		fY = aY;
	}

	public int getX() {
		return fX;
	}

	public int getY() {
		return fY;
	}

}
