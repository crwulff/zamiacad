/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 16, 2010
 */
package org.zamia.rtlng.nodes;

import org.zamia.SourceLocation;
import org.zamia.rtlng.RTLModule;
import org.zamia.rtlng.RTLNode;
import org.zamia.vhdl.ast.DMUID;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class RTLNInstantiation extends RTLNode {

	private String fSignature;

	private DMUID fDMUID;

	public RTLNInstantiation(String aSignature, DMUID aDMUID, String aInstanceName, RTLModule aModule, SourceLocation aLocation, ZDB aZDB) {
		super(aInstanceName, aModule, aLocation, aZDB);

		fSignature = aSignature;
		fDMUID = aDMUID;
	}

	@Override
	public String getClassName() {
		return fDMUID.getUID();
	}

	public String getSignature() {
		return fSignature;
	}

}
