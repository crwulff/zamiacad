/*
 * Copyright 2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.rtl;


import org.zamia.ZamiaException;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author guenter bartsch
 *
 */
@SuppressWarnings("serial")
public class RTLLiteral extends RTLModule {

	private ZILValue v;
	private RTLPort z;
	
	public RTLLiteral(ZILValue v_, RTLGraph parent_, String instanceName_, VHDLNode src_) {
		super(parent_, instanceName_, src_);
		v = v_;

		try {
			z = createPort(RTLPort.z_str, v.getType(), PortDir.OUT, src_);
		} catch (ZamiaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public RTLPort getZ() {
		return z;
	}

	@Override
	public String getClassName() {
		return "Literal";
	}

	public ZILValue getValue() {
		return v;
	}
	
	public String toString() {
		return "\"" + v+"\" @"+hashCode();
	}
	
	@Override
	public boolean equals(RTLModule module2_) {
		
		if (!(module2_ instanceof RTLLiteral))
			return false;
		
		RTLLiteral l2 = (RTLLiteral) module2_;

		RTLSignal z = getZ().getSignal();
		RTLSignal z2 = l2.getZ().getSignal();
		if (z != z2)
			return false;
		
		ZILValue v = getValue();
		ZILValue v2 = l2.getValue();
		return v.equals(v2); 
	}

}
