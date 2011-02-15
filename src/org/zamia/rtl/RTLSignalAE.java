/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 17, 2010
 */
package org.zamia.rtl;

import org.zamia.SourceLocation;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class RTLSignalAE extends RTLItem {

	private final RTLSignal fSignal;

	private final RTLSignal fEnable;

	public RTLSignalAE(RTLSignal aSignal, RTLSignal aEnable, SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);

		fSignal = aSignal;
		fEnable = aEnable;
	}

	public RTLSignal getSignal() {
		return fSignal;
	}

	public RTLSignal getEnable() {
		return fEnable;
	}

}
