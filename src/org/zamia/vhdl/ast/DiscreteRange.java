/*
 * Copyright 2006-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on May 12, 2006
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
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGType;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class DiscreteRange extends ASTObject {

	private Range range;

	private TypeDefinitionSubType td;

	public DiscreteRange(Range range_, ASTObject parent_, long location_) {
		super(parent_, location_);
		setRange(range_);
	}

	private void setRange(Range range_) {
		range = range_;
		range.setParent(this);
	}

	public DiscreteRange(TypeDefinitionSubType td_, ASTObject parent_, long location_) {
		super(parent_, location_);
		setTypeDefinition(td_);
	}

	public void setTypeDefinition(TypeDefinitionSubType td_) {
		td = td_;
		td.setParent(this);
	}

	@Override
	public int getNumChildren() {
		return 2;
	}

	@Override
	public ASTObject getChild(int idx_) {
		switch (idx_) {
		case 0:
			return range;
		case 1:
			return td;
		}
		return null;
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
		range.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
	}

	@Override
	public String toString() {
		if (range != null)
			return range.toString();
		return td.toString();
	}

	public IGItem computeIG(IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache) throws ZamiaException {
		if (range != null) {
			return range.computeIG(aTypeHint, aContainer, aEE, aCache, ASTErrorMode.EXCEPTION, null);
		}

		return td.computeIGItem(aContainer, aEE);
	}
}
