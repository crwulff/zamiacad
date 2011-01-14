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
public class RTLNArrayIdx extends RTLNode {

	private final RTLType fType;

	private final RTLPort fA;

	private final RTLPort fZ;

	private final RTLPort fS;

	private RTLType fIdxType;

	public RTLNArrayIdx(RTLType aType, RTLType aIdxType, RTLModule aModule, SourceLocation aLocation, ZDB aZDB) throws ZamiaException {
		super(aModule.getUniqueId("IDX"), aModule, aLocation, aZDB);

		fType = aType;
		fIdxType = aIdxType;

		fA = createPort(RTLPort.a_str, fType, PortDir.IN, aLocation);
		fS = createPort(RTLPort.s_str, fIdxType, PortDir.IN, aLocation);
		fZ = createPort(RTLPort.z_str, fType, PortDir.OUT, aLocation);
	}

	@Override
	public String getClassName() {
		return "IDX";
	}

	public RTLType getType() {
		return fType;
	}

	public RTLType getIdxType() {
		return fIdxType;
	}

	public RTLPort getA() {
		return fA;
	}

	public RTLPort getZ() {
		return fZ;
	}

	public RTLPort getS() {
		return fS;
	}

	@Override
	public String toString() {
		return "RTLNArrayIdx";
	}

}
