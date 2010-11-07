/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on May 11, 2007
 */

package org.zamia.rtl;


import org.zamia.ZamiaException;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.ZILType;


/**
 * This is a general multiplexer for decision points in RTL-style synthesis
 * 
 * @author guenter bartsch
 *
 */


@SuppressWarnings("serial")
public class RTLTargetEMux extends RTLModule {

	private RTLPort d1, d2, e1, e2, z, ze;
	private ZILType type;

	public RTLTargetEMux (ZILType type_, RTLGraph parent_, String instanceName_, VHDLNode src_) {
		super(parent_, instanceName_, src_);

		type = type_;

		try {
			d1 = createPort("D1", type, PortDir.IN, src_);
			d2 = createPort("D2", type, PortDir.IN, src_);
			e1 = createPort("E1", type.getEnableType(), PortDir.IN, src_);
			e2 = createPort("E2", type.getEnableType(), PortDir.IN, src_);
			z = createPort(RTLPort.z_str, type, PortDir.OUT, src_);
			ze = createPort(RTLPort.ze_str, type, PortDir.OUT, src_);
		} catch (ZamiaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public String getClassName() {
		return "RTLTargetEMux";
	}

	public RTLPort getD1() {
		return d1;
	}

	public RTLPort getD2() {
		return d2;
	}

	public RTLPort getE1() {
		return e1;
	}

	public RTLPort getE2() {
		return e2;
	}

	public RTLPort getZ() {
		return z;
	}

	public RTLPort getZE() {
		return ze;
	}

}
