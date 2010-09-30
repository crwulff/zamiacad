/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Mar 6, 2007
 */
package org.zamia.zil.synthesis;

import org.zamia.zil.ZILClock;
import org.zamia.zil.ZILTargetOperation;
import org.zamia.zil.ZILVariable;

/**
 * Variable bindings are inmutable because OperationName will store them when resolving variables
 *  
 * @author Guenter Bartsch
 *
 */
public class VariableBinding {
	private ZILVariable fVariable;
	private ZILTargetOperation fTargetOperation;
	private ZILClock fClock;
	
	// debugging:
	private static int counter = 0;
	protected int fCnt = 0;

	public VariableBinding (ZILVariable aVariable, ZILClock aClock, ZILTargetOperation aTargetOperation) {
		fVariable = aVariable;
		fClock = aClock;
		fTargetOperation = aTargetOperation;
		fCnt = counter++;
	}

	public ZILClock getClk() {
		return fClock;
	}
	
	public ZILVariable getVar() {
		return fVariable;
	}
	
//	public ZILValue getConstant(OperationCache cache_, SigType typeHint_, VariableRemapping vr_) throws ZamiaException {
//		if (!(to instanceof TargetOperationOp))
//			return null;
//		TargetOperationOp too = (TargetOperationOp) to;
//		
//		Operation op = too.getOp();
//		return op.getConstant(cache_, typeHint_ != null ? typeHint_ : type, vr_, false);
//	}

	public ZILTargetOperation getTO() {
		return fTargetOperation;
	}

	@Override
	public String toString() {
		return "VB (var="+fVariable+", to="+fTargetOperation+", clk="+fClock+", cnt="+fCnt+")";
	}
}

