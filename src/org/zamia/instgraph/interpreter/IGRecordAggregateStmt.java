/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on May 21, 2009
 */
package org.zamia.instgraph.interpreter;

import java.util.ArrayList;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGRecordField;
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
public class IGRecordAggregateStmt extends IGOpStmt {

	private ArrayList<String> fFields;

	public IGRecordAggregateStmt(ArrayList<String> aFields, IGType aResType, SourceLocation aLocation, ZDB aZDB) {
		super(aResType, aLocation, aZDB);
		fFields = aFields;
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		IGTypeStatic rT = getType().computeStaticType(aRuntime, aErrorMode, aReport);
		if (rT == null) {
			return ReturnStatus.ERROR;
		}
		
		IGStaticValueBuilder builder = new IGStaticValueBuilder(rT, null, computeSourceLocation());

		int n = fFields.size();

		for (int i = n - 1; i >= 0; i--) {
			IGStaticValue v = aRuntime.pop().getValue();

			IGRecordField rf = getType().findRecordField(fFields.get(i), computeSourceLocation());

			builder.set(rf, v, computeSourceLocation());
		}

		IGStaticValue c = builder.buildConstant();

		aRuntime.push(c);

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "RECORD AGGREGATE (#fields=" + fFields.size() + ", resType=" + getType() + ")";
	}

}
