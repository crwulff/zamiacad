/*
 * Copyright 2005-2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jul 20, 2005
 */

package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.SourceFile;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGDesignUnit;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGModule;
import org.zamia.vhdl.ast.DMUID.LUType;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class ConfigurationDeclaration extends PrimaryUnit {

	public ConfigurationDeclaration(Context aContext, String aId, Name aName, SourceFile aSF, long aLinCol, String aLibId, ZDB aZDB) {
		super(aContext, aId, aSF, aLinCol, aLibId, aZDB);
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	@Override
	public VHDLNode getChild(int aIdx) {
		return getContext();
	}

	@Override
	public DMUID getDMUID(String aLibId) throws ZamiaException {
		return new DMUID(LUType.Configuration, aLibId, getId(), null);
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aEE,
			ReferenceSearchResult aResult, ArrayList<SearchJob> aTODO) throws ZamiaException {
		// FIXME: implement
	}

	@Override
	public void computeIG(IGManager aIGM, IGDesignUnit aDesignModule) {
		// FIXME: implement
	}

	@Override
	public void computeStatementsIG(IGManager aIGM, IGModule aModule) {
		// FIXME: implement
	}

}
