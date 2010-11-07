/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 6, 2008
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.interpreter.ZILInterpreterCode;


/**
 * 
 * @author Guenter Bartsch
 *
 */
public class ZILSubProgram extends ZILContainer {
	
	public enum ZILBI {
		NOW
	};

	private ZILBI fBuiltin;

	private ZILInterfaceList fParameters;
	private ZILSequenceOfStatements fCode;

	public ZILSubProgram (String aId, ZILInterfaceList aParameters, ZILType aReturnType, ZILIContainer aContainer, VHDLNode aSrc) {
		super (aId, aReturnType, aContainer, aSrc);
		
		fParameters = aParameters;
		
	}

	public void setBuiltin(ZILBI aBuiltin) {
		fBuiltin = aBuiltin;
	}
	
	@Override
	public ZILIObject resolve(String aId) {
		
		ZILInterface interf = fParameters.resolve(aId);
		
		if (interf != null)
			return interf;
		
		return super.resolve(aId);
	}

	public void setCode(ZILSequenceOfStatements aCode) {
		fCode = aCode;
	}
	
	public ZILSequenceOfStatements getCode() {
		return fCode;
	}

	public ZILInterfaceList getParameters() {
		return fParameters;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "SubProgram (id=%s, returnType=%s, interfaces=%s) {", getId(), getType(), fParameters);

		if (fCode != null) {
			fCode.dump(aIndent+2);
		}
		
		logger.debug(aIndent, "}");
		
	}
	
	@Override
	public String toString() {
		return "SubProgram (id="+getId()+", returnType="+getType()+", interfaces="+fParameters+")"; 
	}

	public int getNumParameters() {
		if (fParameters == null)
			return 0;
		return fParameters.getNumInterfaces();
	}

	public ZILInterpreterCode generateCode(RTLCache aCache) throws ZamiaException {
		
		ZILInterpreterCode code;
		
		if (aCache != null) {
			code = aCache.getCode(this);
			if (code != null) {
				return code;
			}
		}
		
		code= new ZILInterpreterCode(getId());
		
		fCode.generateCode(code, aCache, null);

		aCache.setCode(this, code);
		
		return code;
	}

	public ZILBI getBuiltin() {
		return fBuiltin;
	}

//	public SubProgram getSubProgram() {
//		return (SubProgram) getSrc();
//	}

	/*
	 * the actual return type of a function may depend on its parameters that's
	 * why we have to go through all the code and compute all actual variable
	 * types just to figure out the actual return type
	 */

