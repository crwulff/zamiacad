/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 6, 2008
 */
package org.zamia.zil;

import org.zamia.vhdl.ast.VHDLNode;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

public interface ZILIObject {

	public VHDLNode getSrc();

	public ZILIContainer getContainer();

	public ZILType getType();

	public void dump(int aIndent) ;

	public void addAttribute(ZILAttribute aAttr) ;

	public ZILAttribute getAttribute(String aId);

	public String getId();
	
}
