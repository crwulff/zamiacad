/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 22, 2008
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILLabel;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public abstract class ZILSequentialStatement extends ZILObject {

	public ZILSequentialStatement(ZILIContainer aContainer, ASTObject aSrc) {
		super(ZILTypeVoid.getInstance(), aContainer, aSrc);
	}

	// this only needs to be implemented in SequentialIf and SequentialAssignments,
	// so the default is to throw an exception if it is accidently called on anything else
	// (which should have been replaced by inlining and unrolling before)
	protected Bindings resolveVariables(Bindings aVB, ZILSequenceOfStatements aSOS, ZILClock aClk, RTLCache aCache, VariableRemapping aVR) throws ZamiaException {
		throw new ZamiaException("Internal error: resolveVariables() called on " + this);
	}

	// this only needs to be implemented in SequentialIf and SequentialAssignments,
	// so the default is to throw an exception if it is accidently called on anything else
	// (which should have been replaced by inlining and unrolling before)
	public Bindings computeBindings(ZILClock aClock, RTLCache aCache, VariableRemapping aVR) throws ZamiaException {
		throw new ZamiaException("Internal error: computeBindings() called on " + this);
	}

	protected abstract void inlineSubprograms(VariableRemapping aVR, ZILSequenceOfStatements aSOS, RTLCache aCache, String aReturnVarName) throws ZamiaException;

	public abstract void generateCode(ZILInterpreterCode aCode, RTLCache aCache, ZILLabel aLoopExitLabel) throws ZamiaException;

	public abstract boolean isSynthesizable() throws ZamiaException;

	public abstract void computeReadSignals(HashSetArray<ZILSignal> aReadSignals);

}
