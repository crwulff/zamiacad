/* 
 * Copyright 2010,2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 17, 2010
 */
package org.zamia.rtlng;

import org.zamia.ZamiaProject;
import org.zamia.rtlng.RTLType.TypeCat;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class RTLManager {

	private final RTLType fBitType;

	private final ZamiaProject fZPrj;

	private ZDB fZDB;

	public RTLManager(ZamiaProject aZPrj) {
		fZPrj = aZPrj;
		fZDB = fZPrj.getZDB();
		fBitType = new RTLType(TypeCat.BIT, null, fZPrj.getZDB());
	}

	public RTLType getBitType() {
		return fBitType;
	}

	public RTLType getBitVectorType(int aWidth) {
		
		RTLType bvt = new RTLType(TypeCat.ARRAY, null, fZDB);
		
		bvt.setArrayParams(fBitType, aWidth-1, false, 0);
		
		return bvt;
	}

	public RTLModule loadRTLModule(String aSignature) {
		// FIXME: implement

		throw new RuntimeException("Sorry, not implemented yet.");
	}
}
