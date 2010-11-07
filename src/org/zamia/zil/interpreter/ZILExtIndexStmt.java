/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 24, 2008
 */
package org.zamia.zil.interpreter;

import org.zamia.ZamiaException;
import org.zamia.rtl.sim.Simulator;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.ZILType;
import org.zamia.zil.ZILTypeArray;
import org.zamia.zil.ZILTypeDiscrete;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class ZILExtIndexStmt extends ZILStmt {

	// private ZILType resType;
	private ZILType subType;

	public ZILType getSubType() {
		return subType;
	}

	public ZILExtIndexStmt(ZILType type_, ZILType subType_, VHDLNode src_) {
		super(src_);
		// resType = type_;
		subType = subType_;
	}

	@Override
	public ReturnStatus execute(Simulator sim_, ZILInterpreterRuntimeEnv runtime_, ZILInterpreter comp) throws ZamiaException {
		//System.out.println("executing:   EXT INDEX");

		ZILStackFrame sfIdx = runtime_.pop();
						
		int idx = sfIdx.getInt(sim_);
		
		ZILValue op = runtime_.pop().getValue(sim_);

		ZILType t = op.getType();
		if (!(t instanceof ZILTypeArray)) {
			throw new ZamiaException ("Array type expected");
		}
		
		ZILTypeArray ta = (ZILTypeArray) t;
		ZILTypeDiscrete idxType = ta.getIndexType();
		int minIdx = idxType.getLow().getInt(getSource());
		
		ZILValue res = op.getValue(idx-minIdx);

		runtime_.push(res);
		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "EXT INDEX";
	}
}

