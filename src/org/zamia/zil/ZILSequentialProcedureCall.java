/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 12, 2009
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILLabel;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 *
 */
public class ZILSequentialProcedureCall extends ZILSequentialStatement {

	private ZILSubProgramInvocation fInvocation;

	public ZILSequentialProcedureCall(ZILSubProgramInvocation aInvocation, ZILIContainer aContainer, ASTObject aSrc) {
		super(aContainer, aSrc);
		fInvocation = aInvocation;
	}

	public void dump(int aIndent) {
		fInvocation.dump(aIndent);
	}

	@Override
	public Bindings computeBindings(ZILClock clock_, RTLCache cache_, VariableRemapping vr_) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Internal Error: unimplemented method called on " + this);
	}

	@Override
	public void generateCode(ZILInterpreterCode code_, RTLCache cache_, ZILLabel loopExitLabel_) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Internal Error: unimplemented method called on " + this);
//
//		name.getNameResolver(cache_);
//		IResolvableObject obj = name.resolve(null, cache_);
//
//		if (!(obj instanceof SubProgramSet)) {
//			throw new ZamiaException("Not a subprogram: " + obj, name);
//		}
//
//		SubProgramSet subs = (SubProgramSet) obj;
//
//		SubInvocation si = name.findSubProgram(subs, cache_, null, false);
//		if (si == null) {
//			logger.error("WARNING: " + this + " resolved to a SubProgramSet: " + obj
//					+ " but parameters didn't match. Report follows:");
//
//			si = name.findSubProgram(subs, cache_, null, true);
//
//			logger.error("WARNING: " + this + " resolved to a SubProgramSet: " + obj
//					+ " but parameters didn't match. Report ends.");
//
//			throw new ZamiaException("No matching sub program found.", this);
//		}
//
//		int n = si.params.size();
//
//		for (int i = 0; i < n; i++) {
//			ArrayList<AssociationElement> elements = si.params.get(i);
//
//			InterfaceDeclaration intf = si.sub.getInterface(i);
//			Operation op = null;
//			if (elements.size() == 0) {
//				op = intf.getValue();
//				if (op == null) {
//					throw new ZamiaException("No value given for interface " + intf, this);
//				}
//			} else {
//				if (elements.size() != 1) {
//					// FIXME: implement
//					throw new ZamiaException("Sorry, only one association per interface supported yet.", this);
//				}
//
//				op = elements.get(0).getActualPart();
//			}
//
//			SigType formalType = intf.getType().elaborate(cache_, null);
//
//			SigType actualType = op.getType(cache_, formalType, null, false);
//
//			if (!formalType.isCompatible(actualType))
//				throw new ZamiaException("Type mismatch.", op.getLocation());
//
//			op.generateCode(code_, cache_, actualType);
//		}
//
//		code_.add(new ZILCallStmt(si.sub, si.sub.generateInterpreterCode(si.params, cache_), this));
	}

	@Override
	protected void inlineSubprograms(VariableRemapping vr_, ZILSequenceOfStatements sos_, RTLCache cache_, String returnVarName_) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Internal Error: unimplemented method called on " + this);
	}


	@Override
	public boolean isSynthesizable() throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("Internal Error: unimplemented method called on " + this);
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		fInvocation.computeReadSignals(aReadSignals);
	}



}
