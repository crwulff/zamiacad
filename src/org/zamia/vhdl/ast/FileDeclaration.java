/* 
 * Copyright 2007-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
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
import org.zamia.instgraph.IGType.TypeCat;


/**
 * Intermediate class representing VHDL file declarations
 * 
 * @author Guenter Bartsch
 */

@SuppressWarnings("serial")
public class FileDeclaration extends BlockDeclarativeItem {

	private TypeDefinition td;

	private FileOpenInformation foi;

	public FileDeclaration(String id_, TypeDefinition td_, FileOpenInformation foi_, VHDLNode parent_, long location_) {
		super(id_, parent_, location_);
		td = td_;
		foi = foi_;
		td.setParent(this);
		if (foi != null)
			foi.setParent(this);
	}

	@Override
	public void dump(PrintStream out_) {
		// TODO Auto-generated method stub

	}

	@Override
	public VHDLNode getChild(int idx_) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumChildren() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String toString() {
		return "FILE " + id + " : " + td + " " + foi;
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
		// FIXME: type search.
		if (id_.equals(getId())) {
			result_.add(new ReferenceSite(this, RefType.Declaration));
		}
	}

	@Override
	public IGContainerItem computeIG(ArrayList<IGContainerItem> aSpecItems, IGContainer aContainer, IGElaborationEnv aCache) throws ZamiaException {

		// first, declare the file object

		//String filename = null;
		OIDir mode = OIDir.NONE;

		if (foi != null) {
			OIDir fmode = foi.getMode();
			if (fmode != null) {
				switch (foi.getMode()) {
				case IN:
					mode = OIDir.IN;
					break;
				case BUFFER:
					mode = OIDir.BUFFER;
					break;
				case INOUT:
					mode = OIDir.INOUT;
					break;
				case LINKAGE:
					mode = OIDir.LINKAGE;
					break;
				case OUT:
					mode = OIDir.OUT;
					break;
				}
			}
		}

		// figure out the element type

		IGType t = td.computeIG(aContainer, aCache);

		if (!(t.getCat() == TypeCat.FILE)) {
			throw new ZamiaException("File type expected here.", getLocation());
		}

		IGOperation filePathOp = foi.getStringExpr().computeIGOperation(aContainer.findStringType(), aContainer, aCache, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);
		IGObject fo = new IGObject(mode, filePathOp, IGObjectCat.FILE, t, getId(), getLocation(), aCache.getZDB());
		aContainer.add(fo);

		return fo;
	}
}
