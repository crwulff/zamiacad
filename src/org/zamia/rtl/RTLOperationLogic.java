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
import org.zamia.vhdl.ast.OperationLogic.LogicOp;
import org.zamia.zil.ZILType;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class RTLOperationLogic extends RTLModule {

	private LogicOp op;
	private RTLPort a, b, z;
	private ZILType type;

	public RTLOperationLogic (LogicOp op_, ZILType type_, RTLGraph parent_, String instanceName_, ASTObject src_) {
		super(parent_, instanceName_, src_);

		op = op_;
		type = type_;
		
		try {
			a = createPort(RTLPort.a_str, type, PortDir.IN, src_);
			if (op != LogicOp.BUF && op != LogicOp.NOT)
				b = createPort(RTLPort.b_str, type, PortDir.IN, src_);
			z = createPort(RTLPort.z_str, type, PortDir.OUT, src_);
		} catch (ZamiaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public String getClassName() {
		return "RTLOp";
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

	public LogicOp getOp() {
		return op;
	}

	@Override
	public boolean equals(RTLModule module2_) {
		
		if (!(module2_ instanceof RTLOperationLogic))
			return false;
		
		RTLOperationLogic logic2 = (RTLOperationLogic) module2_;
		
		if (logic2.getOp() != getOp())
			return false;
		
		return super.equals(module2_);
	}
	
	

}
