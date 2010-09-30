/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 22, 2009
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILLabel;
import org.zamia.zil.interpreter.ZILSetConnectionStmt;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 *
 */
public class ZILSequentialSetConnection extends ZILSequentialStatement {

	private boolean fDoConnect = false;

	public ZILSequentialSetConnection(boolean aDoConnect, ZILIContainer aContainer, ASTObject aSrc) {
		super(aContainer, aSrc);
		fDoConnect = aDoConnect;
	}

	@Override
	public void generateCode(ZILInterpreterCode aCode, RTLCache aCache, ZILLabel aLoopExitLabel) throws ZamiaException {
		aCode.add(new ZILSetConnectionStmt(fDoConnect, getSrc()));
	}

	@Override
	protected void inlineSubprograms(VariableRemapping aVR, ZILSequenceOfStatements aSOS, RTLCache aCache, String aReturnVarName) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException ("Sorry, not implemented yet.");
	}

	@Override
	public boolean isSynthesizable() throws ZamiaException {
		return false;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "ZILSequentialSetConnection  (doConnect=%b)", fDoConnect);
	}
	
	@Override 
	public String toString() {
		return "ZILSequentialSetConnection (doConnect="+fDoConnect+")";
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
	}

}
