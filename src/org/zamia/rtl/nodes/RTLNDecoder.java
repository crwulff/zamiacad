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
import org.zamia.rtl.RTLManager;
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
public class RTLNDecoder extends RTLNode {

	private final RTLPort fA, fZ;

	private final RTLType fInputType, fOutputType;

	private final int fOutputWidth;

	private final int fInputWidth;

	public RTLNDecoder(int aOutputWidth, RTLModule aModule, SourceLocation aLocation, ZDB aZDB) throws ZamiaException {
		super(aModule.getUniqueId("DEC"), aModule, aLocation, aZDB);

		RTLManager rtlm = getRTLManager();

		fInputWidth = (int) Math.ceil(Math.log(aOutputWidth) / Math.log(2));
		fOutputWidth = aOutputWidth;

		fInputType = rtlm.getBitVectorType(fInputWidth);
		fOutputType = rtlm.getBitVectorType(fOutputWidth);

		fA = createPort(RTLPort.a_str, fInputType, PortDir.IN, aLocation);
		fZ = createPort(RTLPort.z_str, fOutputType, PortDir.OUT, aLocation);

	}

	@Override
	public String getClassName() {
		return "Decoder";
	}

	public RTLPort getZ() {
		return fZ;
	}

	public RTLPort getA() {
		return fA;
	}

	public RTLType getInputType() {
		return fInputType;
	}

	@Override
	public String toString() {
		return "RTLDecoder (" + fInputWidth + ":" + fOutputWidth + ")";
	}

}