//	public SigType getReturnType(ArrayList<ArrayList<AssociationElement>> params_, OperationCache cache_,
//			VariableRemapping vr_) throws ZamiaException {
//
//		OperationCache cache = new OperationCache(cache_.getRTLGraph(), cache_);
//
//		if (returnType == null)
//			throw new ZamiaException("Error, subprogram " + id + " is not a function");
//
//		SigType rt = returnType.elaborateAsType(null, null, cache);
//
//		if (rt == null)
//			throw new ZamiaException("Error: cannot elaborate return type of subprogram "+this);
//
//		// check for built-in functions
//
//		if (id.equals("CONV_INTEGER")) {
//			return SigType.intType;
//		} else if (id.equals("TO_UNSIGNED") || id.equals("TO_SIGNED")) {
//			Value size = params_.get(1).get(0).getActualPart().getConstant(cache, SigType.intType, null, false);
//			if (size == null)
//				throw new ZamiaException("Constant size expression expected here.", this);
//
//			int n = size.getInt(this);
//
//			SigTypeArray at = (SigTypeArray) rt;
//
//			SigType t = at.createSubType(n - 1, 0, false, this);
//
//			return t;
//		} else if (id.equals("CONV_STD_LOGIC_VECTOR")) {
//
//			Value size = params_.get(1).get(0).getActualPart().getConstant(cache, SigType.intType, null, false);
//			if (size == null)
//				throw new ZamiaException("Constant size expression expected here.", this);
//
//			int n = size.getInt(this);
//
//			SigTypeArray at = (SigTypeArray) rt;
//
//			SigType t = at.createSubType(n - 1, 0, false, this);
//
//			return t;
//		}
//
//		if (!rt.isOpen() || code == null) {
//			return rt;
//		}
//
//		// maybe this computes a constant?
//
//		Value c = computeConstant(params_, cache_, rt, vr_);
//		if (c != null) {
//			return c.getType();
//		}
//
//		Resolver resolver = new Resolver(parent.getResolver(cache), this);
//		cache_.setResolver(this, resolver);
//
//		VariableRemapping vr = null;
//		if (vr_ == null) {
//			vr = new VariableRemapping(resolver);
//		} else {
//			vr = new VariableRemapping(vr_);
//		}
//
//		/*
//		 * inline the parameters
//		 */
//
//		int n = params_.size();
//		for (int i = 0; i < n; i++) {
//			ArrayList<AssociationElement> elements = params_.get(i);
//			InterfaceDeclaration formal = interfaces.get(i);
//
//			Operation actual = null;
//
//			if (elements.size() == 0) {
//				actual = formal.getValue();
//				if (actual == null) {
//					throw new ZamiaException("No actual and no default value given for interface " + formal, this);
//				}
//			} else {
//				// FIXME: implement
//				if (elements.size() != 1) {
//					throw new ZamiaException(
//							"Sorry, multiple explicit parameter assignments are not supported yet for subprograms.",
//							this);
//				}
//				actual = elements.get(0).getActualPart();
//			}
//			
//			formal.inline(vr, actual, null, cache);
//		}
//
//		/*
//		 * remap the local variables
//		 */
//
//		n = declarations.size();
//		for (int i = 0; i < n; i++) {
//			BlockDeclarativeItem decl = declarations.get(i);
//			decl.elaborate(cache_, vr);
//			decl.inline(vr, cache);
//		}
//
//		SigType res = code.getReturnType(rt, cache, vr);
//
//		if (res == null)
//			return rt;
//
//		return res;
//	}

	// private void elaborateDeclarations(OperationCache cache_) throws
	// ZamiaException {
	// resolver = new Resolver(parent.getResolver());
	// //resolver.elaborate(storage_, context);
	//		
	// int n = declarations.size();
	// for (int i = 0; i < n; i++) {
	// BlockDeclarativeItem decl = declarations.get(i);
	// decl.elaborate(cache_);
	// }
	// }

	// private void elaborateParams (ArrayList<Operation> params_,
	// OperationCache cache_) throws ZamiaException {
	// int n = params_.size();
	// for (int i = 0; i<n; i++) {
	// Operation actual = params_.get(i);
	// InterfaceDeclaration formal = interfaces.get(i);
	// formal.elaborateAsParameter(resolver, actual, cache_);
	// }
	// }

	// private Signal elaborateConvInteger(Operation operation, NetList nl_,
	// OperationCache cache_) throws ZamiaException {
	//		
	// operation.computeType(SigType.intType, cache_);
	// Signal s = operation.elaborate(cache_);
	//		
	// return s.convert(SigType.intType, this);
	// }

