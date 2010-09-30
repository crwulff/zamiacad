/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 9, 2008
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLGraph;
import org.zamia.rtl.RTLSignal;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILPushRefStmt;
import org.zamia.zil.synthesis.Bindings;


/**
 * 
 * @author Guenter Bartsch
 *
 */
public class ZILSignal extends ZILObject implements ZILIReferable {

	protected ZILValue fValue;
	
	public ZILSignal (String aId, ZILType aType, ZILIContainer aContainer, ASTObject aSrc) {
		super (aId, aType, aContainer, aSrc);
	}
	
	public void setInitialValue(ZILValue aValue) {
		fValue = aValue;
	}

	public ZILValue getInitialValue() {
		return fValue;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		return "signal (id="+getId()+", type="+getType()+")";
	}

	public void generateInterpreterCodeRef(boolean aInertial, boolean aHaveDelay, boolean aHaveReject, ZILInterpreterCode aCode, RTLCache aCache) throws ZamiaException {
		aCode.add(new ZILPushRefStmt(elaborate(null, aCache), aInertial, aHaveDelay, aHaveReject, getSrc()));
	}

	public RTLSignal elaborate(Bindings aLastBindings, RTLCache aCache) throws ZamiaException {
		
		RTLSignal signal = aCache.getSignal(this);
		
		if (signal == null) {
			
			RTLGraph rtlg = aCache.getGraph();
			
			signal = rtlg.createSignal(getId(), getType(), getSrc());
			
			signal.setInitialValue(fValue);
			
			aCache.setSignal(this, signal);
		}
		
		return signal;
	}

	public boolean isValidTarget() {
		return true;
	}


}
