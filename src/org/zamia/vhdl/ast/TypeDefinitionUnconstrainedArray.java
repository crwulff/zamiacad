/*
 * Copyright 2004-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.ErrorReport;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.IGSubProgram.IGBuiltin;
import org.zamia.instgraph.IGType.TypeCat;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class TypeDefinitionUnconstrainedArray extends TypeDefinition {

	private ArrayList<Name> fIndexTypes = new ArrayList<Name>(1);

	private TypeDefinition fElementType; /* array */

	public TypeDefinitionUnconstrainedArray(ASTObject aParent, long aLocation) {
		super(aParent, aLocation);
	}

	public void add(Name aIndexTypeName) {
		fIndexTypes.add(aIndexTypeName);
		aIndexTypeName.setParent(this);
	}

	public void setElementType(TypeDefinition aElementType) {
		fElementType = aElementType;
		fElementType.setParent(this);
	}

	public TypeDefinition getElementType() {
		return fElementType;
	}

	public int getNumIndexTypes() {
		return fIndexTypes.size();
	}

	public Name getIndexType(int aIdx) {
		return (Name) fIndexTypes.get(aIdx);
	}

	@Override
	public int getNumChildren() {
		return 1 + fIndexTypes.size();
	}

	@Override
	public ASTObject getChild(int idx_) {
		if (idx_ == 0)
			return fElementType;
		return fIndexTypes.get(idx_ - 1);
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aRSR, ArrayList<SearchJob> aTODO) {

		// FIXME: todo

		logger.warn("%s: findReferences not implemented yet.", getClass());

	}

	@Override
	public IGType computeIG(IGContainer aContainer, IGElaborationEnv aEE) {

		try {
			IGType b = aContainer.findBoolType();

			IGType res = fElementType.computeIG(aContainer, aEE);

			int n = fIndexTypes.size();
			boolean discreteArray = n == 1 && res.isDiscrete();
			boolean logicArray = n==1 && (res.isBit() || res.isBool());
			for (int i = n - 1; i >= 0; i--) {

				Name tn = fIndexTypes.get(i);

				IGType idxType = tn.computeIGAsType(aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);
				res = new IGType(TypeCat.ARRAY, null, null, idxType, res, null, true, getLocation(), aEE.getZDB());

				IGTypeStatic sType = res.computeStaticType(aEE.getInterpreterEnv(), ASTErrorMode.RETURN_NULL, new ErrorReport());
				if (sType != null) {
					res = sType;
				}

				aContainer.addBuiltinOperator("\"=\"", res, res, b, IGBuiltin.ARRAY_EQUALS, getLocation());
				aContainer.addBuiltinOperator("\"/=\"", res, res, b, IGBuiltin.ARRAY_NEQUALS, getLocation());

			}

			if (discreteArray) {
				aContainer.addBuiltinOperator("\"<\"", res, res, b, IGBuiltin.ARRAY_LESS, getLocation());
				aContainer.addBuiltinOperator("\"<=\"", res, res, b, IGBuiltin.ARRAY_LESSEQ, getLocation());
				aContainer.addBuiltinOperator("\">\"", res, res, b, IGBuiltin.ARRAY_GREATER, getLocation());
				aContainer.addBuiltinOperator("\">=\"", res, res, b, IGBuiltin.ARRAY_GREATEREQ, getLocation());
			}

			if (logicArray) {
				addBuiltinArrayLogicOperators(res, aContainer, getLocation());
				addBuiltinBitvectorShiftOperators(res, aContainer, getLocation());
			}

			if (n==1) {
				addBuiltinArrayConcatenationOperators(res, aContainer, getLocation());
			}

			res.storeOrUpdate();

			return res;
		} catch (ZamiaException e) {
			reportError(e);
			return IGType.createErrorType(aEE.getZDB());
		}
	}

}