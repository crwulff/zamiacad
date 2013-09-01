/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
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
import org.zamia.instgraph.IGContainerItem;
import org.zamia.instgraph.IGDesignUnit;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGModule;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.vhdl.ast.DMUID.LUType;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class PackageBody extends SecondaryUnit {

	public PackageBody(Context aContext, String aId, SourceFile aSF, long aLinCol, String aLibId, ZDB aZDB) {
		super(aContext, aId, aSF, aLinCol, aLibId, aZDB);

	}

	@Override
	public VHDLNode getChild(int aIdx) {
		return getDeclaration(aIdx);
	}

	@Override
	public int getNumChildren() {
		return fDeclarations.size();
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aEE,
			ReferenceSearchResult aResult, ArrayList<SearchJob> aTODO) throws ZamiaException {

		for (BlockDeclarativeItem decl : fDeclarations) {
			if (decl != null) {
				decl.findReferences(aId, aCat, aRefType, aDepth + 1, aZPrj, aContainer, aEE, aResult, aTODO);
			}
		}
	}

	@Override
	public DMUID getDMUID(String aLibId) throws ZamiaException {
		return new DMUID(LUType.PackageBody, aLibId, getId(), null);
	}

	public void computePackageBodyIG(IGContainer aContainer, IGElaborationEnv aCache) throws ZamiaException {

		IGContainer container = new IGContainer(aContainer.getDBID(), getLocation(), aCache.getZDB());
		
		fContext.computeIG(container, aCache);
		
		IGInterpreterRuntimeEnv env = aCache.getInterpreterEnv();
		
		for (BlockDeclarativeItem decl : fDeclarations) {
			
			try {
				logger.info("PackageBody: Elaborating %s", decl);

				ArrayList<IGContainerItem> specItems = aContainer.findLocalItems(decl.getId());
				
				//if (specItem != null) {
				//	logger.info("PackageBody:  found specification item: %s", specItem);
				//}
				
				IGContainerItem item = decl.computeIG(specItems, container, aCache);
				if (item instanceof IGObject) {
					IGObject obj = (IGObject) item;
					env.newObject(obj, ASTErrorMode.EXCEPTION, null, decl.getLocation());
				}
			} catch (ZamiaException e) {
				reportError(e);
			} catch (Throwable t) {
				el.logException(t);
			}
		}
	}

	@Override
	public String toString() {
		return "PACKAGE BODY "+getId();
	}

	@Override
	public void computeIG(IGManager aIGM, IGDesignUnit aDesignModule) {
	}

	@Override
	public void computeStatementsIG(IGManager aIGM, IGModule aModule) {
	}
}
