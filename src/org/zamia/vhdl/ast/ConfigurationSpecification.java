/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jul 17, 2009
 */
package org.zamia.vhdl.ast;

import java.io.PrintStream;
import java.util.ArrayList;

import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
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
public class ConfigurationSpecification extends BlockDeclarativeItem {

	private BindingIndication fBI;

	private Name fComponentName;

	public ConfigurationSpecification(BindingIndication aBI, Name aComponentName, String aId, VHDLNode aParent, long aLocation) {
		super(aId, aParent, aLocation);

		fBI = aBI;
		fComponentName = aComponentName;
		if (fBI != null) {
			fBI.setParent(this);
		}
		if (fComponentName != null) {
			fComponentName.setParent(this);
		}
	}

	@Override
	public void dump(PrintStream aOut) {
		aOut.println("*** ERROR: dont' know how to dump " + this); // FIXME
	}

	@Override
	public IGContainerItem computeIG(ArrayList<IGContainerItem> aSpecItems, IGContainer aContainer, IGElaborationEnv aCache) throws ZamiaException {
		aContainer.add(this);
		return null;
	}

	@Override
	public void findReferences(String aId, ObjectCat aCategory, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aResult, ArrayList<SearchJob> aTODO) throws ZamiaException {
		throw new ZamiaException("Sorry, not implemented.", this);
	}

	@Override
	public VHDLNode getChild(int aIdx) {
		return aIdx == 0 ? fBI : fComponentName;
	}

	@Override
	public int getNumChildren() {
		return 2;
	}

	public Name getComponentName() {
		return fComponentName;
	}

	public BindingIndication getBindingIndication() {
		return fBI;
	}

	@Override
	public String toString() {
		return "FOR " + getId() + ":" + fComponentName + " " + fBI + ";";
	}
}
