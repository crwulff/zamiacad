/*
 * Copyright 2004-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.ZamiaException;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.IGSubProgram.IGBuiltin;
import org.zamia.instgraph.IGType.TypeCat;
import org.zamia.vhdl.ast.OperationLiteral.LiteralCat;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class TypeDefinitionEnum extends TypeDefinition {

	private ArrayList<OperationLiteral> fEnumLiterals; // of OperationLiteral

	public TypeDefinitionEnum(VHDLNode aParent, long aLocation) {
		super(aParent, aLocation);
		fEnumLiterals = new ArrayList<OperationLiteral>();
	}

	public void add(OperationLiteral aLiteral) {
		if (aLiteral != null) {
			fEnumLiterals.add(aLiteral);
			aLiteral.setParent(this);
		}
	}

	@Override
	public int getNumChildren() {
		return fEnumLiterals.size();
	}

	@Override
	public VHDLNode getChild(int aIdx) {
		return fEnumLiterals.get(aIdx);
	}

	public int getNumLiterals() {
		return fEnumLiterals.size();
	}

	public OperationLiteral getLiteral(int aIdx) {
		return fEnumLiterals.get(aIdx);
	}

	@Override
	public IGType computeIG(IGContainer aContainer, IGElaborationEnv aCache) {

		IGTypeStatic te = null;
		try {
			te = new IGTypeStatic(TypeCat.ENUM, null, null, null, null, false, getLocation(), aCache.getZDB());
			int n = fEnumLiterals.size();
			for (int i = 0; i < n; i++) {
				try {
					OperationLiteral l = fEnumLiterals.get(i);
					if (l.getCategory() == LiteralCat.CHAR) {
						te.addEnumLiteral(l.getImage().charAt(0), l.getLocation());
					} else {
						IGStaticValue c = te.addEnumLiteral(l.getImage(), l.getLocation());

						aContainer.add(c);
					}

				} catch (ZamiaException e) {
					reportError(e);
				}
			}
			te.finishEnum(getLocation());

			// add implicit operators

			IGType b = aContainer.findBoolType();
			if (b == null) {
				// hapens once in standard.vhdl => bootstrapping
				b = te;
			}

			aContainer.addBuiltinOperator("\"=\"", te, te, b, IGBuiltin.SCALAR_EQUALS, getLocation());
			aContainer.addBuiltinOperator("\"/=\"", te, te, b, IGBuiltin.SCALAR_NEQUALS, getLocation());
			aContainer.addBuiltinOperator("\"<\"", te, te, b, IGBuiltin.SCALAR_LESS, getLocation());
			aContainer.addBuiltinOperator("\"<=\"", te, te, b, IGBuiltin.SCALAR_LESSEQ, getLocation());
			aContainer.addBuiltinOperator("\">\"", te, te, b, IGBuiltin.SCALAR_GREATER, getLocation());
			aContainer.addBuiltinOperator("\">=\"", te, te, b, IGBuiltin.SCALAR_GREATEREQ, getLocation());

		} catch (ZamiaException e1) {
			reportError(e1);
		}

		if (te == null) {
			return IGType.createErrorType(aCache.getZDB());
		}

		te.storeOrUpdate();

		return te;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("(");
		int n = fEnumLiterals.size();
		for (int i = 0; i < n; i++) {
			buf.append(fEnumLiterals.get(i));
			if (i < n - 1) {
				buf.append(", ");
			}
		}
		buf.append(")");
		return buf.toString();
	}
}