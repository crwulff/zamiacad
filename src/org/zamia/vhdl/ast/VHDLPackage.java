/*
 * Copyright 2004-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Dec 1, 2004
 */

package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.apache.log4j.Level;
import org.zamia.DMManager;
import org.zamia.IDesignModule;
import org.zamia.SourceFile;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGContainerItem;
import org.zamia.instgraph.IGDesignUnit;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGModule;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGSubProgram.IGBuiltin;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGType.TypeCat;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.vhdl.ast.DMUID.LUType;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class VHDLPackage extends PrimaryUnit {

	public VHDLPackage(Context aContext, String aId, SourceFile aSF, long aLinCol, String aLibId, ZDB aZDB) {
		super(aContext, aId, aSF, aLinCol, aLibId, aZDB);
	}

	public String toString() {
		return "VHDLPackage id=" + id;
	}

	@Override
	public int getNumChildren() {
		return fDeclarations.size() + 1;
	}

	@Override
	public VHDLNode getChild(int aIdx) {
		switch (aIdx) {
		case 0:
			return getContext();
		}
		aIdx -= 1;
		return getDeclaration(aIdx);
	}

	public void dump(ZamiaLogger aLogger, Level aLevel) {
		aLogger.log(aLevel, "DUMP OF PACKAGE " + this + " STARTS");
		aLogger.log(aLevel, "======================================================");

		int n = fDeclarations.size();
		for (int i = 0; i < n; i++) {
			BlockDeclarativeItem decl = getDeclaration(i);
			aLogger.log(aLevel, "DECL %4d/%4d : %s\n", i, n, decl.toString());
		}

		aLogger.log(aLevel, "");

		aLogger.log(aLevel, "DUMP OF PACKAGE " + this + " ENDS HERE");
		aLogger.log(aLevel, "");
	}

	@Override
	public DMUID getDMUID(String libId_) throws ZamiaException {
		return new DMUID(LUType.Package, libId_, getId(), null);
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) {

		// FIXME: todo

		logger.warn("%s: findReferences not implemented yet.", getClass());

	}

	@Override
	public void computeIG(IGManager aIGM, IGDesignUnit aPkg) {

		boolean bootstrapMode = getLibId().equals("STD") && getId().equals("STANDARD");

		logger.info("VHDLPackage: starting computeIG for %s bootstrap mode: %b", this, bootstrapMode);

		/*
		 * context
		 */

		IGContainer container = aPkg.getContainer();

		ZamiaProject zprj = aIGM.getProject();

		IGElaborationEnv cache = new IGElaborationEnv(zprj);

		fContext.computeIG(container, cache);

		/*
		 * header declarations
		 */

		IGInterpreterCode ic = new IGInterpreterCode("Package " + this, getLocation());
		IGInterpreterRuntimeEnv env = new IGInterpreterRuntimeEnv(ic, zprj);

		cache.setInterpreterEnv(env);

		int n = getNumDeclarations();
		for (int i = 0; i < n; i++) {

			BlockDeclarativeItem decl = getDeclaration(i);

			//logger.debug ("VHDLPackage: computeIG(): working on header declaration '%s'", decl.getId());

			try {
				IGContainerItem item = decl.computeIG(null, container, cache);

				if (item instanceof IGObject) {
					IGObject obj = (IGObject) item;
					env.newObject(obj, ASTErrorMode.EXCEPTION, null, decl.getLocation());
				}
			} catch (ZamiaException e) {
				reportError(e);
			} catch (Throwable t) {
				el.logException(t);
			}

			if (bootstrapMode && decl.getId().equals("BOOLEAN")) {
				container.declareUniversalTypes();
			}
		}

		try {
			// look for package body

			DMUID duuid = new DMUID(LUType.PackageBody, getLibId(), getId(), null);

			DMManager dum = zprj.getDUM();

			IDesignModule bodyDU = dum.getDM(duuid);

			if (bodyDU != null) {

				PackageBody body = (PackageBody) bodyDU;

				logger.info("VHDLPackage: starting computePackageBodyIG for %s", this);

				body.computePackageBodyIG(container, cache);
			}
		} catch (ZamiaException e) {
			reportError(e);
		} catch (Throwable t) {
			el.logException(t);
		}

		// is this the STD.STANDARD package? => bootstrap mode
		// bootstrap mode?
		// if so, we need to add the implicitly declared operands 
		// for the standard types
		if (bootstrapMode) {
			addImplicitOperands(container, cache);
		}

		aPkg.storeOrUpdate();

		logger.info("VHDLPackage: done with computeIG for %s", this);

		// some statistics: how many types did we manage to compute
		// successfully?

		int nTotal = 0, nOK = 0;
		int nItems = container.getNumLocalItems();
		for (int i = 0; i < nItems; i++) {

			IGContainerItem item = container.getLocalItem(i);

			if (item instanceof IGType) {

				IGType t = (IGType) item;
				nTotal++;
				if (t.getCat() != TypeCat.ERROR) {
					nOK++;
				} else {
					logger.error("VHDLPackage: failed to compute type %s in pkg %s", t.getId(), getId());
				}
			} else if (item instanceof IGObject) {
				IGObject obj = (IGObject) item;

				IGType t = obj.getType();

				nTotal++;
				if (t.getCat() != TypeCat.ERROR) {
					nOK++;
				} else {
					logger.error("VHDLPackage: failed to compute type for item %s in pkg %s", obj, getId());
				}
			}
		}
		if (nTotal > 0) {
			double p = (double) nOK * 100 / (double) nTotal;
			logger.info("VHDLPackage: Out of %d types zamia managed to compute %d successfully, pkg: %s => %3.2f%%", nTotal, nOK, getId(), p);
		}
	}

	private void addImplicitOperands(IGContainer aContainer, IGElaborationEnv aCache) {

		try {
			IGType b = aContainer.findBoolType();

			aContainer.addBuiltinOperator("\"AND\"", b, b, b, IGBuiltin.BOOL_AND, getLocation());
			aContainer.addBuiltinOperator("\"OR\"", b, b, b, IGBuiltin.BOOL_OR, getLocation());
			aContainer.addBuiltinOperator("\"NAND\"", b, b, b, IGBuiltin.BOOL_NAND, getLocation());
			aContainer.addBuiltinOperator("\"NOR\"", b, b, b, IGBuiltin.BOOL_NOR, getLocation());
			aContainer.addBuiltinOperator("\"XOR\"", b, b, b, IGBuiltin.BOOL_XOR, getLocation());
			aContainer.addBuiltinOperator("\"XNOR\"", b, b, b, IGBuiltin.BOOL_XNOR, getLocation());
			aContainer.addBuiltinOperator("\"NOT\"", b, b, IGBuiltin.BOOL_NOT, getLocation());

			IGType bit = aContainer.findBitType();
			bit.setBit(true);

			aContainer.addBuiltinOperator("\"AND\"", bit, bit, bit, IGBuiltin.BIT_AND, getLocation());
			aContainer.addBuiltinOperator("\"OR\"", bit, bit, bit, IGBuiltin.BIT_OR, getLocation());
			aContainer.addBuiltinOperator("\"NAND\"", bit, bit, bit, IGBuiltin.BIT_NAND, getLocation());
			aContainer.addBuiltinOperator("\"NOR\"", bit, bit, bit, IGBuiltin.BIT_NOR, getLocation());
			aContainer.addBuiltinOperator("\"XOR\"", bit, bit, bit, IGBuiltin.BIT_XOR, getLocation());
			aContainer.addBuiltinOperator("\"XNOR\"", bit, bit, bit, IGBuiltin.BIT_XNOR, getLocation());
			aContainer.addBuiltinOperator("\"NOT\"", bit, bit, IGBuiltin.BIT_NOT, getLocation());

			IGType it = aContainer.findIntType();
			IGType tt = aContainer.findTimeType();

			aContainer.addBuiltinOperator("\"/\"", tt, tt, it, IGBuiltin.TIME_DIVINT, getLocation());

			IGType st = aContainer.findStringType();

			aContainer.addBuiltinOperator("\"=\"", st, st, b, IGBuiltin.STRING_EQUALS, getLocation());
			aContainer.addBuiltinOperator("\"/=\"", st, st, b, IGBuiltin.STRING_NEQUALS, getLocation());
			aContainer.addBuiltinOperator("\"<\"", st, st, b, IGBuiltin.STRING_LESS, getLocation());
			aContainer.addBuiltinOperator("\"<=\"", st, st, b, IGBuiltin.STRING_LESSEQ, getLocation());
			aContainer.addBuiltinOperator("\">\"", st, st, b, IGBuiltin.STRING_GREATER, getLocation());
			aContainer.addBuiltinOperator("\">=\"", st, st, b, IGBuiltin.STRING_GREATEREQ, getLocation());
			aContainer.addBuiltinOperator("\"&\"", st, st, st, IGBuiltin.STRING_CONCAT, getLocation());

			IGType bvt = aContainer.findBitVectorType();

			TypeDefinition.addBuiltinArrayLogicOperators(bvt, aContainer, getLocation());

			TypeDefinition.addBuiltinBitvectorShiftOperators(bvt, aContainer, getLocation());

			aContainer.addBuiltinOperator("\"=\"", bvt, bvt, b, IGBuiltin.BITVECTOR_EQUALS, getLocation());
			aContainer.addBuiltinOperator("\"/=\"", bvt, bvt, b, IGBuiltin.BITVECTOR_NEQUALS, getLocation());
			aContainer.addBuiltinOperator("\"<\"", bvt, bvt, b, IGBuiltin.BITVECTOR_LESS, getLocation());
			aContainer.addBuiltinOperator("\"<=\"", bvt, bvt, b, IGBuiltin.BITVECTOR_LESSEQ, getLocation());
			aContainer.addBuiltinOperator("\">\"", bvt, bvt, b, IGBuiltin.BITVECTOR_GREATER, getLocation());
			aContainer.addBuiltinOperator("\">=\"", bvt, bvt, b, IGBuiltin.BITVECTOR_GREATEREQ, getLocation());
			aContainer.addBuiltinOperator("\"&\"", bvt, bvt, bvt, IGBuiltin.BITVECTOR_CONCAT, getLocation());

		} catch (ZamiaException e) {
			el.logZamiaException(e);
		}
	}

	@Override
	public void computeStatementsIG(IGManager aIGM, IGModule aModule) {
	}

}
