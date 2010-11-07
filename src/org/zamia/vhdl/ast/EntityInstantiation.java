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

import org.zamia.DMManager;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGDUUID;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGInstantiation;
import org.zamia.instgraph.IGStructure;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class EntityInstantiation extends InstantiatedUnit {

	private String fArchId;

	public EntityInstantiation(String aLabel, Name aName, String aArchId, VHDLNode aParent, long aLocation) {
		super(aLabel, aName, aParent, aLocation);
		fArchId = aArchId;
	}

	@Override
	public void dumpVHDL(int aIndent, PrintStream aOut) {
		if (fArchId != null) {
			printIndented(fLabel + ": entity " + fName + "(" + fArchId + ")", aIndent, aOut);
		} else {
			aOut.println(fLabel + ": entity " + fName);
		}
	}

	@Override
	public String toString() {
		if (fArchId != null)
			return fLabel + ": entity " + fName + "(" + fArchId + ")";

		return fLabel + ": entity " + fName;
	}

	@Override
	public IGInstantiation computeIGInstantiation(DMUID aDUUID, IGContainer aContainer, IGStructure aStructure, IGElaborationEnv aEE) {
		try {
			IGDUUID igDUUID = fName.computeIGAsDesignUnit(aContainer, aEE, ASTErrorMode.EXCEPTION, null);

			if (igDUUID == null) {
				throw new ZamiaException("Entity name expected here.", fName);
			}

			DMUID duuid = igDUUID.getDUUID();

			DMManager duManager = aEE.getZamiaProject().getDUM();

			Architecture arch = duManager.getArchitecture(duuid.getLibId(), duuid.getId(), fArchId);

			if (arch == null) {
				reportError("EntityInstantiation: Couldn't find '%s'", fName);
				return null;
			}

			return instantiateIGModule(arch, aDUUID, aContainer, aStructure, aEE);
		} catch (ZamiaException e) {
			reportError(e);
		}
		return null;
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache, ReferenceSearchResult aResult,
			ArrayList<SearchJob> aTODO) throws ZamiaException {

		IGDUUID igDUUID = fName.computeIGAsDesignUnit(aContainer, aCache, ASTErrorMode.EXCEPTION, null);

		if (igDUUID == null) {
			throw new ZamiaException("Entity name expected here.", fName);
		}

		DMUID duuid = igDUUID.getDUUID();

		DMManager duManager = aCache.getZamiaProject().getDUM();

		Architecture arch = duManager.getArchitecture(duuid.getLibId(), duuid.getId(), fArchId);

		if (arch == null) {
			reportError("EntityInstantiation: Couldn't find '%s'", fName);
			return;
		}

		findReferences(arch, aId, aDepth, aZPrj, aContainer, aCache, aResult, aTODO);
	}

}
