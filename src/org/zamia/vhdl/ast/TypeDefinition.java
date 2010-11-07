/* 
 * Copyright 2004-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created on Dec 13, 2004
 */
package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGSubProgram.IGBuiltin;


/**
 * Abstract base class for all VHDL type definitions
 * 
 * @author Guenter Bartsch
 */

@SuppressWarnings("serial")
public abstract class TypeDefinition extends VHDLNode {

	public TypeDefinition(VHDLNode aParent, long aLocation) {
		super(aParent, aLocation);
	}
	
	public static void addBuiltinIntOperators(IGType aType, IGContainer aContainer, SourceLocation aLocation) throws ZamiaException {
		aContainer.addBuiltinOperator("\"*\"", aType, aType, aType, IGBuiltin.INT_MUL, aLocation);
		aContainer.addBuiltinOperator("\"**\"", aType, aType, aType, IGBuiltin.INT_POWER, aLocation);
		aContainer.addBuiltinOperator("\"/\"", aType, aType, aType, IGBuiltin.INT_DIV, aLocation);
		aContainer.addBuiltinOperator("\"+\"", aType, aType, aType, IGBuiltin.INT_ADD, aLocation);
		aContainer.addBuiltinOperator("\"-\"", aType, aType, aType, IGBuiltin.INT_MINUS, aLocation);
		aContainer.addBuiltinOperator("\"-\"", aType, aType, IGBuiltin.INT_NEG, aLocation);
		aContainer.addBuiltinOperator("\"+\"", aType, aType, IGBuiltin.INT_POS, aLocation);
		aContainer.addBuiltinOperator("\"ABS\"", aType, aType, IGBuiltin.INT_ABS, aLocation);
		aContainer.addBuiltinOperator("\"REM\"", aType, aType, aType, IGBuiltin.INT_REM, aLocation);
		aContainer.addBuiltinOperator("\"MOD\"", aType, aType, aType, IGBuiltin.INT_MOD, aLocation);
		addBuiltinScalarOperators(aType, aContainer, aLocation);
	}

	public static void addBuiltinRealOperators(IGType aType, IGContainer aContainer, SourceLocation aLocation) throws ZamiaException {
		IGType it = aContainer.findUniversalIntType();
		aContainer.addBuiltinOperator("\"*\"", aType, aType, aType, IGBuiltin.REAL_MUL, aLocation);
		aContainer.addBuiltinOperator("\"**\"", aType, aType, aType, IGBuiltin.REAL_POWER, aLocation);
		aContainer.addBuiltinOperator("\"**\"", aType, it, aType, IGBuiltin.REALINT_POWER, aLocation);
		aContainer.addBuiltinOperator("\"/\"", aType, aType, aType, IGBuiltin.REAL_DIV, aLocation);
		aContainer.addBuiltinOperator("\"+\"", aType, aType, aType, IGBuiltin.REAL_ADD, aLocation);
		aContainer.addBuiltinOperator("\"-\"", aType, aType, aType, IGBuiltin.REAL_MINUS, aLocation);
		aContainer.addBuiltinOperator("\"-\"", aType, aType, IGBuiltin.REAL_NEG, aLocation);
		aContainer.addBuiltinOperator("\"+\"", aType, aType, IGBuiltin.REAL_POS, aLocation);
		aContainer.addBuiltinOperator("\"ABS\"", aType, aType, IGBuiltin.REAL_ABS, aLocation);
		addBuiltinScalarOperators(aType, aContainer, aLocation);
	}
	
