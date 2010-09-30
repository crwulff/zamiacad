/*
 * Copyright 2004-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.vhdl.ast;

import java.io.PrintStream;
import java.util.ArrayList;

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
import org.zamia.instgraph.IGObject.IGObjectCat;
import org.zamia.instgraph.IGObject.OIDir;


/**
 * 
 * @author Guenter Bartsch
 *
 */


@SuppressWarnings("serial")
public class SharedVariableDeclaration extends BlockDeclarativeItem{
	
	private TypeDefinition td;
	private Operation initialValue;
	
	public SharedVariableDeclaration (String id_, TypeDefinition td_, Operation initialValue_, ASTObject parent_, long location_) {
		super (id_, parent_, location_);
		setType(td_);
		initialValue = initialValue_;
		if (initialValue != null)
			initialValue.setParent(this);
	}
	
	public TypeDefinition getType() {
		return td;
	}
	public void setId(String string) {
		id = string;
	}
	public void setType(TypeDefinition td_) {
		td = td_;
		td.setParent(this);
	}
	
	public void dump() {
		System.out.println ("SV id="+id+" type="+td);
	}

	public void dump(PrintStream out_) {
		out_.println ("Shared Variable Declaration id="+id+" type="+td);
	}

	@Override
	public int getNumChildren() {
		return 2;
	}

	@Override
	public ASTObject getChild(int idx_) {
		switch (idx_) {
		case 0:
			return initialValue;
		case 1:
			return td;
		}
		return null;
	}
	
	@Override 
	public String toString() {
		return "SHARED VARIABLE "+id+" : "+td + " := "+initialValue;
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache, ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
		if (id_.equals(getId())) {
			result_.add(new ReferenceSite(this, RefType.Declaration));
		}
	}

	@Override
	public IGContainerItem computeIG(ArrayList<IGContainerItem> aSpecItems, IGContainer aContainer, IGElaborationEnv aEE) throws ZamiaException {

		IGType type = td.computeIG(aContainer, aEE);

		IGOperation iv = initialValue != null ? initialValue.computeIGOperation(type, aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null) : null;

		IGObject v = new IGObject(OIDir.NONE, iv, IGObjectCat.VARIABLE, type, getId(), getLocation(), aEE.getZDB());
		v.setShared(true);
		
		aContainer.add(v);

		return v;
	}

}
