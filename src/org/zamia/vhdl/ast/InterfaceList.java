/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 4, 2008
 */
package org.zamia.vhdl.ast;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;

import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.util.HashMapArray;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class InterfaceList extends VHDLNode {

	private HashMapArray<String, InterfaceDeclaration> interfaces;

	public InterfaceList(VHDLNode parent_, long location_) {
		super(parent_, location_);

		interfaces = new HashMapArray<String, InterfaceDeclaration>(1);
	}

	public void add(InterfaceDeclaration id_) throws ZamiaException {
		String id = id_.getId();
		if (interfaces.containsKey(id))
			throw new ZamiaException("Interface named " + id + " defined more than once.", id_);

		interfaces.put(id, id_);
		id_.setParent(this, true);
	}

	@Override
	public VHDLNode getChild(int idx_) {
		return interfaces.get(idx_);
	}

	@Override
	public int getNumChildren() {
		return interfaces.size();
	}

	public InterfaceDeclaration get(String id_) {
		return interfaces.get(id_);
	}

	public InterfaceDeclaration get(int i) {
		return interfaces.get(i);
	}

	public int getNumInterfaces() {
		return interfaces.size();
	}
	
	public Collection<InterfaceDeclaration> all() {
		return this.interfaces.values();
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("(");
		int n = interfaces.size();
		for (int i = 0; i < n; i++) {
			InterfaceDeclaration intf = interfaces.get(i);
			buf.append(intf);

			if (i < n - 1)
				buf.append(", ");
		}
		buf.append(")");
		return buf.toString();
	}

	public void dumpVHDL(int indent_, PrintStream out_) throws ZamiaException {
		int n = interfaces.size();
		for (int i = 0; i < n; i++) {
			InterfaceDeclaration intf = interfaces.get(i);
			printIndented(intf.toString(), indent_, out_);
			if (i < n - 1) {
				out_.print(" ;");
			}
			out_.println();
		}
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
	}

}
