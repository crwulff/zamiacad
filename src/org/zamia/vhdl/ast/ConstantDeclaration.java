/*
 * Copyright 2007-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by guenter on Jul 14, 2005
 */
package org.zamia.vhdl.ast;

import java.io.PrintStream;
import java.util.ArrayList;

import org.zamia.ErrorReport;
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
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.IGObject.IGObjectCat;
import org.zamia.instgraph.IGObject.OIDir;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class ConstantDeclaration extends BlockDeclarativeItem {

	private TypeDefinition fType;

	private Operation fValue;

	public ConstantDeclaration(String aId, TypeDefinition aType, Operation aValue, VHDLNode aParent, long aLocation) {
		super(aId, aParent, aLocation);
		setType(aType);
		setValue(aValue);
	}

	private void setType(TypeDefinition aType) {
		fType = aType;
		fType.setParent(this);
	}

	public Operation getValue() {
		return fValue;
	}

	public void setValue(Operation aValue) {
		if (aValue == null)
			return;
		fValue = aValue;
		fValue.setParent(this);
	}

	public TypeDefinition getType() {
		return fType;
	}

	public void dump(PrintStream aOut) {
		aOut.println("Constant " + id + ", type=" + fType + ", value=" + fValue);
	}

	@Override
	public void dumpVHDL(int aIndent, PrintStream aOut) throws ZamiaException {
		aOut.printf("CONSTANT %s : %s", getId(), getType());
		if (fValue != null) {
			aOut.print(" := " + fValue.toVHDL());
		}
		aOut.println(";");
	}

	@Override
	public int getNumChildren() {
		return 2;
	}

	@Override
	public VHDLNode getChild(int aIdx) {
		switch (aIdx) {
		case 0:
			return fType;
		case 1:
			return fValue;
		}
		return null;
	}

	@Override
	public String toString() {
		return "CONSTANT " + id + " : " + fType + " := " + fValue;
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aRSR, ArrayList<SearchJob> aTODO) throws ZamiaException {
		fValue.findReferences(aId, aCat, RefType.Read, aDepth + 1, aZPrj, aContainer, aCache, aRSR, aTODO);
		if (aId.equals(getId())) {
			aRSR.add(new ReferenceSite(this, RefType.Declaration));
		}
	}

	@Override
	public IGContainerItem computeIG(ArrayList<IGContainerItem> aSpecItems, IGContainer aContainer, IGElaborationEnv aEE) throws ZamiaException {

		IGType t = fType.computeIG(aContainer, aEE);

		try {
			IGTypeStatic st = t.computeStaticType(aEE.getInterpreterEnv(), ASTErrorMode.RETURN_NULL, new ErrorReport());
			if (st != null) {
				t = st;
			}
		} catch (ZamiaException e) {
			el.logException(e);
		}

		IGOperation iv = null;
		try {
			iv = fValue != null ? fValue.computeIGOperation(t, aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null) : null;
		} catch (ZamiaException e) {
			reportError(e);
		} catch (Throwable t1) {
			el.logException(t1);
		}

		IGObject obj = null;
		if (iv != null && aSpecItems != null) {

			int n = aSpecItems.size();
			for (int i = 0; i < n; i++) {
				IGContainerItem specItem = aSpecItems.get(i);

				if (specItem instanceof IGObject) {

					// deferred constant

					obj = (IGObject) specItem;
					obj.setInitialValue(iv);
					obj.storeOrUpdate();

					break;
				}
			}

		}

		if (obj == null) {
			obj = new IGObject(OIDir.NONE, iv, IGObjectCat.CONSTANT, t, getId(), getLocation(), aEE.getZDB());
			aContainer.add(obj);
		}

		return obj;
	}

}
