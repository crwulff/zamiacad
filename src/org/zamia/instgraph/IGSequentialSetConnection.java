/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on May 23, 2009
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
public class IGSequentialSetConnection extends IGSequentialStatement {

	public IGSequentialSetConnection(boolean aDoConnect, String aId, SourceLocation aSrc, ZDB aZDB) {
		super(aId, aSrc, aZDB);
	}

	@Override
	public void computeAccessedItems(IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {
	}

	@Override
	public void generateCode(IGInterpreterCode aCode) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Sorry, not implemented yet.");
	}

	@Override
	public IGItem getChild(int aIdx) {
		return null;
	}

	@Override
	public int getNumChildren() {
		return 0;
	}

}
