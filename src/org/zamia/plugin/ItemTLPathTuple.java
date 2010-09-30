/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 8, 2009
 */
package org.zamia.plugin;

import org.zamia.ToplevelPath;
import org.zamia.instgraph.IGItem;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class ItemTLPathTuple {
	public final IGItem fStmt;

	public final ToplevelPath fTL;

	public ItemTLPathTuple(ToplevelPath aTL, IGItem aStmt) {
		fTL = aTL;
		fStmt = aStmt;
	}
}
