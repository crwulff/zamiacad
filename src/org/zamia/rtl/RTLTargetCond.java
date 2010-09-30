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
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.ZILType;


/**
 * Multiple and-gates representing conditions for RTL-style synthesis
 * 
 * @author guenter bartsch
 * 
 */

@SuppressWarnings("serial")
public class RTLTargetCond extends RTLModule {

	private RTLPort d, e, z, ze, c;
	private ZILType type;

	public RTLTargetCond(ZILType t_, RTLGraph parent_, String instanceName_,
			ASTObject src_) {
		super(parent_, instanceName_, src_);

		type = t_;
		try {
			d = createPort("D", type, PortDir.IN, src_);
			e = createPort(RTLPort.e_str, type, PortDir.IN, src_);
			z = createPort(RTLPort.z_str, type, PortDir.OUT, src_);
			ze = createPort(RTLPort.ze_str, type, PortDir.OUT, src_);
			c = createPort("C", ZILType.bit, PortDir.IN, src_);

		} catch (ZamiaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String getClassName() {
		return "RTLTargetCond";
	}

	public RTLPort getC() {
		return c;
	}

	public RTLPort getD() {
		return d;
	}

	public RTLPort getE() {
		return e;
	}

	public RTLPort getZ() {
		return z;
	}

	public RTLPort getZE() {
		return ze;
	}

}
