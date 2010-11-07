/*
 * Copyright 2004-2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
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
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.IGObject.IGObjectCat;
import org.zamia.instgraph.IGObject.OIDir;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class SignalDeclaration extends BlockDeclarativeItem {

	public static final int KIND_NONE = 0;

	public static final int KIND_REGISTER = 1;

	public static final int KIND_BUS = 2;

	private TypeDefinition td;

	private Operation initialValue;

	private int kind;

	public SignalDeclaration(String id_, TypeDefinition td_, int kind_, Operation initialValue_, VHDLNode parent_, long location_) {
		super(id_, parent_, location_);
		setType(td_);
		initialValue = initialValue_;
		if (initialValue != null)
			initialValue.setParent(this);
		kind = kind_;
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
		System.out.println("  id=" + id + " type=" + td);
	}

	public void dump(PrintStream out_) {
		out_.println("Signal Declaration id=" + id + " type=" + td);
	}

	@Override
	public int getNumChildren() {
		return 2;
	}

	@Override
	public VHDLNode getChild(int idx_) {
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

		String skind = "";
		switch (kind) {
		case KIND_BUS:
			skind = "BUS";
			break;
		case KIND_REGISTER:
			skind = "REGISTER";
			break;
		}

		String sIV = initialValue != null ? " := " + initialValue : "";

		return "SIGNAL " + id + " : " + td + " " + skind + sIV;
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {

		if (category_ == ObjectCat.Type) {

			// FIXME: TODO

			// if (td instanceof TypeDefinitionSubType)
			//			
			// if (getId().equals(id_)) {
			//				
			// result_.add(new ReferenceSite(getLocation(), false));
			//				
			// }
		} else {
			if (id_.equals(getId())) {
				result_.add(new ReferenceSite(this, RefType.Declaration));
			}
		}
	}

	@Override
	public IGContainerItem computeIG(ArrayList<IGContainerItem> aSpecItems, IGContainer aContainer, IGElaborationEnv aEE) throws ZamiaException {

		IGType type = td.computeIG(aContainer, aEE);

		IGTypeStatic sType = type.computeStaticType(aEE.getInterpreterEnv(), ASTErrorMode.RETURN_NULL, new ErrorReport());
		if (sType != null) {
			type = sType;
		}
		
		IGOperation iv = null;
		if (initialValue != null) {
			try {
				iv = initialValue.computeIGOperation(type, aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);
			} catch (ZamiaException e) {
				reportError(e);
			} catch (Throwable t1) {
				el.logException(t1);
			}
		}

		IGObject s = new IGObject(OIDir.NONE, iv, IGObjectCat.SIGNAL, type, getId(), getLocation(), aEE.getZDB());

		aContainer.add(s);

		return s;
	}

	@Override
	public void dumpVHDL(int indent_, PrintStream out_) throws ZamiaException {
		printIndented(toString() + " ;", indent_, out_);
		out_.println();
	}
}
