/* 
 * Copyright 2009,2010 by the authors indicated in the @author tags. 
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

		IGStackFrame sf = aRuntime.pop();

		IGTypeStatic t = getType().computeStaticType(aRuntime, ASTErrorMode.EXCEPTION, null);
		
		SourceLocation location = computeSourceLocation();

		IGObjectDriver driver = sf.getObjectDriver();
		if (driver != null) {
			
			IGObjectDriver aliasDriver = driver.createAliasDriver(t, location);

			if (IGInterpreterRuntimeEnv.dump) {
				logger.debug ("Interpreter: result of driver alias op: %s", aliasDriver);
			}

			aRuntime.push(aliasDriver);

		} else {

			IGStaticValue v2 = sf.getValue();
			if (v2 == null) {
				ZamiaException e = new ZamiaException("Interpreter: Uninitialized value detected.", location);
				
				if (aErrorMode == ASTErrorMode.EXCEPTION) {
					throw e;
				}
				
				if (aReport != null) {
					aReport.append(e);
				}
				return ReturnStatus.ERROR;
			}

			IGTypeStatic t2 = v2.getStaticType();

			IGStaticValueBuilder b = new IGStaticValueBuilder(t, null, location);

			IGTypeStatic idx2 = t2.getStaticIndexType(location);

			int off2 = (int) idx2.getStaticLow(location).getOrd();
			int card2 = (int) idx2.computeCardinality(location);
			int off1 = b.getArrayOffset();
			for (int i = off2; i < off2 + card2; i++) {
				b.set(i - off2 + off1, v2.getValue(i, location), location);
			}

			aRuntime.push(b.buildConstant());
		}

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "ALIAS " + getType();
	}
}
