/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.vhdl.ast.OperationMath.MathOp;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class MultiplyingOperator extends VHDLNode {

	private MathOp op;
	
	public MultiplyingOperator (MathOp op_, long location_) {
		super(location_);
		op = op_;
	}

	public MathOp getOp() {
		return op;
	}

	@Override
	public void findReferences(String aId_, ObjectCat aCategory_, RefType aRefType_, int aDepth_, ZamiaProject aZprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aResult_, ArrayList<SearchJob> aTODO) throws ZamiaException {
	}

	@Override
	public VHDLNode getChild(int aIdx_) {
		return null;
	}

	@Override
	public int getNumChildren() {
		return 0;
	}
}
