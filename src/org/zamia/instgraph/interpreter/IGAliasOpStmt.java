/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jun 24, 2009
 */
package org.zamia.instgraph.interpreter;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGStaticValueBuilder;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 *
 */
@SuppressWarnings("serial")
public class IGAliasOpStmt extends IGOpStmt {

	public IGAliasOpStmt(IGType aResultType, SourceLocation aLocation, ZDB aZDB) {
		super(aResultType, aLocation, aZDB);
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
		
		IGStaticValue v2 = aRuntime.pop().getValue();
		
		IGTypeStatic t2 = v2.getStaticType();
		IGTypeStatic t = getType().computeStaticType(aRuntime, ASTErrorMode.EXCEPTION, null);
		
		IGStaticValueBuilder b = new IGStaticValueBuilder(t, null, computeSourceLocation());

		IGTypeStatic idx2 = t2.getStaticIndexType(computeSourceLocation());
		
		int off2 = (int) idx2.getStaticLow(computeSourceLocation()).getOrd();
		int card2 = (int) idx2.computeCardinality(computeSourceLocation());
		int off1 = b.getArrayOffset();
		for (int i = off2; i<off2+card2; i++) {
			b.set(i-off2+off1, v2.getValue(i, computeSourceLocation()), computeSourceLocation());
		}

		aRuntime.push(b.buildConstant());
		
		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "ALIAS "+getType();
	}
}
