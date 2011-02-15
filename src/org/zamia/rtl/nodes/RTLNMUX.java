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
import org.zamia.rtl.RTLType;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class RTLNMUX extends RTLNode {

	private final RTLType fType;

	private final RTLPort fA;

	private final RTLPort fZ;

	private final RTLPort fB;

	private final RTLPort fS;

	public RTLNMUX(RTLType aType, RTLModule aModule, SourceLocation aLocation, ZDB aZDB) throws ZamiaException {
		super(aModule.getUniqueId("MUX"), aModule, aLocation, aZDB);

		fType = aType;

		fA = createPort(RTLPort.a_str, fType, PortDir.IN, aLocation);
		fB = createPort(RTLPort.b_str, fType, PortDir.IN, aLocation);
		fS = createPort(RTLPort.s_str, fType, PortDir.IN, aLocation);
		fZ = createPort(RTLPort.z_str, fType, PortDir.OUT, aLocation);

	}

	@Override
	public String getClassName() {
		return "MUX";
	}

	public RTLType getType() {
		return fType;
	}

	public RTLPort getA() {
		return fA;
	}

	public RTLPort getZ() {
		return fZ;
	}

	public RTLPort getB() {
		return fB;
	}

	public RTLPort getS() {
		return fS;
	}

	@Override
	public String toString() {
		return "RTLNMUX";
	}

}