	public static void addBuiltinScalarOperators(IGType aType, IGContainer aContainer, SourceLocation aLocation) throws ZamiaException {
		
		IGType b = aContainer.findBoolType();
		
		aContainer.addBuiltinOperator("\"=\"", aType, aType, b, IGBuiltin.SCALAR_EQUALS, aLocation);
		aContainer.addBuiltinOperator("\"/=\"", aType, aType, b, IGBuiltin.SCALAR_NEQUALS, aLocation);
		aContainer.addBuiltinOperator("\"<\"", aType, aType, b, IGBuiltin.SCALAR_LESS, aLocation);
		aContainer.addBuiltinOperator("\"<=\"", aType, aType, b, IGBuiltin.SCALAR_LESSEQ, aLocation);
		aContainer.addBuiltinOperator("\">\"", aType, aType, b, IGBuiltin.SCALAR_GREATER, aLocation);
		aContainer.addBuiltinOperator("\">=\"", aType, aType, b, IGBuiltin.SCALAR_GREATEREQ, aLocation);
	}
	
	public static void addBuiltinArrayLogicOperators(IGType aType, IGContainer aContainer, SourceLocation aLocation) throws ZamiaException {
		
		aContainer.addBuiltinOperator("\"AND\"", aType, aType, aType, IGBuiltin.ARRAY_AND, aLocation);
		aContainer.addBuiltinOperator("\"OR\"", aType, aType, aType, IGBuiltin.ARRAY_OR, aLocation);
		aContainer.addBuiltinOperator("\"NAND\"", aType, aType, aType, IGBuiltin.ARRAY_NAND, aLocation);
		aContainer.addBuiltinOperator("\"NOR\"", aType, aType, aType, IGBuiltin.ARRAY_NOR, aLocation);
		aContainer.addBuiltinOperator("\"XOR\"", aType, aType, aType, IGBuiltin.ARRAY_XOR, aLocation);
		aContainer.addBuiltinOperator("\"XNOR\"", aType, aType, aType, IGBuiltin.ARRAY_XNOR, aLocation);
		aContainer.addBuiltinOperator("\"NOT\"", aType, aType, IGBuiltin.ARRAY_NOT, aLocation);
	}

	public static void addBuiltinBitvectorShiftOperators(IGType aBVT, IGContainer aContainer, SourceLocation aLocation) throws ZamiaException {
		IGType it = aContainer.findUniversalIntType();
		aContainer.addBuiltinOperator("\"SLL\"", aBVT, it, aBVT, IGBuiltin.BITVECTOR_SLL, aLocation);
		aContainer.addBuiltinOperator("\"SRL\"", aBVT, it, aBVT, IGBuiltin.BITVECTOR_SRL, aLocation);
		aContainer.addBuiltinOperator("\"SLA\"", aBVT, it, aBVT, IGBuiltin.BITVECTOR_SLA, aLocation);
		aContainer.addBuiltinOperator("\"SRA\"", aBVT, it, aBVT, IGBuiltin.BITVECTOR_SRA, aLocation);
		aContainer.addBuiltinOperator("\"ROL\"", aBVT, it, aBVT, IGBuiltin.BITVECTOR_ROL, aLocation);
		aContainer.addBuiltinOperator("\"ROR\"", aBVT, it, aBVT, IGBuiltin.BITVECTOR_ROR, aLocation);
	}

	public static void addBuiltinArrayConcatenationOperators(IGType aType, IGContainer aContainer, SourceLocation aLocation) throws ZamiaException {
		
		aContainer.addBuiltinOperator("\"&\"", aType, aType, aType, IGBuiltin.ARRAY_CONCATAA, aLocation);
		aContainer.addBuiltinOperator("\"&\"", aType, aType.getElementType(), aType, IGBuiltin.ARRAY_CONCATAE, aLocation);
		aContainer.addBuiltinOperator("\"&\"", aType.getElementType(), aType, aType, IGBuiltin.ARRAY_CONCATEA, aLocation);
		aContainer.addBuiltinOperator("\"&\"", aType.getElementType(), aType.getElementType(), aType, IGBuiltin.ARRAY_CONCATEE, aLocation);
	}


	
	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache, ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) {
		
		// FIXME: todo
		
		logger.warn("%s: findReferences not implemented yet.", getClass());
		
	}

	public abstract IGType computeIG(IGContainer aContainer, IGElaborationEnv aCache);

}