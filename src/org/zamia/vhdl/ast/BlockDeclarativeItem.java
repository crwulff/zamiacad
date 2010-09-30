/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Aug 8, 2005
 */

package org.zamia.vhdl.ast;

import java.io.PrintStream;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public abstract class BlockDeclarativeItem extends DeclarativeItem {

	public BlockDeclarativeItem (String aId, ASTObject aParent, long aLocation) {
		super(aId, aParent, aLocation);
	}
	
	public abstract void dump (PrintStream aOut);

	@Override 
	public String toString() {
		return id;
	}
}
