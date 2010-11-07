/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jul 23, 2005
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
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGInstantiation;
import org.zamia.instgraph.IGStructure;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class ConfigurationInstantiation extends InstantiatedUnit {

	public ConfigurationInstantiation(String aLabel, Name aName, VHDLNode aParent, long aLocation) {
		super(aLabel, aName, aParent, aLocation);
	}

	@Override
	public void dumpVHDL(int aIndent, PrintStream aOut) {
		// FIXME: implement
		aOut.println("-- ERROR: don't know how to dump " + this);
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aResult, ArrayList<SearchJob> aTODO) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException("ConfigurationInstantiation.findReferences: not implemented yet, sorry.", this);
	}

	@Override
	public IGInstantiation computeIGInstantiation(DMUID aDUUID, IGContainer aContainer, IGStructure aStructure, IGElaborationEnv aEE) throws ZamiaException {
		// FIXME
		throw new ZamiaException("ConfigurationInstantiation.computeIG(): Sorry, not implemented yet.", this);
	}


}
