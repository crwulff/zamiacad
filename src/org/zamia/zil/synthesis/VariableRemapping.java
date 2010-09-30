/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Mar 3, 2007
 */

package org.zamia.zil.synthesis;

import java.util.HashMap;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLGraph;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.ZILIContainer;
import org.zamia.zil.ZILType;
import org.zamia.zil.ZILVariable;



/**
 * 
 * @author Guenter Bartsch
 *
 */

public class VariableRemapping {
	
	private HashMap<ZILVariable,ZILVariable> fVarMap;
	
	private VariableRemapping fParent;
	
	public VariableRemapping () {
		fParent = null;
		fVarMap = new HashMap<ZILVariable, ZILVariable>();
	}
	
	public VariableRemapping(VariableRemapping aParent) {
		fParent = aParent;
		fVarMap = new HashMap<ZILVariable, ZILVariable>();
	}

	public ZILVariable get(ZILVariable aOldVariable) {
		ZILVariable res = fVarMap.get(aOldVariable);
		if (res != null)
			return res;
		if (fParent != null)
			return fParent.get(aOldVariable);
		return null;
	}
	
	public ZILVariable remap(ZILVariable aOldVariable, ZILIContainer aContainer, ASTObject aSource) {

		ZILVariable nVar = new ZILVariable(aOldVariable.getId()+"(remapped)", aOldVariable.getType(), aOldVariable.getInitialValue(), aContainer, aSource);

		fVarMap.put(aOldVariable, nVar);
		
		return nVar;
		
	}

	public String remapSignal(String prefix_, String old_, ZILType type_, RTLGraph rtlg_) throws ZamiaException {

		// FIXME: implement
		throw new RuntimeException ("Not implemented method called.");

//		String s = prefix_+"_"+old_;
//		int cnt = 0;
//		while (r.isSignalDefined(s)) {
//			cnt++;
//			s = prefix_+"_"+old_+"_"+cnt;
//		}
//		
//		//System.out.println ("variable remap: variable "+s+" created in resolver "+r);
//		
//		r.createSignal(s, type_, null, rtlg_);
//		map.put(old_, s);
//		return s;
	}

//	private HashMap<String, Value> constMap = new HashMap<String, Value>();
//	
//	public void addConstant(String id, Value c) {
//		constMap.put(id, c);
//	}
//
//	public Value getConst(String id) {
//		return constMap.get(id);
//	}
	
}
