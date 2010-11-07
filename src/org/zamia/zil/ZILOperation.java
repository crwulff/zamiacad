/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 20, 2008
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLGraph;
import org.zamia.rtl.RTLSignal;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILInterpreterRuntimeEnv;
import org.zamia.zil.interpreter.ZILStackFrame;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public abstract class ZILOperation extends ZILObject {

	public ZILOperation(ZILType aType, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aType, aContainer, aSrc);
	}

	public abstract boolean isSynthesizable() throws ZamiaException;

	/**
	 * Inline any subprogram calls contained in this operation (and it's
	 * children)
	 * 
	 * @param vr_
	 *            TODO
	 * @param sos_
	 *            statement sequence where inlined statements can be added to
	 *            they will be executed before the operation is evaluated
	 * @param aCache
	 *            cache for type information mainly
	 * 
	 * @return clone of this operation tree with subprogram calls replaced by
	 *         variables whose values have been computed in sos_
	 */

	public abstract ZILOperation inlineSubprograms(VariableRemapping aVR, ZILSequenceOfStatements aSOS, RTLCache aCache) throws ZamiaException;

	/**
	 * replace any names representing variables by cloned names which carry
	 * information about the current content of that variable
	 * 
	 * @param vbs_
	 * @param sos_
	 * @param aCache
	 * @return cloned operation tree with names containing var information
	 * @throws ZamiaException
	 */

	public abstract ZILOperation resolveVariables(Bindings aVBs, ZILSequenceOfStatements aSOS, RTLCache aCache) throws ZamiaException;

	/**
	 * In case this Operation is synthesizable, this will synthesize the
	 * hardware implementing it.
	 * 
	 * @param aLastBindings
	 *            TODO
	 * @param aCache
	 * 
	 * @return signal carrying the result of this operation
	 * @throws ZamiaException
	 */

	public final RTLSignal elaborate(Bindings aLastBindings, RTLCache aCache) throws ZamiaException {

		//System.out.println ("Elaborating Operation "+this);

		RTLSignal result = aCache.getSignal(this);
		if (result != null) {
			//System.out.println ("  cached.");
			return result;
		}

		ZILType type = getType();

		RTLGraph rtlg = aCache.getGraph();

		result = rtlg.createUnnamedSignal(type, getSrc());
		aCache.setSignal(this, result);

		doElaborate(result, aLastBindings, aCache);
		result = result.getCurrent();

		aCache.setSignal(this, result);

		return result;
	}

	protected abstract void doElaborate(RTLSignal aResult, Bindings aLastBindings, RTLCache aCache) throws ZamiaException;

	/**
	 * Check wheter this operation sub-tree represents a clock edge e.g.
	 * clk'event and clk='1'
	 * 
	 * @return the clock if this operation sub-tree represents a clock edge,
	 *         null otherwise
	 * @throws ZamiaException
	 */

	public abstract ZILClock getClock() throws ZamiaException;

	/**
	 * In case this Object is used in a non-synthesizable process, this method
	 * will be called to generate code for the interpreter
	 * 
	 * @param aCache
	 *            TODO
	 * @param typeHint_
	 *            TODO
	 * @param nl_
	 *            NetList the surrounding process lives in
	 * @param code_
	 *            Generated code can be stored in here
	 * @throws ZamiaException
	 */
	public abstract void generateCode(ZILInterpreterCode aCode, RTLCache aCache) throws ZamiaException;

	public abstract boolean isConstant() throws ZamiaException;

	public ZILValue computeConstant() throws ZamiaException {
		ZILInterpreterCode ic = new ZILInterpreterCode(toString());
		ZILInterpreterRuntimeEnv env = new ZILInterpreterRuntimeEnv(ic, null);
		env.enterContext();

		generateCode(ic, null);

		/*
		 * execute the code in the end we should find the return
		 * value on the stack
		 */

		try {
			env.resume(null, null, 0);
			ZILStackFrame sf = env.pop();
			if (sf == null)
				return null;
			ZILValue res = sf.getLiteral();
			return res;
		} catch (ZamiaException e) {

			throw new ZamiaException("Expression not constant.", getSrc());

		}
	}
	
	public abstract int getNumOperands();
	
	public abstract ZILOperation getOperand(int aIdx);

	public abstract void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) ;

	public long getLong(VHDLNode aSrc) throws ZamiaException {
		return computeConstant().getLong(aSrc);
	}

	public int getInt(VHDLNode aSrc) throws ZamiaException {
		return computeConstant().getInt(aSrc);
	}
}
