/*
 * Copyright 2004-2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.vhdl.ast;

import java.util.ArrayList;
import java.util.HashSet;

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

		for (BlockDeclarativeItem decl : fDeclarations) {
			if (decl != null) {
				decl.findReferences(aId, aCat, aRefType, aDepth + 1, aZPrj, aContainer, aCache, aResult, aTODO);
			}
		}

		for (InterfaceDeclaration idecl : fPorts) {

			if (idecl != null) {
				idecl.findReferences(aId, aCat, aRefType, aDepth + 1, aZPrj, aContainer, aCache, aResult, aTODO);
			}
		}
	}

	@Override
	public void collectIdentifiers(HashSetArray<String> aIdentifiers, ZamiaProject aZPrj) {
		super.collectIdentifiers(aIdentifiers, aZPrj);

		if (fPorts != null) {
			for (InterfaceDeclaration interf : fPorts) {
				aIdentifiers.add(interf.getId());
			}
		}

		if (fGenerics != null) {

			for (InterfaceDeclaration interf : fGenerics) {
				aIdentifiers.add(interf.getId());
			}
		}
	}

	abstract class DeclarationElaborator {
		protected abstract void body(DeclarativeItem interf, int i) throws ZamiaException;
		public DeclarationElaborator (Iterable<? extends DeclarativeItem> c) {
			int i = 0;
			for (DeclarativeItem interf: c) {
				try {
					body(interf, i++);
				} catch (ZamiaException e) {
					reportError(e);
				} catch (Throwable t) {
					el.logException(t);
				}
			}
		}
		
	}

	
	public void computeEntityIG(final IGModule aModule, final IGContainer aContainer, final IGElaborationEnv aEE) {

		final IGInterpreterRuntimeEnv env = aEE.getInterpreterEnv();

		fContext.computeIG(aContainer, aEE);

		class Helper extends DeclarationElaborator {
			protected void body2(IGObject igg, DeclarativeItem interf, int i) throws ZamiaException {
				env.newObject((IGObject) igg, ASTErrorMode.EXCEPTION, null, interf.getLocation());
			}

			protected void body(DeclarativeItem interf, int i) throws ZamiaException {
				IGContainerItem igg = interf.computeIG(null, aContainer, aEE);
				if (igg instanceof IGObject)
					body2((IGObject) igg, interf, i);
			};
			Helper (Iterable<? extends DeclarativeItem> c) { super(c); }
		}
		
		// generics

		if (fGenerics != null) {
			final int nActualGenerics = aModule.getNumActualGenerics();
			new Helper(fGenerics) {
				
				@Override
				protected void body2(IGObject igg, DeclarativeItem interf, int i) throws ZamiaException {
					aContainer.addGeneric(igg);

					env.newObject(igg, ASTErrorMode.EXCEPTION, null, interf.getLocation());

					if (i < nActualGenerics) {
						IGStaticValue actualGeneric = aModule.getActualGeneric(i);
						env.setObjectValue(igg, actualGeneric, actualGeneric.computeSourceLocation());
					}
				}

			};
		}

		if (fPorts != null) new Helper(fPorts) { 
			
			@Override
			protected void body2(IGObject igg, DeclarativeItem interf, int i) throws ZamiaException {

				
				aContainer.addInterface(igg);

				env.newObject(igg, ASTErrorMode.EXCEPTION, null, interf.getLocation());

			}
		};

		new Helper(fDeclarations);
		
		// statements

		// FIXME
		//		n = css.size();
		//		for (int i = 0; i < n; i++) {
		//			ConcurrentStatement cs = css.get(i);
		//			cs.computeIG(aContainer, aCache);
		//		}

	}

	/**
	 * Same as {@link #computeEntityIG(IGModule, IGContainer, IGElaborationEnv)}, but doesn't create new objects.
	 * Takes them from within the specified aContainer instead.
	 *
	 * @param aModule
	 * @param aContainer
	 * @param aEE
	 */
	void initEnv(final IGModule aModule, final IGContainer aContainer, IGElaborationEnv aEE) {

		final IGInterpreterRuntimeEnv env = aEE.getInterpreterEnv();

		final HashSet<Long> processedItems = new HashSet<Long>(aContainer.getNumLocalItems());

		// generics
		
		if (fGenerics != null) {
			final int nActualGenerics = aModule.getNumActualGenerics();
			new DeclarationElaborator(fGenerics) {
				
				protected void body(DeclarativeItem interf, int i) throws ZamiaException {
					IGObject igg = aContainer.getGeneric(i);

					processedItems.add(igg.getDBID());

					env.newObject(igg, ASTErrorMode.EXCEPTION, null, interf.getLocation());

					if (i < nActualGenerics) {
						IGStaticValue actualGeneric = aModule.getActualGeneric(i);
						env.setObjectValue(igg, actualGeneric, actualGeneric.computeSourceLocation());
					}
				};				
			};
		}

		// ports

		if (fPorts != null) {
			new DeclarationElaborator(fPorts) {
				protected void body(DeclarativeItem interf, int i) throws ZamiaException { 
					IGContainerItem igi = aContainer.getInterface(i);

					processedItems.add(igi.getDBID());

					if (igi instanceof IGObject) {
						env.newObject((IGObject) igi, ASTErrorMode.EXCEPTION, null, interf.getLocation());
					}
				}
			};
		}

		// declarations:

		for (IGContainerItem item : aContainer.localItems()) {
			try {

				if (processedItems.contains(item.getDBID())) {
					continue;
				}

				if (item instanceof IGObject) {
					IGObject obj = (IGObject) item;
					env.newObject(obj, ASTErrorMode.EXCEPTION, null, item.computeSourceLocation());
				}

			} catch (ZamiaException e) {
				reportError(e);
			} catch (Throwable t) {
				el.logException(t);
			}
		}
	}

	@Override
	public void computeIG(IGManager aIGM, IGDesignUnit aDesignModule) {
	}

	@Override
	public void computeStatementsIG(IGManager aIGM, IGModule aModule) {
	}
}