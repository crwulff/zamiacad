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
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.ZILTypeArray;
import org.zamia.zil.ZILTypeDiscrete;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class ZILExtRangeStmt extends ZILStmt {

	private ZILTypeArray resType;

	public ZILExtRangeStmt(ZILTypeArray resType_, ASTObject src_) {
		super(src_);
		resType = resType_;
	}

	@Override
	public ReturnStatus execute(Simulator sim_, ZILInterpreterRuntimeEnv runtime_, ZILInterpreter comp) throws ZamiaException {
		//System.out.println("executing:   EXT RANGE");

		boolean ascending = runtime_.pop().getBool(sim_);
		ZILStackFrame sfLeft = runtime_.pop();
		ZILStackFrame sfRight = runtime_.pop();
						
		int l = sfLeft.getInt(sim_);
		int r = sfRight.getInt(sim_);
		
		ZILValue op = runtime_.pop().getValue(sim_);

		ZILTypeDiscrete idxType = resType.getIndexType();
		int minIdx = idxType.getLow().getInt(getSource());
		ZILValue res = new ZILValue (resType, null, null);
		
		if (ascending) {
			for (int i = l; i<=r; i++) {
				res.setValue(i,op.getValue(i-minIdx));
			}
		} else {
			for (int i = r; i<=l; i++) {
				res.setValue(i,op.getValue(i-minIdx));
			}
		}

		runtime_.push(res);
		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "EXT RANGE";
	}

}
