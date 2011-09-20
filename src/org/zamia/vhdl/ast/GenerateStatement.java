/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Aug 9, 2005
 */

package org.zamia.vhdl.ast;

import java.io.PrintStream;
import java.util.ArrayList;

import org.zamia.ToplevelPath;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGContainerItem;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationAttribute;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGOperationLiteral;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGStructure;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGObject.IGObjectCat;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.instgraph.IGOperationAttribute.AttrOp;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.util.HashSetArray;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class GenerateStatement extends ConcurrentStatement {

	private ArrayList<ConcurrentStatement> fCSS = new ArrayList<ConcurrentStatement>();

	private ArrayList<BlockDeclarativeItem> fDecls = new ArrayList<BlockDeclarativeItem>();

	private Range fRange;

	private Operation fCond;

	private String fLoopVarId;

	public GenerateStatement(String aLoopVarId, Range aRange, String aLabel, VHDLNode aParent, long aLocation) {
		super(aLabel, aParent, aLocation);
		fLoopVarId = aLoopVarId;
		fRange = aRange;
		fRange.setParent(this);
	}

	public GenerateStatement(Operation aCond, String aLabel, VHDLNode aParent, long aLocation) {
		super(aLabel, aParent, aLocation);
		fCond = aCond;
		fCond.setParent(this);
	}

	public void add(ConcurrentStatement aStmt) {
		if (aStmt == null)
			return;
		fCSS.add(aStmt);
		aStmt.setParent(this);
	}

	public void add(BlockDeclarativeItem aDecl) {
		fDecls.add(aDecl);
		aDecl.setParent(this);
	}

	public void add(ArrayList<BlockDeclarativeItem> aDecls) {
		int n = aDecls.size();
		for (int i = 0; i < n; i++) {
			BlockDeclarativeItem decl = aDecls.get(i);
			add(decl);
		}
	}

	@Override
	public int getNumChildren() {
		return 2 + fCSS.size() + fDecls.size();
	}

	@Override
	public VHDLNode getChild(int aIdx) {
		int n = fCSS.size();
		if (aIdx < n) {
			return fCSS.get(aIdx);
		}
		aIdx -= n;
		n = fDecls.size();
		if (aIdx < n) {
			return fDecls.get(aIdx);
		}
		aIdx -= n;
		switch (aIdx) {
		case 0:
			return fCond;
		case 1:
			return fRange;
		}
		return null;
	}

	@Override
	public void dumpVHDL(int aIndent, PrintStream aOut) {
		// FIXME: implement
		aOut.println("-- ERROR: don't know how to dump " + this);
	}

	@Override
	public DeclarativeItem findDeclaration(String aId, ZamiaProject aZPrj) {

		int n = fDecls.size();
		for (int i = 0; i < n; i++) {
			BlockDeclarativeItem decl = fDecls.get(i);
			if (decl.getId().equals(aId))
				return decl;
		}

		return super.findDeclaration(aId, aZPrj);
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {

		int n = fDecls.size();
		if (depth_ > 0) {

			// do we have a local declaration that shadows the id we're searching for?

			for (int i = 0; i < n; i++) {

				BlockDeclarativeItem decl = fDecls.get(i);
				if (decl.getId().equals(id_)) {
					return;
				}
			}
		} else {
			for (int i = 0; i < n; i++) {

				BlockDeclarativeItem decl = fDecls.get(i);
				if (decl.getId().equals(id_)) {
					result_.add(new ReferenceSite(decl, RefType.Declaration));
				}
			}

		}

		n = fCSS.size();
		for (int i = 0; i < n; i++) {
			ConcurrentStatement cs = fCSS.get(i);
			cs.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		}
		if (fCond != null) {
			fCond.findReferences(id_, category_, RefType.Read, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		}
		if (fRange != null) {
			fRange.findReferences(id_, category_, RefType.Read, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		}
	}

	private void computeDeclarationsIG(IGContainer aContainer, IGElaborationEnv aCache) {

		IGInterpreterRuntimeEnv env = aCache.getInterpreterEnv();

		int n = fDecls.size();
		for (int i = 0; i < n; i++) {
			BlockDeclarativeItem decl = fDecls.get(i);

			try {
				IGContainerItem item = decl.computeIG(null, aContainer, aCache);

				if (item != null) {
					if (item instanceof IGObject) {
						IGObject obj = (IGObject) item;
						env.newObject(obj, ASTErrorMode.EXCEPTION, null, decl.getLocation());
					}
				}

			} catch (ZamiaException e) {
				reportError(e);
			} catch (Throwable t) {
				el.logException(t);
			}

		}
	}

	private void computeStatementsIG(DMUID aDUUID, String aLabel, IGStructure aStructure, IGElaborationEnv aCache) {
		int n = fCSS.size();
		for (int i = 0; i < n; i++) {
			ConcurrentStatement stmt = fCSS.get(i);

			try {
				stmt.computeIG(aDUUID, aStructure.getContainer(), aStructure, aCache);
			} catch (ZamiaException e) {
				reportError(e);
			} catch (Throwable t) {
				el.logException(t);
			}
		}
	}

	@Override
	public void computeIG(DMUID aDUUID, IGContainer aContainer, IGStructure aStructure, IGElaborationEnv aEE) throws ZamiaException {

		IGInterpreterRuntimeEnv env = aEE.getInterpreterEnv();

		if (fCond != null) {

			IGStaticValue cond = fCond.computeIGOperation(aContainer.findBoolType(), aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null).computeStaticValue(env,
					ASTErrorMode.EXCEPTION, null);

			if (cond.isTrue()) {

				String label = getLabel();
				ToplevelPath path = aStructure.getPath();
				if (label != null) {
					path = path.append(label);
				}

				IGStructure structure = new IGStructure(path, aContainer.getDBID(), label, getLocation(), aEE.getZDB());

				computeDeclarationsIG(structure.getContainer(), aEE);

				computeStatementsIG(aDUUID, getLabel(), structure, aEE);

				aStructure.addStatement(structure);

			}
		} else {

			IGOperation rop = fRange.computeIG(null, aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);

			IGType rt = rop.getType();

			if (!rt.isRange()) {
				reportError("Range expected here.");
				return;
			}

			IGType type = rt.getElementType();

			IGOperationAttribute left = new IGOperationAttribute(AttrOp.LEFT, rop, null, type, getLocation(), aEE.getZDB());
			IGOperationAttribute right = new IGOperationAttribute(AttrOp.RIGHT, rop, null, type, getLocation(), aEE.getZDB());
			IGOperationAttribute asc = new IGOperationAttribute(AttrOp.ASCENDING, rop, null, type, getLocation(), aEE.getZDB());

			int l = (int) left.computeStaticValue(env, ASTErrorMode.EXCEPTION, null).getOrd();
			int r = (int) right.computeStaticValue(env, ASTErrorMode.EXCEPTION, null).getOrd();
			boolean a = asc.computeStaticValue(env, ASTErrorMode.EXCEPTION, null).isTrue();

			if (a) {

				for (int i = l; i <= r; i++) {

					// create label and path

					String label = getLabel() + "#" + i;
					ToplevelPath path = aStructure.getPath().append(label);

					// declare the loop constant in a local scope

					IGOperation iv = type.isEnum() ? type.getEnumLiteral(i, getLocation(), ASTErrorMode.EXCEPTION, null) : new IGOperationLiteral(i, type, getLocation());

					IGObject loopConst = new IGObject(OIDir.NONE, iv, IGObjectCat.CONSTANT, type, fLoopVarId, getLocation(), aEE.getZDB());

					// set loop constant value in interpreter environment

					IGStructure structure = new IGStructure(path, aContainer.getDBID(), label, getLocation(), aEE.getZDB());

					env.pushContextFor(structure);

					structure.getContainer().add(loopConst);
					env.newObject(loopConst, ASTErrorMode.EXCEPTION, null, getLocation());

					computeDeclarationsIG(structure.getContainer(), aEE);

					computeStatementsIG(aDUUID, getLabel(), structure, aEE);

					aStructure.addStatement(structure);

					env.exitContext();
				}

			} else {

				for (int i = l; i >= r; i--) {

					// create label and path

					String label = getLabel() + "#" + i;
					ToplevelPath path = aStructure.getPath().append(label);

					// declare the loop constant in a local scope

					IGOperation iv = type.isEnum() ? type.getEnumLiteral(i, getLocation(), ASTErrorMode.EXCEPTION, null) : new IGOperationLiteral(i, type, getLocation());

					IGObject loopConst = new IGObject(OIDir.NONE, iv, IGObjectCat.CONSTANT, type, fLoopVarId, getLocation(), aEE.getZDB());

					// set loop constant value in interpreter env

					IGStructure structure = new IGStructure(path, aContainer.getDBID(), label, getLocation(), aEE.getZDB());

					env.pushContextFor(structure);

					structure.getContainer().add(loopConst);
					env.newObject(loopConst, ASTErrorMode.EXCEPTION, null, getLocation());

					computeDeclarationsIG(structure.getContainer(), aEE);

					computeStatementsIG(aDUUID, getLabel(), structure, aEE);

					aStructure.addStatement(structure);

					env.exitContext();
				}
			}
		}
	}

	public void collectIdentifiers(HashSetArray<String> aIdentifiers, ZamiaProject aZPrj) {

		int n = fDecls.size();
		for (int i = 0; i < n; i++) {
			aIdentifiers.add(fDecls.get(i).getId());
		}

		super.collectIdentifiers(aIdentifiers, aZPrj);
	}

	public int getNumConcurrentStatements() {
		return fCSS.size();
	}

	public ConcurrentStatement getConcurrentStatement(int aIdx) {
		return fCSS.get(aIdx);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();

		String label = getLabel();
		if (label != null)
			buf.append(label + ": ");

		if (fCond != null) {
			buf.append("IF " + fCond + " THEN ");
		} else {
			buf.append("FOR " + fLoopVarId + " IN " + fRange + " ");
		}

		buf.append("GENERATE");

		return buf.toString();
	}
}
