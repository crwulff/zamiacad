/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 18, 2010
 */
package org.zamia.rtlng.nodes;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.rtlng.RTLModule;
import org.zamia.rtlng.RTLNode;
import org.zamia.rtlng.RTLPort;
import org.zamia.rtlng.RTLPort.PortDir;
import org.zamia.rtlng.RTLValue;
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
