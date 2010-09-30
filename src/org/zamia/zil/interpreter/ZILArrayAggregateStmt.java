/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 1, 2009
 */
package org.zamia.zil.interpreter;

import org.zamia.ZamiaException;
import org.zamia.rtl.sim.Simulator;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.ZILTypeArray;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 *
 */
@SuppressWarnings("serial")
public class ZILArrayAggregateStmt extends ZILStmt {

	private ZILTypeArray fArrayType;
	private int fNumEntries;

	public ZILArrayAggregateStmt(ZILTypeArray aArrayType, int aNumEntries, ASTObject aSrc) {
		super(aSrc);
		fArrayType = aArrayType;
		fNumEntries = aNumEntries;
	}

	@Override
	public ReturnStatus execute(Simulator aSim, ZILInterpreterRuntimeEnv aRuntime, ZILInterpreter aInterpreter) throws ZamiaException {
		
		ZILValue res = new ZILValue(fArrayType, null, getSource());
		
		for (int i = 0; i<fNumEntries; i++) {
			
			ZILStackFrame sfValue = aRuntime.pop();
			int pos = aRuntime.popInt(aSim);
			
			ZILValue value = sfValue.getValue(aSim);
			
			res.setValue(pos, value);
		}

		aRuntime.push(res);
		
		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "ARRAY AGGREGATE";
	}

}
