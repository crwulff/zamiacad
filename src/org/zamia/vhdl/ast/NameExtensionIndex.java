/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jun 19, 2005
 */

package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGOperationIndex;
import org.zamia.instgraph.IGOperationInvokeSubprogram;
import org.zamia.instgraph.IGOperationTypeConversion;
import org.zamia.instgraph.IGSubProgram;
import org.zamia.instgraph.IGType;


/**
 * An array index
 * 
 * @author Guenter Bartsch
 */

@SuppressWarnings("serial")
public class NameExtensionIndex extends NameExtension {

	private ArrayList<Operation> fIndices = new ArrayList<Operation>(2);

	public NameExtensionIndex(Operation aExp, VHDLNode aParent, long aLocation) {
		super(aParent, aLocation);
		add(aExp);
	}

	public void add(Operation aOp) {
		fIndices.add(aOp);
		aOp.setParent(this);
	}

	@Override
	public String toString() {
		return toVHDL();
	}

	@Override
	public int getNumChildren() {
		return fIndices.size();
	}

	@Override
	public VHDLNode getChild(int aIdx) {
		return fIndices.get(aIdx);
	}

	@Override
	public String toVHDL() {
		StringBuilder buf = new StringBuilder("(");

		int n = fIndices.size();
		for (int i = 0; i < n; i++) {
			buf.append(fIndices.get(i).toString());
			if (i < (n - 1))
				buf.append(", ");
		}
		buf.append(")");
		return buf.toString();
	}

	public int getNumIndices() {
		return fIndices.size();
	}

	public Operation getIndex(int aIdx) {
		return fIndices.get(aIdx);
	}

	@Override
	public void computeIG(IGItem aItem, SourceLocation aPrevLocation, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ArrayList<IGItem> aResult, ErrorReport aReport) throws ZamiaException {

		IGItem item = aItem;

		if (item instanceof IGType) {

			IGType type = (IGType) item;

			int nIndices = fIndices.size();

			for (int i = 0; i < nIndices; i++) {

				Operation index = fIndices.get(i);

				IGType indexType = type.getIndexType();

				IGOperation ritem = index.computeIGOperation(indexType, aContainer, aEE, aCache, ASTErrorMode.RETURN_NULL, aReport);
				if (ritem == null) {

					// type conversion?

					ArrayList<IGOperation> items = index.computeIG(type, aContainer, aEE, aCache, ASTErrorMode.RETURN_NULL, aReport);
					if (items == null || items.size() == 0) {
						items = index.computeIG(null, aContainer, aEE, aCache, ASTErrorMode.RETURN_NULL, aReport);
						if (items == null) {
							aReport.append("Failed to compute index.", getLocation());
							return;
						}
					}

					int n = items.size();
					for (int j = 0; j < n; j++) {

						// FIXME: check if types are closely related

						aResult.add(new IGOperationTypeConversion(items.get(j), type, getLocation(), aEE.getZDB()));
					}

					return;
				}
				IGType rT = ritem.getType();

				if (rT.isRange()) {

					type = type.createSubtype(ritem, aEE.getInterpreterEnv(), getLocation());

				} else {

					// type conversion

					aResult.add(new IGOperationTypeConversion(ritem, type, getLocation(), aEE.getZDB()));
					return;
				}
			}

			aResult.add(type);
			return;
		}

		if (item instanceof IGSubProgram) {

			IGSubProgram sp = (IGSubProgram) item;

			int nIndices = fIndices.size();
			AssociationList al = new AssociationList(this, getLineCol());

			for (int i = 0; i < nIndices; i++) {
				Operation op = fIndices.get(i);
				AssociationElement ae = al.add(this, getLineCol());
				ae.setActualPart(op);
			}

			IGOperationInvokeSubprogram invocation = sp.generateInvocation(al, aContainer, aEE, aCache, aPrevLocation, aReport);
			if (invocation != null) {
				aResult.add(invocation);
			}
			return;
		}

		if (!(item instanceof IGOperation)) {
			aReport.append("Operation expected here, " + item + " found instead.", getLocation());
			return;
		}

		IGOperation op = (IGOperation) item;

		IGType type = op.getType();

		int nIndices = fIndices.size();

		for (int i = 0; i < nIndices; i++) {

			Operation exp = fIndices.get(i);

			if (!type.isArray()) {
				aReport.append("Trying to index something that is not an array.", getLocation());
				return;
			}

			IGType idxType = type.getIndexType();

			IGOperation zExp = exp.computeIGOperation(idxType, aContainer, aEE, aCache, ASTErrorMode.RETURN_NULL, aReport);
			if (zExp == null) {
				aReport.append("Failed to compute index expression" + exp, getLocation());
				return;
			}

			// IGType tExp = zExp.getType();
			IGType subtype = type.getElementType();

			item = op = new IGOperationIndex(zExp, op, subtype, getLocation(), aContainer.getZDB());

			type = subtype;
		}

		aResult.add(item);
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache, ReferenceSearchResult aResult, ArrayList<SearchJob> aTODO) throws ZamiaException {
		int n = getNumIndices();
		for (int i = 0; i < n; i++) {
			Operation idx = getIndex(i);
			idx.findReferences(aId, aCat, RefType.Read, aDepth + 1, aZPrj, aContainer, aCache, aResult, aTODO);
		}
	}


}
