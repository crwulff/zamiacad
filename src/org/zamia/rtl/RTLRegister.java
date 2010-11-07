/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.rtl;


import org.zamia.ZamiaException;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.ZILType;


/**
 * This is a general register for RTL-style synthesis
 * 
 * @author guenter bartsch
 *
 */

@SuppressWarnings("serial")
public class RTLRegister extends RTLModule {

	private ZILType type;
	private RTLPort asyncData, asyncEnable, syncData, syncEnable, q, clk;
	public static final String REGISTER_NAME = new String("Reg");
	public String getClassName() {
		return REGISTER_NAME;
	}

	public RTLRegister(RTLGraph parent_, ZILType t_, String instanceName_, VHDLNode src_) throws ZamiaException {
		super(parent_, instanceName_, src_);

		type = t_;
		asyncData = createPort("aD", type, PortDir.IN, src_);
		asyncEnable = createPort("aE", type, PortDir.IN, src_);
		syncData = createPort(RTLPort.d_str, type, PortDir.IN, src_);
		syncEnable = createPort(RTLPort.e_str, type, PortDir.IN, src_);
		q = createPort(RTLPort.q_str, type, PortDir.OUT, src_);
		clk = createPort(RTLPort.cp_str, ZILType.bit, PortDir.IN, src_);
	}

	public RTLPort getASyncData() {
		return asyncData;
	}
	public RTLPort getSyncData() {
		return syncData;
	}
	public RTLPort getASyncEnable() {
		return asyncEnable;
	}
	public RTLPort getSyncEnable() {
		return syncEnable;
	}

	public RTLPort getZ() {
		return q;
	}
	public RTLPort getClk() {
		return clk;
	}
	
	public ZILType getType() {
		return type;
	}
	
	@Override
	public boolean isPortMandatory(RTLPort port_) {
		return port_ != asyncData && port_ != asyncEnable && port_ != syncData && port_ != syncEnable && port_ != clk;
	}
}
