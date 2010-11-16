/*
 * Copyright 2004-2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.DMManager;
import org.zamia.SourceFile;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGContainerItem;
import org.zamia.instgraph.IGDesignUnit;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGInstantiation;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGLibraryImport;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGModule;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGResolveResult;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.DMUID.LUType;
import org.zamia.zdb.ZDB;

/**
 * A VHDL Entity.
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class Entity extends PrimaryUnit {

	private InterfaceList fPorts, fGenerics;

	private ArrayList<ConcurrentStatement> fCSS;

	public Entity(Context aContext, String aId, SourceFile aFile, long aLocation, String aLibId, ZDB aZDB) {
		super(aContext, aId, aFile, aLocation, aLibId, aZDB);
		fCSS = new ArrayList<ConcurrentStatement>(1);
	}

	public void setPorts(InterfaceList aIds) {
		fPorts = aIds;
		if (fPorts != null)
			fPorts.setParent(this);
	}

	public InterfaceList getPorts() {
		return fPorts;
	}

	public void setGenerics(InterfaceList aIds) {
		fGenerics = aIds;
		if (fGenerics != null)
			fGenerics.setParent(this);
	}

	public InterfaceList getGenerics() {
		return fGenerics;
	}

	public InterfaceDeclaration findInterfaceDeclaration(String aId) {
		return fPorts.get(aId);
	}

	public String getId() {
		return id;
	}

	public String toString() {
		return "Entity " + id + "@" + hashCode() + " from " + getLocation();
	}

	public int getNumInterfaceDeclarations() {
		if (fPorts == null)
			return 0;
		return fPorts.getNumInterfaces();
	}

	public InterfaceDeclaration getInterfaceDeclaration(int aIdx) {
		return fPorts.get(aIdx);
	}

	@Override
	public int getNumChildren() {
		return 3;
	}

	@Override
	public VHDLNode getChild(int aIdx) {

		switch (aIdx) {
		case 0:
			return getContext();
		case 1:
			return fGenerics;
		case 2:
			return fPorts;
		}
		return null;
	}

	@Override
	public DeclarativeItem findDeclaration(String aId, ZamiaProject aZPrj) {

		DeclarativeItem decl = null;
		if (fPorts != null) {
			decl = fPorts.get(aId);
			if (decl != null)
				return decl;
		}
		if (fGenerics != null) {
			decl = fGenerics.get(aId);
			if (decl != null)
				return decl;
		}

		try {

			IGManager igm = aZPrj.getIGM();

			DMUID duuid = getDMUID();

			DMManager dum = aZPrj.getDUM();

			duuid = dum.getArchDUUID(duuid);

			String signature = IGInstantiation.computeSignature(duuid, null);

			IGModule module = igm.findModule(signature);

			if (module == null) {
				logger.error("SA: IGModule for %s not found.", duuid);
				return null;
			}

			IGContainer container = module.getContainer();

			IGResolveResult rres = container.resolve(aId);

			int n = rres.getNumResults();
			for (int i = 0; i < n; i++) {
				IGItem item = rres.getResult(i);
				if (item instanceof IGLibraryImport) {
					IGLibraryImport ip = (IGLibraryImport) item;

					String libId = ip.getRealId();

					return dum.getLibrary(libId);
				}
				// FIXME
				//					if (item != null) {
				//						ASTObject src = item.getSrc();
				//						if (src instanceof DeclarativeItem) {
				//							return (DeclarativeItem) src;
				//						}
				//					}
			}

		} catch (ZamiaException e) {
			el.logZamiaException(e);
		}

		return super.findDeclaration(aId, aZPrj);
	}

	public void add(ConcurrentStatement aCS) {
		if (aCS != null) {
			fCSS.add(aCS);
			aCS.setParent(this);
		}
	}

	@Override
	public DMUID getDMUID(String aLibId) throws ZamiaException {
		return new DMUID(LUType.Entity, aLibId, getId(), null);
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aResult, ArrayList<SearchJob> aTODO) throws ZamiaException {
		// FIXME: type search

		int n = getNumDeclarations();

		for (int i = 0; i < n; i++) {
			BlockDeclarativeItem decl = getDeclaration(i);
			if (decl != null) {
				decl.findReferences(aId, aCat, aRefType, aDepth + 1, aZPrj, aContainer, aCache, aResult, aTODO);
			}
		}

		n = getNumInterfaceDeclarations();
		for (int i = 0; i < n; i++) {

			InterfaceDeclaration idecl = getInterfaceDeclaration(i);

			if (idecl != null) {
				idecl.findReferences(aId, aCat, aRefType, aDepth + 1, aZPrj, aContainer, aCache, aResult, aTODO);
			}
		}
	}

	@Override
	public void collectIdentifiers(HashSetArray<String> aIdentifiers, ZamiaProject aZPrj) {
		super.collectIdentifiers(aIdentifiers, aZPrj);

		if (fPorts != null) {
			int n = fPorts.getNumInterfaces();
			for (int i = 0; i < n; i++) {
				InterfaceDeclaration intf = fPorts.get(i);
				aIdentifiers.add(intf.getId());
			}
		}

		if (fGenerics != null) {

			int n = fGenerics.getNumInterfaces();
			for (int i = 0; i < n; i++) {
				InterfaceDeclaration intf = fGenerics.get(i);
				aIdentifiers.add(intf.getId());
			}
		}
	}

	public void computeEntityIG(IGModule aModule, IGContainer aContainer, IGElaborationEnv aEE) {

		IGInterpreterRuntimeEnv env = aEE.getInterpreterEnv();

		fContext.computeIG(aContainer, aEE);

		// generics

		if (fGenerics != null) {

			int nActualGenerics = aModule.getNumActualGenerics();
			int n = fGenerics.getNumInterfaces();
			for (int i = 0; i < n; i++) {
				try {
					InterfaceDeclaration interf = fGenerics.get(i);

					IGObject igg = (IGObject) interf.computeIG(null, aContainer, aEE);

					aContainer.addGeneric(igg);

					env.newObject(igg, ASTErrorMode.EXCEPTION, null, interf.getLocation());

					if (i < nActualGenerics) {
						IGStaticValue actualGeneric = aModule.getActualGeneric(i);
						env.setObjectValue(igg, actualGeneric, actualGeneric.computeSourceLocation());
					}

				} catch (ZamiaException e) {
					reportError(e);
				} catch (Throwable t) {
					el.logException(t);
				}
			}
		}

		// ports

		if (fPorts != null) {
			int n = fPorts.getNumInterfaces();
			for (int i = 0; i < n; i++) {
				try {
					InterfaceDeclaration interf = (InterfaceDeclaration) fPorts.get(i);

					IGContainerItem igi = interf.computeIG(null, aContainer, aEE);

					aContainer.addInterface((IGObject) igi);

					if (igi instanceof IGObject) {
						env.newObject((IGObject) igi, ASTErrorMode.EXCEPTION, null, interf.getLocation());
					}

				} catch (ZamiaException e) {
					reportError(e);
				} catch (Throwable t) {
					el.logException(t);
				}
			}
		}

		// declarations:

		int n = fDeclarations.size();
		for (int i = 0; i < n; i++) {
			try {
				BlockDeclarativeItem decl = getDeclaration(i);
				IGContainerItem item = decl.computeIG(null, aContainer, aEE);

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

		// statements

		// FIXME
		//		n = css.size();
		//		for (int i = 0; i < n; i++) {
		//			ConcurrentStatement cs = css.get(i);
		//			cs.computeIG(aContainer, aCache);
		//		}

	}

	@Override
	public void computeIG(IGManager aIGM, IGDesignUnit aDesignModule) {
	}

	@Override
	public void computeStatementsIG(IGManager aIGM, IGModule aModule) {
	}
}