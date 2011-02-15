/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 18, 2010
 */
package org.zamia.rtl.nodes;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLNode;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLValue;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class RTLNLiteral extends RTLNode {

	private final RTLValue fValue;

	private final RTLPort fZ;

	public RTLNLiteral(RTLValue aValue, RTLModule aModule, SourceLocation aLocation, ZDB aZDB) throws ZamiaException {
		super(aModule.getUniqueId(aValue.toString()), aModule, aLocation, aZDB);

		fValue = aValue;

		fZ = createPort(RTLPort.z_str, fValue.getType(), PortDir.OUT, aLocation);

	}

	@Override
	public String getClassName() {
		return "Literal";
	}

	public RTLPort getZ() {
		return fZ;
	}

	public RTLValue getValue() {
		return fValue;
	}
	
	@Override
	public String toString() {
		return "RTLLiteral(value="+fValue+")";
	}

}
