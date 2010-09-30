/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Dec 16, 2005
 */

package org.zamia.vhdl.ast;

import org.zamia.SourceFile;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public abstract class PrimaryUnit extends DesignUnit {

	public PrimaryUnit (Context aContext, String aId, SourceFile aSF, long aLinCol, String aLibId, ZDB aZDB) {
		super (aContext, aId, aSF, aLinCol, aLibId, aZDB);
	}

}
