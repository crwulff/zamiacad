/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 15, 2008
 */
package org.zamia.zil;

import org.zamia.vhdl.ast.VHDLNode;

/**
 * 
 * @author Guenter Bartsch
 *
 */
public class ZILComponent extends ZILObject{

	private ZILInterfaceList fInterfaces;
	private ZILInterfaceList fGenerics;

	public ZILComponent(String aId, ZILIContainer aContainer, VHDLNode aSrc) {
		super (aId, null, aContainer, aSrc);
	}

	public void setInterfaces(ZILInterfaceList aZIL) {
		fInterfaces = aZIL;
	}
	
	public void setGenerics(ZILInterfaceList aZIL) {
		fGenerics = aZIL;
	}

	public ZILInterfaceList getInterfaces() {
		return fInterfaces;
	}

	public ZILInterfaceList getGenerics() {
		return fGenerics;
	}
	
	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		return "Component (id='"+getId()+"', type="+getType()+", interfaces="+getInterfaces()+")";
	}
}
