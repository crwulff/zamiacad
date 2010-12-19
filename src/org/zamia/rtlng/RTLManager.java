/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 17, 2010
 */
package org.zamia.rtlng;

import java.util.HashMap;

import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.rtlng.RTLType.TypeCat;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class RTLManager {

	private final RTLType fBitType;

	private final ZamiaProject fZPrj;

	private HashMap<Long, RTLType> fTypeCache;

	public RTLManager(ZamiaProject aZPrj) {

		fZPrj = aZPrj;

		fBitType = new RTLType(TypeCat.BIT, null, fZPrj.getZDB());
		clear();
	}

	public void clear() {
		fTypeCache = new HashMap<Long, RTLType>();
	}

	public RTLType getBitType() {
		return fBitType;
	}

	public RTLType getCachedType(IGTypeStatic aType) {
		
		long dbid = aType.getDBID();
		
		return fTypeCache.get(dbid);
	}

	public void setCachedType(IGTypeStatic aType, RTLType aT) {
		fTypeCache.put(aType.getDBID(), aT);
	}

	public RTLModule loadRTLModule(String aSignature) {
		// FIXME: implement
		
		throw new RuntimeException ("Sorry, not implemented yet.");
	}
}
