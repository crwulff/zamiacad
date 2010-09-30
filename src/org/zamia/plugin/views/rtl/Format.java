/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.plugin.views.rtl;

/**
 * @author guenter bartsch
 */
public class Format {

	int numX, numY;

	public Format(int numX_, int numY_) {
		numX = numX_;
		numY = numY_;
	}

	public int getNumX() {
		return numX;
	}

	public int getNumY() {
		return numY;
	}

}
