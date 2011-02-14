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
import org.zamia.rtlng.RTLType;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class RTLNUnaryOp extends RTLNode {

	public enum UnaryOp {
		NOT, BUF, NEG, ABS
	}

	private final UnaryOp fOp;

	private final RTLType fType;

	private final RTLPort fA;

	private final RTLPort fZ;

	public RTLNUnaryOp(UnaryOp aOp, RTLType aType, RTLModule aModule, SourceLocation aLocation, ZDB aZDB) throws ZamiaException {
		super(aModule.getUniqueId(getClassName(aOp)), aModule, aLocation, aZDB);

		fOp = aOp;
		fType = aType;

		fA = createPort(RTLPort.a_str, fType, PortDir.IN, aLocation);
		fZ = createPort(RTLPort.z_str, fType, PortDir.OUT, aLocation);

	}

	private static String getClassName(UnaryOp aOp) {
		return aOp.name();
	}

	@Override
	public String getClassName() {
		return getClassName(fOp);
	}

	public UnaryOp getOp() {
		return fOp;
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

	@Override
	public String toString() {
		return "RTLNUnaryOp(op=" + fOp + ")";
	}


}
