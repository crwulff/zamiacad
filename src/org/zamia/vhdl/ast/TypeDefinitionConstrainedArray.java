/*
 * Copyright 2004-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.ErrorReport;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.IGSubProgram.IGBuiltin;
import org.zamia.instgraph.IGType.TypeCat;


/**
 * 
 * @author Guenter Bartsch
 * 
 * 
 *         testcase
 * 
 *         TYPE AIB_cmd_crd_avail is array
 *         (aib_channel_number'(aib_channel_number'low) to
 *         aib_channel_number'(aib_channel_number'high)) of std_ulogic_vector(0
 *         to 5); TYPE AIB_dat_crd_avail is array
 *         (aib_channel_number'(aib_channel_number'low) to
 *         aib_channel_number'(aib_channel_number'high)) of std_ulogic_vector(0
 *         to 7); CONSTANT aib_channels : natural := 4; -- can be 4 or 8
 * 
 *         SUBTYPE aib_channel_number IS natural range 0 to (aib_channels - 1);
 * 
 * 
 */

@SuppressWarnings("serial")
public class TypeDefinitionConstrainedArray extends TypeDefinition {

	private ArrayList<DiscreteRange> fRanges; /* of Range */

	private TypeDefinition fElementType; /* array */

	public TypeDefinitionConstrainedArray(TypeDefinition aElementType, ASTObject aParent, long aLocation) {
		super(aParent, aLocation);
		fRanges = new ArrayList<DiscreteRange>();
		fElementType = aElementType;
		fElementType.setParent(this);
	}

	public TypeDefinitionConstrainedArray(DiscreteRange aRange, TypeDefinition aElementType, ASTObject aParent, long aLocation) {
		this(aElementType, aParent, aLocation);
		add(aRange);
	}

	public void add(DiscreteRange aRange) {
		fRanges.add(aRange);
		aRange.setParent(this);
	}

	public void setElementType(TypeDefinition aElementType) {
		fElementType = aElementType;
	}

	public TypeDefinition getElementType() {
		return fElementType;
	}

	@Override
	public int getNumChildren() {
		return fRanges.size() + 1;
	}

	@Override
	public ASTObject getChild(int aIdx) {
		if (aIdx == 0)
			return fElementType;
		return fRanges.get(aIdx - 1);
	}

	@Override
	public IGType computeIG(IGContainer aContainer, IGElaborationEnv aEE) {
		IGType res = null;
		try {
			IGType b = aContainer.findBoolType();

			int n = fRanges.size();

			if (n == 0) {
				throw new ZamiaException("At least one dimension expected here.", this);
			}

			res = fElementType.computeIG(aContainer, aEE);
			
			boolean discreteArray = n == 1 && res.isDiscrete();
			boolean logicArray = n==1 && (res.isBit() || res.isBool());

			for (int iRange = n - 1; iRange >= 0; iRange--) {

				DiscreteRange dr = fRanges.get(iRange);

				IGItem zItem = dr.computeIG(null, aContainer, aEE, new IGOperationCache());

				if (zItem instanceof IGType) {

					IGType idxType = (IGType) zItem;
					
					// anonymous, unconstrained base type
					res = new IGType(TypeCat.ARRAY, null, null, idxType, res, null, true, getLocation(), aEE.getZDB());
					res = res.createSubtype(idxType.getRange(), aEE.getInterpreterEnv(), getLocation());
					
					//res = new IGType(TypeCat.ARRAY, null, null, idxType, res, null, false, getLocation(), aCache.getZDB());

				} else if (zItem instanceof IGOperation) {

					IGOperation zr = (IGOperation) zItem;

					IGType rangeType = zr.getType();
					if (!rangeType.isRange()) {
						throw new ZamiaException("Discrete range expected here, found: " + zItem, dr);
					}

					//IGType idxTypeRaw = rangeType.getElementType().createSubtype(zr, aCache.getInterpreterEnv(), getLocation());
					//res = new IGType(TypeCat.ARRAY, null, null, idxTypeRaw, res, null, false, getLocation(), aCache.getZDB());

					// anonymous, unconstrained base type
					res = new IGType(TypeCat.ARRAY, null, null, rangeType.getElementType(), res, null, true, getLocation(), aEE.getZDB());
					res = res.createSubtype(zr, aEE.getInterpreterEnv(), getLocation());

				} else {
					throw new ZamiaException("Discrete range expected here, found instead: " + zItem, dr);
				}

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

		} catch (ZamiaException e) {
			reportError(e);
		} catch (Throwable t) {
			el.logException(t);
			res = IGType.createErrorType(aEE.getZDB());
		}

		return res;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("ARRAY (");

		int n = fRanges != null ? fRanges.size() : 0;
		for (int i = 0; i < n; i++) {

			buf.append(fRanges.get(i));

			if (i < n - 1) {
				buf.append(", ");
			}
		}

		buf.append(") OF ");
		buf.append(fElementType);

		return buf.toString();
	}
}