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
import org.zamia.instgraph.interpreter.IGStmt.ReturnStatus;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * modified fall 2012 by Valentin Tihhomirov
 * 
 */
@SuppressWarnings("serial")
public class IGPushStmt {

	
	public static class LITERAL extends IGStmt {

		private IGOperationLiteral fLiteral;

		public LITERAL(IGOperationLiteral aLiteral, SourceLocation aSrc, ZDB aZDB) {
			super(aSrc, aZDB);
			fLiteral = aLiteral;
			IGType t = fLiteral.getType();
			if (t == null) {
				logger.error ("IGPushStmt: Sanity check failed. Literal doesn't have a type (anymore). %s", aLiteral);
			}
		}

		@Override
		public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
			IGStaticValue c = fLiteral.computeStaticValue(aRuntime);
			aRuntime.push(c);
			return ReturnStatus.CONTINUE;
		}

		@Override
		public String toString() {
			return "PUSH LITERAL " + fLiteral;
		}
	}

	public static class BOOLEAN extends IGStmt {
		private boolean fTruthValue;

		public BOOLEAN(boolean aTruthValue, SourceLocation aSrc, ZDB aZDB) {
			super(aSrc, aZDB);
			fTruthValue = aTruthValue;
		}

		public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
			IGStaticValue c = new IGStaticValue.INNER_BOOLEAN_DUPLICATE(fTruthValue, getZDB());;
			aRuntime.push(c);
			return ReturnStatus.CONTINUE;
		}

		@Override
		public String toString() {
			return "PUSH " + fTruthValue;
		}
		
	}
	
	public static class STATIC_VALUE extends IGStmt {
		private IGStaticValue fStaticValue;

		public STATIC_VALUE(IGStaticValue aStaticValue, SourceLocation aSrc, ZDB aZDB) {
			super(aSrc, aZDB);
			fStaticValue = aStaticValue;
		}

		public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
			aRuntime.push(fStaticValue);
			return ReturnStatus.CONTINUE;
		}

		@Override
		public String toString() {
			return "PUSH STATIC VALUE " + fStaticValue;
		}
		
	}
	
	//TODO: share code with Type push statement
	public static class OBJECT extends IGStmt {

		private long fObjDBID;

		public OBJECT(IGObject aObj, SourceLocation aSrc, ZDB aZDB) {
			super(aSrc, aZDB);
			fObjDBID = save(aObj);
		}

		private IGObject getObject() {
			if (fObjDBID == 0)
				return null;
			return (IGObject) getZDB().load(fObjDBID);
		}
		
		@Override
		public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
			IGObject obj = getObject();
			//if (obj != null) {

			IGObjectDriver driver = aRuntime.getDriver(obj);
				
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
			//}
				
			return ReturnStatus.CONTINUE;
		}

		@Override
		public String toString() {
			return "PUSH OBJECT " + getObject();
		}
	}
	
	public static class TYPE extends IGStmt {

		private long fTypeDBID;

		public TYPE(IGType aType, SourceLocation aSrc, ZDB aZDB) {
			super(aSrc, aZDB);
			fTypeDBID = save(aType);
		}

		private IGType getType() {
			if (fTypeDBID == 0)
				return null;
			return (IGType) getZDB().load(fTypeDBID);
		}

		@Override
		public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
			//if (fTypeDBID != 0) {
				IGTypeStatic type = getType().computeStaticType(aRuntime, aErrorMode, aReport);
				if (type == null) {
					return ReturnStatus.ERROR;
				}
				aRuntime.push(type);
//			} 
			return ReturnStatus.CONTINUE;
		}

		@Override
		public String toString() {
			return "PUSH TYPE " + getType();
		}
	}	

	
}
