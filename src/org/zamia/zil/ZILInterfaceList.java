/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 9, 2008
 */
package org.zamia.zil;

import org.zamia.util.HashMapArray;
import org.zamia.vhdl.ast.ASTObject;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class ZILInterfaceList extends ZILObject {

	private HashMapArray<String,ZILInterface> fInterfaces;
	
	public ZILInterfaceList(ZILIContainer aContainer, ASTObject aSrc) {
		super (ZILTypeVoid.getInstance(), aContainer, aSrc);
		fInterfaces = new HashMapArray<String, ZILInterface>();
	}

	public void add(ZILInterface aInterface) {
		fInterfaces.put(aInterface.getId(), aInterface);
	}
	
	public ZILInterface resolve(String aId) {
		return fInterfaces.get(aId);
	}
	
	public int getNumInterfaces() {
		return fInterfaces.size();
	}
	
	public ZILInterface getInterface(int aIdx) {
		return fInterfaces.get(aIdx);
	}

	public ZILInterface getInterface(String aId) {
		return fInterfaces.get(aId);
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("InterfaceList (");
		
		int n = fInterfaces.size();
		for (int i = 0; i<n; i++) {
			ZILInterface interf = fInterfaces.get(i);
			
			buf.append(interf);
			if (i<n-1) {
				buf.append(", ");
			}
		}

		buf.append(")");
		return buf.toString();
	}


}

