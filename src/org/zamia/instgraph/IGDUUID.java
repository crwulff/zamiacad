/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 10, 2009
 */
package org.zamia.instgraph;

import org.zamia.SourceLocation;
import org.zamia.vhdl.ast.DUUID;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class IGDUUID extends IGContainerItem {

	private final DUUID fDUUID;
	
	public IGDUUID(DUUID aDUUID, SourceLocation aSrc, ZDB aZDB) {
		super (aDUUID.getUID(), aSrc, aZDB);
		fDUUID = aDUUID;
	}
	
	public DUUID getDUUID() {
		return fDUUID;
	}
	
	@Override
	public int getNumChildren() {
		return 0;
	}

	@Override
	public IGItem getChild(int aIdx) {
		return null;
	}

}
