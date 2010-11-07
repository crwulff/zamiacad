/* 
 * Copyright 2008 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 5, 2008
 */
package org.zamia.zil;

import org.zamia.vhdl.ast.VHDLNode;

/**
 * @author guenter bartsch
 */
public abstract class ZILDesignUnit extends ZILContainer {

//	private ZILResolver fResolver;
	
	public ZILDesignUnit(String aId, VHDLNode aSrc) {
		super(aId, null, null, aSrc);
	}
	
//	public void setResolver (ZILResolver aResolver) {
//		fResolver = aResolver;
//	}
//
//	public ZILResolver getResolver() {
//		return fResolver;
//	}
}
