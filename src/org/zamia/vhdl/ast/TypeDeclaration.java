/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jan 9, 2005
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
import org.zamia.instgraph.IGRange;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGTypeStatic;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class TypeDeclaration extends BlockDeclarativeItem {

	private TypeDefinition t;

	public TypeDeclaration(String id_, TypeDefinition t_, VHDLNode parent_, long location_) {
		super(id_, parent_, location_);
		t = t_;
		t.setParent(this);
	}

	public TypeDefinition getType() {
		return t;
	}

	public void dump(PrintStream out_) {
		out_.println("TypeDeclaration: id=" + id + ", type=" + t);
	}

	public String toString() {
		return "TYPE " + id + " IS " + t;
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	@Override
	public VHDLNode getChild(int idx_) {
		return t;
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) {

		if (id_.equals(getId())) {
			result_.add(new ReferenceSite(this, RefType.Declaration));
		}

	}

	@Override
	public void dumpVHDL(int indent_, PrintStream out_) throws ZamiaException {
		// same as dump(PrintStream) - code duplication
		printIndented("TypeDeclaration: id=" + id + ", type=" + t, indent_, out_);
		out_.println();
	}

	@Override
	public IGContainerItem computeIG(ArrayList<IGContainerItem> aSpecItems, IGContainer aContainer, IGElaborationEnv aEE) throws ZamiaException {

		IGType type = t.computeIG(aContainer, aEE);

		IGTypeStatic sType = type.computeStaticType(aEE.getInterpreterEnv(), ASTErrorMode.RETURN_NULL, new ErrorReport());
		if (sType != null) {
			type = sType;
		}

		String oldId = type.getId();
		if (oldId != null) {
			try {
				type = type.createSubtype((IGRange) null, aEE.getInterpreterEnv(), getLocation());
			} catch (ZamiaException e) {
				reportError(e);
			}
		}

		type.setId(getId());
		type.storeOrUpdate();

		aContainer.add(type);

		return type;
	}
}
