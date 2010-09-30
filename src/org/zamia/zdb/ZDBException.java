/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 10, 2010
 */
package org.zamia.zdb;

import java.io.File;

/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class ZDBException extends Exception {

	private File fLockFile;

	public ZDBException(String aMessage, File aLockFile) {
		super(aMessage);
		fLockFile = aLockFile;
	}

	public File getLockFile() {
		return fLockFile;
	}

}
