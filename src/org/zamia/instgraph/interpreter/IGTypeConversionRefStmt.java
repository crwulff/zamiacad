/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jun 7, 2009
 */
package org.zamia.instgraph.interpreter;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGType;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class IGTypeConversionRefStmt extends IGOpStmt {

	//	public enum CastOp { CONVERSION, QUALIFICATIN };
	//	
	//	private CastOp fOp;

	public IGTypeConversionRefStmt(IGType aType, SourceLocation aSrc, ZDB aZDB) {
		super(aType, aSrc, aZDB);
		//		fOp = aOp;
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		// FIXME
		throw new ZamiaException ("Sorry, not implemented yet.");
	}

	@Override
	public String toString() {
		return "TYPE CONVERSION REF type=" + getType();
	}

}
