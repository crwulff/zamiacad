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
public class RTLNRegister extends RTLNode {

	private final RTLPort fASyncData, fASyncEnable, fSyncData, fSyncEnable, fZ, fClk;

	private final RTLType fType;

	public RTLNRegister(RTLType aType, RTLModule aModule, SourceLocation aLocation, ZDB aZDB) throws ZamiaException {
		super(aModule.getUniqueId("reg"), aModule, aLocation, aZDB);

		RTLManager rtlm = getRTLManager();
		
		fType = aType;

		fASyncData = createPort("ad", aType, PortDir.IN, aLocation);
		fASyncEnable = createPort("ae", aType.computeEnableType(), PortDir.IN, aLocation);
		fSyncData = createPort(RTLPort.d_str, aType, PortDir.IN, aLocation);
		fSyncEnable = createPort(RTLPort.e_str, aType.computeEnableType(), PortDir.IN, aLocation);
		fClk = createPort(RTLPort.cp_str, rtlm.getBitType(), PortDir.IN, aLocation);
		fZ = createPort(RTLPort.z_str, aType, PortDir.OUT, aLocation);

	}

	@Override
	public String getClassName() {
		return "Register";
	}

	public RTLPort getZ() {
		return fZ;
	}

	public RTLPort getASyncData() {
		return fASyncData;
	}

	public RTLPort getASyncEnable() {
		return fASyncEnable;
	}

	public RTLPort getSyncData() {
		return fSyncData;
	}

	public RTLPort getSyncEnable() {
		return fSyncEnable;
	}

	public RTLPort getClk() {
		return fClk;
	}

	public RTLType getType() {
		return fType;
	}

	@Override
	public String toString() {
		return "RTLRegister";
	}

}
