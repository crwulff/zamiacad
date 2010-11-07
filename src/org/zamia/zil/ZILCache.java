/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 6, 2008
 */
package org.zamia.zil;

import java.util.HashMap;

import org.zamia.ZamiaProject;
import org.zamia.rtl.RTLGraph;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.vhdl.ast.InterfaceDeclaration;
import org.zamia.vhdl.ast.Name;
import org.zamia.vhdl.ast.Operation;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class ZILCache {

	public final static int CONST_UNKNOWN = -1;
	public final static int CONST_YES = 1;
	public final static int CONST_NO = 0;
	
	private ZamiaProject fZPrj;
	private HashMap<Operation,ZILValue> fConstantOperations;
	private HashMap<VHDLNode, ZILType> fTypeCache;
	private HashMap<VHDLNode, Object> fTransients;
	private HashMap<Operation,ZILOperation> fOperationCache;
	private HashMap<Name,ZILIObject> fNameCache;
	private HashMap<InterfaceDeclaration,ZILInterface> fInterfaceCache;
	
	public ZILCache (ZamiaProject aZPrj) {
		fZPrj = aZPrj;
		fConstantOperations = new HashMap<Operation, ZILValue>();
		fTypeCache = new HashMap<VHDLNode, ZILType>();
		fTransients = new HashMap<VHDLNode, Object>();
		fOperationCache = new HashMap<Operation, ZILOperation>();
		fNameCache = new HashMap<Name, ZILIObject>();
		fInterfaceCache = new HashMap<InterfaceDeclaration, ZILInterface>();
	}
	
	public void setIsConstant (Operation op_, ZILValue val_) {
		fConstantOperations.put(op_, val_);
	}
	
	public int isConstant (Operation op_) {
		
		if (!fConstantOperations.containsKey(op_))
			return CONST_UNKNOWN;
		
		ZILValue v = fConstantOperations.get(op_);
		if (v != null)
			return CONST_YES;
		return CONST_NO;
	}
	
	public ZILValue getConstant(Operation op_) {
		return fConstantOperations.get(op_);
	}

	public ZILType getType(VHDLNode op_) {
		return fTypeCache.get(op_);
	}
	
	public void setType(VHDLNode c_, ZILType t_) {
		fTypeCache.put(c_, t_);
	}

	public Object getTransients(VHDLNode aIO) {
		return fTransients.get(aIO);
	}

	public void setTransients(VHDLNode aIO, Object aTransients) {
		fTransients.put(aIO, aTransients);
	}

	public ZamiaProject getZamiaProject() {
		return fZPrj;
	}

	public ZILOperation getZILOperation(Operation aOperation) {
		return fOperationCache.get(aOperation);
	}

	public void setZILOperation(Operation aOperation, ZILOperation aObj) {
		fOperationCache.put(aOperation, aObj);
	}

	public RTLGraph getRTLGraph() {
		
		// FIXME FIXME FIXME
		
		return null;
	}

	public ZILIObject getZILObject(Name aName) {
		return fNameCache.get(aName);
	}

	public void setZILObject(Name aName, ZILIObject aObj) {
		fNameCache.put(aName, aObj);
	}

	public ZILInterface getZILObject(InterfaceDeclaration aInterfaceDeclaration) {
		return fInterfaceCache.get(aInterfaceDeclaration);
	}

	public void setZILObject(InterfaceDeclaration aInterfaceDeclaration, ZILInterface aInterf) {
		fInterfaceCache.put(aInterfaceDeclaration, aInterf);
	}
}
