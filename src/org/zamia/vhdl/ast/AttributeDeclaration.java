/*
 * Copyright 2005-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jul 20, 2005
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
import org.zamia.instgraph.IGAttribute;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGContainerItem;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGType;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class AttributeDeclaration extends BlockDeclarativeItem {
	
	private Name typeMark;
	
	public AttributeDeclaration (String id_, Name typeMark_, VHDLNode parent_, long location_) {
		super (id_, parent_, location_);
		typeMark = typeMark_;
		typeMark.setParent(this);
	}
	
	public void dump(PrintStream out_) {
		out_.println ("Attribute "+id+", type: "+typeMark);
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	@Override
	public VHDLNode getChild(int idx_) {
		return typeMark;
	}
	
	@Override
	public String toString() {
		return "ATTRIBUTE "+id+" : "+typeMark;
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache, ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
		if (id_.equals(getId())) {
			result_.add(new ReferenceSite(this, RefType.Declaration));
		}
	}

	@Override
	public IGContainerItem computeIG(ArrayList<IGContainerItem> aSpecItems, IGContainer aContainer, IGElaborationEnv aEE) throws ZamiaException {
		IGType type = typeMark.computeIGAsType(aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null) ;
		IGAttribute s = new IGAttribute(type, getId(), getLocation(), aEE.getZDB());
		aContainer.add(s);
		return s;
	}

}
