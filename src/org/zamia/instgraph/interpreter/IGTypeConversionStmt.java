/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jun 7, 2009
 */
package org.zamia.instgraph.interpreter;

import java.math.BigDecimal;

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
public class IGTypeConversionStmt extends IGOpStmt {

	//	public enum CastOp { CONVERSION, QUALIFICATIN };
	//	
	//	private CastOp fOp;

	public IGTypeConversionStmt(IGType aType, SourceLocation aSrc, ZDB aZDB) {
		super(aType, aSrc, aZDB);
		//		fOp = aOp;
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		IGStackFrame sf = aRuntime.pop();

		IGStaticValue v = sf.getValue();

		IGTypeStatic rT = getType().computeStaticType(aRuntime, aErrorMode, aReport);
		if (rT == null) {
			return ReturnStatus.ERROR;
		}
		
		IGStaticValue res = v;

		switch (rT.getCat()) {
		case REAL:
		case PHYSICAL:
			IGStaticValueBuilder b = new IGStaticValueBuilder(rT, null, computeSourceLocation());
			BigDecimal d = v.getType().isDiscrete() ? new BigDecimal("" + v.getOrd()) : v.getReal();
			b.setReal(d);
			res = b.buildConstant();
			break;

		case INTEGER:
		case ENUM:
			b = new IGStaticValueBuilder(rT, null, computeSourceLocation());
			long ord = v.getType().isDiscrete() ? v.getOrd() : v.getReal().longValue();
			b.setOrd(ord);
			res = b.buildConstant();
			break;
			
		case RECORD:
		case ARRAY:
			// do nothing.
			break;
			
			
		default:
			throw new ZamiaException("Unsupported type conversion.", computeSourceLocation());
		}

		aRuntime.push(res);

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "TYPE CONVERSION type=" + getType();
	}

}
