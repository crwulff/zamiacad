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


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class ComponentDeclaration extends BlockDeclarativeItem {

	private InterfaceList interfaces;

	private InterfaceList generics;

	public ComponentDeclaration(String id_, VHDLNode parent_, long location_) {
		super(id_, parent_, location_);
	}

	public void setInterfaces(InterfaceList il_) {
		interfaces = il_;
		if (interfaces != null)
			interfaces.setParent(this);
	}

	public InterfaceList getInterfaces() {
		return interfaces;
	}

	public InterfaceDeclaration findInterfaceDeclaration(String id_) {
		return interfaces.get(id_);
	}

	public void setGenerics(InterfaceList il_) {
		generics = il_;
		if (generics != null)
			generics.setParent(this);
	}

	public InterfaceList getGenerics() {
		return generics;
	}

	public InterfaceDeclaration findGeneric(String id_) {
		return generics.get(id_);
	}

	public void dump(PrintStream out_) {
		out_.println("Component id=" + id);
	}

	@Override
	public int getNumChildren() {
		return 2;
	}

	@Override
	public VHDLNode getChild(int idx_) {
		if (idx_ == 0)
			return generics;
		return interfaces;
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
		if (id_.equals(getId())) {
			result_.add(new ReferenceSite(this, RefType.Declaration));
		}
	}

	@Override
	public String toString() {
		String res = "COMPONENT " + id + " IS ";

		if (generics != null) {
			res += "GENERIC( " + generics + ");";
		}

		if (interfaces != null) {
			res += "PORT( " + interfaces + ");";
		}

		res += "END COMPONENT";
		return res;
	}

	@Override
	public void dumpVHDL(int indent_, PrintStream out_) throws ZamiaException {
		printIndented("COMPONENT " + getId() + " IS", indent_, out_);
		out_.println();
		if (generics != null) {
			printIndented("GENERIC ( ", indent_ + 2, out_);
			out_.println();
			generics.dumpVHDL(indent_ + 2, out_);
			printIndented(") ; ", indent_ + 2, out_);
			out_.println();
		}
		if (interfaces != null) {
			printIndented("PORT ( ", indent_ + 2, out_);
			out_.println();
			interfaces.dumpVHDL(indent_ + 4, out_);
			printIndented(") ; ", indent_ + 2, out_);
			out_.println();
		}
		printIndented("END COMPONENT " + getId() + ";", indent_, out_);
		out_.println();
	}

	public IGContainerItem computeIG(ArrayList<IGContainerItem> aSpecItems, IGContainer aContainer, IGElaborationEnv aCache) throws ZamiaException {
		// component declarations do not exist in IG world for now
		// FIXME: implement signature checking
		return null;
	}
}
