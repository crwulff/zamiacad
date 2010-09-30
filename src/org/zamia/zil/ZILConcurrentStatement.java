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
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.synthesis.Bindings;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public abstract class ZILConcurrentStatement extends ZILContainer {

	public ZILConcurrentStatement(String aId, ZILIContainer aContainer, ASTObject aSrc) {
		super(aId, ZILTypeVoid.getInstance(), aContainer, aSrc);
	}

	public abstract boolean isBindingsProducer(RTLCache aCache) throws ZamiaException;

	public abstract Bindings computeBindings(RTLCache aCache) throws ZamiaException;

	public abstract void elaborate(RTLCache aCache) throws ZamiaException;

}
