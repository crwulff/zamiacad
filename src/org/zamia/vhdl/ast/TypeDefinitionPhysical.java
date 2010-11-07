/*
 * Copyright 2005-2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.vhdl.ast;

import java.math.BigInteger;
import java.util.ArrayList;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGStaticValueBuilder;
import org.zamia.instgraph.IGSubProgram.IGBuiltin;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGType.TypeCat;
import org.zamia.instgraph.IGTypeStatic;

/**
 * 
 * Physical types are range types but have additional unit definitions
 * 
 * @author guenter bartsch
 * 
 */
@SuppressWarnings("serial")
public class TypeDefinitionPhysical extends TypeDefinition {

	private String fBaseUnit;

	private ArrayList<UnitInfo> fUnits = new ArrayList<UnitInfo>();

	private Range fRange;

	public TypeDefinitionPhysical(Range aRange, String aBaseUnit, VHDLNode aParent, long aLocation) {
		super(aParent, aLocation);
		setRange(aRange);
		fBaseUnit = aBaseUnit;
	}

	public void setRange(Range aRange) {
		fRange = aRange;
		fRange.setParent(this);
	}

	public Range getRange() {
		return fRange;
	}

	public void addUnit(String aId, OperationLiteral aUnit, long aLocation) {
		if (aUnit != null) {
			fUnits.add(new UnitInfo(aId, aUnit, this, aLocation));
			aUnit.setParent(this);
		}
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	@Override
	public VHDLNode getChild(int aIdx) {
		return fRange;
	}

	@Override
	public String toString() {

		StringBuilder buf = new StringBuilder(fRange.toString() + " UNITS ");

		int n = fUnits.size();
		for (int i = 0; i < n; i++) {

			UnitInfo unit = fUnits.get(i);

			buf.append(unit.getId());
			if (unit.getLiteral() != null) {
				buf.append(" = " + unit.getLiteral());
			}
			buf.append("; ");
		}

		buf.append(" END UNITS");

		return buf.toString();
	}

	private void addBuiltinPhysicalOperators(IGType aType, IGContainer aContainer, SourceLocation aLocation) throws ZamiaException {

		IGType it = aContainer.findUniversalIntType();
		IGType rt = aContainer.findUniversalRealType();

		aContainer.addBuiltinOperator("\"*\"", aType, it, aType, IGBuiltin.INT_MUL, aLocation);
		aContainer.addBuiltinOperator("\"*\"", aType, rt, aType, IGBuiltin.INT_MUL, aLocation);
		aContainer.addBuiltinOperator("\"*\"", it, aType, aType, IGBuiltin.INT_MUL, aLocation);
		aContainer.addBuiltinOperator("\"*\"", rt, aType, aType, IGBuiltin.INT_MUL, aLocation);

		aContainer.addBuiltinOperator("\"/\"", aType, it, aType, IGBuiltin.INT_DIV, aLocation);
		aContainer.addBuiltinOperator("\"/\"", aType, rt, aType, IGBuiltin.INT_DIV, aLocation);
		aContainer.addBuiltinOperator("\"/\"", aType, aType, it, IGBuiltin.INT_DIV, aLocation);

		aContainer.addBuiltinOperator("\"+\"", aType, aType, aType, IGBuiltin.INT_ADD, aLocation);
		aContainer.addBuiltinOperator("\"-\"", aType, aType, aType, IGBuiltin.INT_MINUS, aLocation);

		aContainer.addBuiltinOperator("\"-\"", aType, aType, IGBuiltin.INT_NEG, aLocation);
		aContainer.addBuiltinOperator("\"+\"", aType, aType, IGBuiltin.INT_POS, aLocation);
		aContainer.addBuiltinOperator("\"ABS\"", aType, aType, IGBuiltin.INT_ABS, aLocation);

		addBuiltinScalarOperators(aType, aContainer, aLocation);
	}

	@Override
	public IGType computeIG(IGContainer aContainer, IGElaborationEnv aEE) {
		IGType type = null;
		try {

			// anonymous base type

			IGTypeStatic abt = new IGTypeStatic(TypeCat.PHYSICAL, null, null, null, null, false, null, aEE.getZDB());

			IGStaticValue siLeft = new IGStaticValueBuilder(abt, null, null).setNum(new BigInteger("-9223372036854775808")).buildConstant();
			IGStaticValue siRight = new IGStaticValueBuilder(abt, null, null).setNum(new BigInteger("9223372036854775807")).buildConstant();
			IGStaticValue siAscending = aContainer.findTrueValue();

			abt.setLeft(siLeft, null);
			abt.setRight(siRight, null);
			abt.setAscending(siAscending, null);
			abt.setUniversal(true);
			abt.storeOrUpdate();

			//IGType abt = new IGType(TypeCat.PHYSICAL, null, null, null, null, null, false, getLocation(), aEE.getZDB());

			addBuiltinPhysicalOperators(abt, aContainer, getLocation());

			IGType iRT = abt.getRange().getType();

			IGOperation rangeOp = fRange.computeIG(iRT, aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);

			IGStaticValue sRangeOp = rangeOp.computeStaticValue(aEE.getInterpreterEnv(), ASTErrorMode.EXCEPTION, null);
			if (sRangeOp == null) {
				throw new ZamiaException("TypeDefinitionPhysical: Static range expected here.", fRange.getLocation());
			}

			IGTypeStatic sType = abt.createSubtype(sRangeOp, getLocation());
			type = sType;

			IGStaticValue unitValue = new IGStaticValueBuilder(sType, fBaseUnit, getLocation()).setNum(1).buildConstant();
			abt.addUnit(fBaseUnit, unitValue, getLocation());
			aContainer.add(unitValue);

			int len = fUnits.size();
			for (int i = 0; i < len; i++) {
				try {
					UnitInfo ui = (UnitInfo) fUnits.get(i);

					IGOperation igScale = ui.getLiteral().computeIGOperation(type, aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);

					unitValue = igScale.computeStaticValue(aEE.getInterpreterEnv(), ASTErrorMode.EXCEPTION, null);

					unitValue.setId(ui.getId());

					abt.addUnit(ui.getId(), unitValue, getLocation());

					aContainer.add(unitValue);

				} catch (ZamiaException e) {
					reportError(e);
				}
			}

		} catch (ZamiaException e) {
			reportError(e);
		}

		if (type == null) {
			type = IGType.createErrorType(aEE.getZDB());
		} else {
			type.storeOrUpdate();
		}

		return type;
	}

}
