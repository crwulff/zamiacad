/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on May 21, 2009
 */
package org.zamia.instgraph.interpreter;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGSubProgram;
import org.zamia.instgraph.IGSubProgram.IGBuiltin;
import org.zamia.instgraph.sim.ref.IGSimRef;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGCallStmt extends IGStmt {

	private long fSPDBID;

	public IGCallStmt(IGSubProgram aSP, SourceLocation aLocation, SourceLocation aOpLocation, ZDB aZDB) {
		super(aLocation, aZDB);
		fOpLine = aOpLocation.fLine;
		fOpCol = aOpLocation.fCol;
		fSPDBID = save(aSP);
	}

	public IGSubProgram getSub() {
		return (IGSubProgram) getZDB().load(fSPDBID);
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		IGSubProgram sub = getSub();

		//		int n = fSPS.getNumSubPrograms();
		//		ArrayList<IGObject> mappedInterfaces = fSPS.getSubProgram(n-1).getContainer().getInterfaces();
		//		
		//		// find matching profile
		//		for (int i = 0; i<n; i++) {
		//			
		//			IGSubProgram sub = fSPS.getSubProgram(i);
		//			
		//			if (sub.getBuiltin() == null && sub.getCode() == null) {
		//				continue;
		//			}
		//			
		//			ArrayList<IGObject> interfaces = sub.getContainer().getInterfaces();
		//			
		//			boolean matched = true;
		//			
		//			int nInterfaces = interfaces.size();
		//			for (int j = 0; j<nInterfaces; j++) {
		//				
		//				IGObject intf = interfaces.get(j);
		//				
		//				// alias parameters if necessary (overloaded subprograms)
		//
		//				if (i != n-1) {
		//					IGObject mappedIntf = mappedInterfaces.get(j);
		//					aRuntime.aliasObject(intf, mappedIntf);
		//				}
		//				
		//				IGActualType interfaceType = intf.getType().computeActualType(aRuntime);
		//				
		//				IGActualConstant v = aRuntime.getObjectValue(intf);
		//				
		//				if (v==null) {
		//					throw new ZamiaException("Interpreter: IGCallStmt: Internal error, no actual value for interface "+intf+" (dbid="+intf.getDBID()+")", computeSourceLocation());
		//				}
		//				
		//				IGActualType currentType = v.getType();
		//				
		//				if (!interfaceType.isAssignmentCompatible(currentType)) {
		//					matched = false;
		//					break;
		//				}
		//			}
		//			
		//			if (matched) {

		IGInterpreterCode code = sub.getInterpreterCode();

		if (code == null) {

			IGBuiltin bi = sub.getBuiltin();
			if (bi != null) {

				ReturnStatus status = IGBuiltinOperations.execBuiltin(sub, aRuntime, computeSourceLocation(), aErrorMode, aReport);

				fIsRelational = isRelational(bi);

				logLogicalValue(aRuntime);

				return status;

			} else {
				throw new ZamiaException("IGCallStmt: I do not have interpreter code for " + sub);
			}
		} else {

			if (IGSimRef.DEBUG) {
				logger.debug("IGCallStmt: calling " + code);
			}
			//code.dump(System.out);

			fIsRelational = sub.isFunction() && sub.getReturnType().isBool();

			return aRuntime.call(code, aErrorMode, aReport);
		}
	}

	private boolean fHasTrueOccurred, fHasFalseOccurred, fIsRelational;

	private int fOpLine, fOpCol;

	public SourceLocation getOpLocation() {

		SourceLocation loc = computeSourceLocation();
		loc.fLine = fOpLine;
		loc.fCol = fOpCol;
		return loc;
	}

	public boolean isRelational() {
		return fIsRelational;
	}

	public boolean hasTrueOccurred() {
		return fHasTrueOccurred;
	}

	public boolean hasFalseOccurred() {
		return fHasFalseOccurred;
	}

	void logLogicalValue(IGInterpreterRuntimeEnv aRuntime) throws ZamiaException {

		if (!fIsRelational) {
			return;
		}

		IGStackFrame sf = aRuntime.pop();

		IGStaticValue logicalValue = sf.getValue();

		if (logicalValue.isTrue())
			fHasTrueOccurred = fHasTrueOccurred || true;
		else
			fHasFalseOccurred = fHasFalseOccurred || true;

		aRuntime.push(logicalValue);
	}

	private static boolean isRelational(IGBuiltin builtin) {
		switch (builtin) {
			case SCALAR_EQUALS:
			case SCALAR_GREATER:
			case SCALAR_GREATEREQ:
			case SCALAR_LESS:
			case SCALAR_LESSEQ:
			case SCALAR_NEQUALS:
			case BOOL_AND:
			case BOOL_NAND:
			case BOOL_NOR:
			case BOOL_OR:
			case BOOL_XNOR:
			case BOOL_XOR:
			case BOOL_NOT:
			case BIT_NOT:
			case BIT_AND:
			case BIT_NAND:
			case BIT_NOR:
			case BIT_OR:
			case BIT_XNOR:
			case BIT_XOR:
			case ARRAY_NOT:
			case ARRAY_EQUALS:
			case ARRAY_NEQUALS:
			case ARRAY_GREATER:
			case ARRAY_GREATEREQ:
			case ARRAY_LESS:
			case ARRAY_LESSEQ:
				return true;
			default:
				return false;
		}
	}

	@Override
	public String toString() {
		return "CALL " + getSub();
	}

}
