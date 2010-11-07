/* 
 * Copyright 2007-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */
package org.zamia.rtl;


import org.zamia.ZamiaException;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.ZILType;


/**
 * RTL module that performs concatenation of two operands
 * 
 * @author guenter bartsch
 */

@SuppressWarnings("serial")
public class RTLOperationConcat extends RTLModule {

	private RTLPort a, b, z;
	private ZILType aType, bType, zType;

	public RTLOperationConcat (ZILType aType_, ZILType bType_, ZILType zType_, RTLGraph parent_, String instanceName_, VHDLNode src_) {
		super(parent_, instanceName_, src_);

		aType = aType_;
		bType = bType_;
		zType = zType_;
		
		try {
			a = createPort(RTLPort.a_str, aType, PortDir.IN, src_);
			b = createPort(RTLPort.b_str, bType, PortDir.IN, src_);
			z = createPort(RTLPort.z_str, zType, PortDir.OUT, src_);
		} catch (ZamiaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public String getClassName() {
		return "RTLConcat";
	}

	public RTLPort getA() {
		return a;
	}

	public RTLPort getB() {
		return b;
	}

	public RTLPort getZ() {
		return z;
	}
}
