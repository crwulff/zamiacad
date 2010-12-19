/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on May 19, 2009
 */
package org.zamia.instgraph;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 *
 */
@SuppressWarnings("serial")
public abstract class IGSequentialStatement extends IGContainerItem {

	public IGSequentialStatement(String aId, SourceLocation aSrc, ZDB aZDB) {
		super(aId, aSrc, aZDB);
	}

	public abstract void computeAccessedItems(IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems);

	public abstract void generateCode(IGInterpreterCode aCode) throws ZamiaException;
	
	public void dump(int aIndent) {
		logger.debug(aIndent, "Unknown stmt: %s", this);
		
	}

}