//	public String inline(VariableRemapping vr_, ArrayList<ArrayList<AssociationElement>> params_,
//			SequenceOfStatements sos_, OperationCache cache_) throws ZamiaException {
//
//		OperationCache cache = new OperationCache(cache_.getRTLGraph(), cache_);
//
//		VariableRemapping vr = new VariableRemapping(vr_);
//
//		/*
//		 * inline the parameters
//		 */
//
//		int n = params_.size();
//		for (int i = 0; i < n; i++) {
//			ArrayList<AssociationElement> elements = params_.get(i);
//
//			// FIXME: implement
//			if (elements.size() != 1) {
//				throw new ZamiaException(
//						"Sorry, multiple explicit parameter assignments are not supported yet for subprograms.", this);
//			}
//			Operation actual = elements.get(0).getActualPart();
//			InterfaceDeclaration formal = interfaces.get(i);
//			actual = actual.inlineSubprograms(vr_, sos_, cache_);
//			actual.setParent(sos_);
//			formal.inline(vr, actual, sos_, cache_);
//		}
//
//		/*
//		 * remap the local variables
//		 */
//
//		inlineDeclarations(vr, cache);
//
//		/*
//		 * add fake return var
//		 */
//
//		String returnVarName = null;
//		if (returnType != null) {
//			returnVarName = vr.remap(getId(), RETURN_VAR_NAME, getReturnType(params_, cache, vr_), location);
//		}
//
//		// check for built-in functions
//		if (id.equals("CONV_INTEGER") || id.equals("TO_INTEGER")) {
//
//			String id = vr.get(interfaces.get(0).getId());
//			Operation exp = new OperationTypeCast(new Name(id, null, location), null, SigType.intType, sos_,
//					location);
//			Target target = new Target(new Name(returnVarName, null, location), null, location);
//			SequentialVariableAssignment sva = new SequentialVariableAssignment(target, exp, null, location);
//			sos_.add(sva);
//		} else if (id.equals("CONV_STD_LOGIC_VECTOR") || id.equals("TO_UNSIGNED") || id.equals("TO_SIGNED")) {
//			String id = vr.get(interfaces.get(0).getId());
//
//			SigType resType = getReturnType(params_, cache, vr_);
//
//			Operation exp = new OperationTypeCast(new Name(id, null, location), null, resType, sos_, location);
//			Target target = new Target(new Name(returnVarName, null, location), null, location);
//			SequentialVariableAssignment sva = new SequentialVariableAssignment(target, exp, null, location);
//			sos_.add(sva);
//		} else {
//
//			/*
//			 * inline the code
//			 */
//
//			n = code.getNumStatements();
//			for (int i = 0; i < n; i++) {
//
//				SequentialStatement stmt = code.getStatement(i);
//
//				stmt.inlineSubprograms(vr, sos_, cache, returnVarName);
//			}
//		}
//
//		return returnVarName;
//	}
//
//	private void inlineDeclarations(VariableRemapping vr_, OperationCache cache_) throws ZamiaException {
//
//		Resolver resolver = new Resolver(parent.getResolver(cache_), this);
//		cache_.setResolver(this, resolver);
//
//		// resolver.elaborate(storage_, context);
//
//		int n = declarations.size();
//		for (int i = 0; i < n; i++) {
//			BlockDeclarativeItem decl = declarations.get(i);
//			decl.inline(vr_, cache_);
//		}
//	}
//
//	@Override
//	public void inline(VariableRemapping vr_, OperationCache cache_) throws ZamiaException {
//		getResolver(cache_).add(this);
//	}

	// public SubProgramTransients getTransients(OperationCache cache_) {
	// SubProgramTransients spt = (SubProgramTransients)
	// cache_.getTransients(this);
	// if (spt == null) {
	// spt = new SubProgramTransients();
	// cache_.setTransients(this, spt);
	// }
	// return spt;
	// }

