/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.instgraph.interpreter;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperationLiteral;
import org.zamia.instgraph.IGStaticValue;
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
public class IGPushStmt extends IGStmt {

	private long fObjectDBID;

	private long fTypeDBID;

	private IGOperationLiteral fLiteral;

	private boolean fTruthValue;

	private IGStaticValue fStaticValue;

	public IGPushStmt(IGObject aObject, SourceLocation aSrc, ZDB aZDB) {
		super(aSrc, aZDB);
		fObjectDBID = save(aObject);
	}

	public IGPushStmt(IGType aType, SourceLocation aSrc, ZDB aZDB) {
		super(aSrc, aZDB);
		fTypeDBID = save(aType);
	}

	public IGPushStmt(IGOperationLiteral aLiteral, SourceLocation aSrc, ZDB aZDB) {
		super(aSrc, aZDB);
		fLiteral = aLiteral;
		IGType t = fLiteral.getType();
		if (t == null) {
			logger.error ("IGPushStmt: Sanity check failed. Literal doesn't have a type (anymore). %s", aLiteral);
		}
	}

	public IGPushStmt(boolean aTruthValue, SourceLocation aSrc, ZDB aZDB) {
		super(aSrc, aZDB);
		fTruthValue = aTruthValue;
	}

	public IGPushStmt(IGStaticValue aStaticValue, SourceLocation aSrc, ZDB aZDB) {
		super(aSrc, aZDB);
		fStaticValue = aStaticValue;
	}

	private IGObject getObject() {
		if (fObjectDBID == 0)
			return null;
		return (IGObject) getZDB().load(fObjectDBID);
	}

	private IGType getType() {
		if (fTypeDBID == 0)
			return null;
		return (IGType) getZDB().load(fTypeDBID);
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
		IGObject obj = getObject();
		if (obj != null) {

			IGObjectDriver driver = aRuntime.getDriver(obj, aErrorMode, aReport);
			
			if (driver == null) {
				
				ZamiaException e = new ZamiaException ("IGPushStmt: Failed to find driver for "+obj, computeSourceLocation());
				if (aErrorMode == ASTErrorMode.RETURN_NULL) {
					if (aReport != null) {
						aReport.append(e);
					}
					return ReturnStatus.ERROR;
				} else {
					throw e;
				}
			}
			
			aRuntime.push(driver);

		} else if (fTypeDBID != 0) {
			IGTypeStatic type = getType().computeStaticType(aRuntime, aErrorMode, aReport);
			if (type == null) {
				return ReturnStatus.ERROR;
			}
			aRuntime.push(type);
		} else if (fLiteral != null) {
			IGStaticValue c = fLiteral.computeStaticValue(aRuntime);
			aRuntime.push(c);
		} else if (fStaticValue != null) {
			aRuntime.push(fStaticValue);
		} else {
			IGStaticValue c = new IGStaticValue(fTruthValue, getZDB());
			aRuntime.push(c);
		}
		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		if (getObject() != null) {
			return "PUSH OBJECT " + getObject() + " DBID " + getObject().getDBID();
		} else if (getType() != null) {
			return "PUSH TYPE " + getType();
		} else if (fLiteral != null) {
			return "PUSH LITERAL " + fLiteral;
		} else if (fStaticValue != null) {
			return "PUSH STATIC VALUE " + fStaticValue;
		}
		return "PUSH " + fTruthValue;
	}
}
