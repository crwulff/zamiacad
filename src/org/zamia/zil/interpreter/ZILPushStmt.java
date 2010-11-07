/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.zil.interpreter;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.sim.Simulator;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.ZILIReferable;
import org.zamia.zil.ZILType;
import org.zamia.zil.ZILValue;
import org.zamia.zil.ZILVariable;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class ZILPushStmt extends ZILStmt {

	private ZILIReferable fObject;

	private ZILType fType;

	private RTLSignal fSignal;

	public ZILPushStmt(ZILIReferable aObject, VHDLNode aSrc) {
		super(aSrc);
		fObject = aObject;
	}

	public ZILPushStmt(RTLSignal aSignal, VHDLNode aSrc) {
		super(aSrc);
		fSignal = aSignal;
	}

	public ZILPushStmt(ZILType aType, VHDLNode aSrc) {
		super(aSrc);
		fType = aType;
	}

	@Override
	public ReturnStatus execute(Simulator aSim, ZILInterpreterRuntimeEnv aRuntime, ZILInterpreter aInterpreter) throws ZamiaException {
		if (fObject != null) {

			if (fObject instanceof ZILValue) {
				aRuntime.push(new ZILStackFrame((ZILValue) fObject));

			} else {

				// System.out.println("executing:   PUSH " + vid);
				ZILValue iv = aRuntime.getObjectValue(fObject);
				if (iv == null)
					throw new ZamiaException("Object not found: " + fObject);
				aRuntime.push(new ZILStackFrame(iv));
			}
		} else if (fSignal != null) {
			// System.out.println("executing:   PUSH " + p);
			aRuntime.push(new ZILStackFrame(aSim.getValue(fSignal)));
		} else if (fType != null) {
			// System.out.println("executing:   PUSH " + fo);
			aRuntime.push(new ZILStackFrame(fType));
		}
		return ReturnStatus.CONTINUE;
	}

	@Override
	public void wire(ZILInterpreter aInterpreter) throws ZamiaException {
		if (fSignal != null) {
			aInterpreter.connectToSignal(fSignal, getSource());
		}
	}

	@Override
	public String toString() {
		if (fObject != null) {
			return "PUSH OBJECT " + fObject;
		} else if (fSignal != null) {
			return "PUSH SIGNAL " + fSignal;
		} else if (fType != null) {
			return "PUSH " + fType;
		}
		return "PUSH ???";
	}
}