//	InterpreterCode generateInterpreterCode(ArrayList<ArrayList<AssociationElement>> params_, OperationCache cache_)
//			throws ZamiaException {
//
//		// SubProgramTransients spt = getTransients(cache_);
//		// InterpreterCode ic = spt.ic;
//		//
//		// if (ic != null)
//		// return ic;
//
//		InterpreterCode ic = new InterpreterCode();
//
//		// check for built-in functions
//		if (getId().equals("CONV_INTEGER")) {
//			ic.add(new ZILBuiltinFunctionStmt(BUILTINS.CONV_INTEGER, this));
//		} else if (getId().equals("CONV_STD_LOGIC_VECTOR")) {
//			ic.add(new ZILBuiltinFunctionStmt(BUILTINS.CONV_STD_LOGIC_VECTOR, this));
//		} else if (getId().equals("TO_INTEGER")) {
//			ic.add(new ZILBuiltinFunctionStmt(BUILTINS.TO_INTEGER, this));
//		} else if (getId().equals("TO_UNSIGNED")) {
//			ic.add(new ZILBuiltinFunctionStmt(BUILTINS.TO_UNSIGNED, this));
//		} else if (getId().equals("STD_MATCH")) {
//			ic.add(new ZILBuiltinFunctionStmt(BUILTINS.STD_MATCH, this));
//		} else if (getId().equals("READ")) {
//			ic.add(new ZILBuiltinFunctionStmt(BUILTINS.READ, this));
//		} else if (getId().equals("WRITE")) {
//			ic.add(new ZILBuiltinFunctionStmt(BUILTINS.WRITE, this));
//		} else if (getId().equals("ENDFILE")) {
//			ic.add(new ZILBuiltinFunctionStmt(BUILTINS.ENDFILE, this));
//		} else if (getId().equals("READLINE")) {
//			ic.add(new ZILBuiltinFunctionStmt(BUILTINS.READLINE, this));
//		} else if (getId().equals("WRITELINE")) {
//			ic.add(new ZILBuiltinFunctionStmt(BUILTINS.WRITELINE, this));
//		} else if (getId().equals("DEALLOCATE")) {
//			ic.add(new ZILBuiltinFunctionStmt(BUILTINS.DEALLOCATE, this));
//		} else if (getId().equals("NOW")) {
//			ic.add(new ZILBuiltinFunctionStmt(BUILTINS.NOW, this));
//		} else {
//
//			Resolver resolver = new Resolver(parent.getResolver(cache_), this);
//			cache_.setResolver(this, resolver);
//
//			/*
//			 * generate code to turn parameters into variables
//			 */
//
//			int n = interfaces.getNumInterfaces();
//			for (int i = n - 1; i >= 0; i--) {
//
//				InterfaceDeclaration formal = interfaces.get(i);
//
//				String varId = formal.getId();
//
//				SigType t = formal.getType().elaborate(cache_, null);
//
//				// compute actual type
//
//				SigType actualType = t;
//
//				ArrayList<AssociationElement> associations = params_.get(i);
//				if (associations.size() > 0) {
//					if (associations.size() != 1) {
//						// FIXME: implement
//						throw new ZamiaException(
//								"Internal error: Sorry, multiple associations for subprograms parameters are not implemented yet.",
//								this);
//					}
//					Operation actual = associations.get(0).getActualPart();
//					actualType = actual.getType(cache_, t, null, false);
//				} else {
//					Operation actual = formal.getValue();
//					if (actual == null) {
//						throw new ZamiaException(
//								"No actual parameter value given for an interface that has no default value.", formal);
//					}
//					actualType = actual.getType(cache_, t, null, false);
//				}
//
//				resolver.createVariable(varId, actualType, this, location);
//
//				ic.add(new ZILNewVarStmt(varId, actualType, this));
//				ic.add(new PushStmt(varId, this));
//				ic.add(new PopStmt(this));
//			}
//
//			n = declarations.size();
//
//			for (int i = 0; i < n; i++) {
//				BlockDeclarativeItem decl = declarations.get(i);
//				decl.elaborate(cache_, null);
//				decl.generateCode(ic, cache_);
//			}
//
//			code.generateCode(null, ic, cache_, null);
//
//			// ic.dump(System.out);
//
//		}
//
//		// spt.ic = ic;
//
//		return ic;
//	}


//	public ArrayList<ArrayList<AssociationElement>> getParams(AssociationList elements_, OperationCache cache_,
//			VariableRemapping vr_, boolean doReport_) throws ZamiaException {
//
//		return interfaces.computeAssociationsList(elements_, cache_, cache_/* FIXME */, vr_, doReport_, false, false, true);
//	}
//	public boolean isSynthesizable(VariableRemapping vr_, OperationCache cache_) throws ZamiaException {
//
//		if (getId().equals("CONV_INTEGER")) {
//			return true;
//		} else if (getId().equals("CONV_STD_LOGIC_VECTOR")) {
//			return true;
//		} else if (getId().equals("TO_INTEGER")) {
//			return true;
//		} else if (getId().equals("TO_UNSIGNED")) {
//			return true;
//		}
//
//		if (code == null) {
//			return false;
//		}
//
//		return code.isSynthesizable(vr_, cache_);
//	}


}
