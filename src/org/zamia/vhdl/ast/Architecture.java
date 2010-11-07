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

import org.zamia.DMManager;
import org.zamia.IASTNodeVisitor;
import org.zamia.SourceFile;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.ASTDeclarationSearch;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGContainerItem;
import org.zamia.instgraph.IGDesignUnit;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGInstantiation;
import org.zamia.instgraph.IGLibraryImport;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGModule;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGStructure;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.DMUID.LUType;
import org.zamia.zdb.ZDB;


/**
 * A VHDL architecture.
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class Architecture extends SecondaryUnit {

	public static final boolean dump = false;

	private ArrayList<Long> fCSS;

	private Name fEntityName;

	public Architecture(Context aContext, String aId, Name aEntityName, ZamiaProject aZPrj, SourceFile aSF, long aLocation, String aLibId, ZDB aZDB) {
		super(aContext, aId, aSF, aLocation, aLibId, aZDB);
		fCSS = new ArrayList<Long>();

		fEntityName = aEntityName;

		aEntityName.setParent(this);
	}

	public void add(ConcurrentStatement aCS) throws ZamiaException {
		if (aCS != null) {
			aCS.setParent(null, true);
			ZDB zdb = getZDB();
			fCSS.add(zdb.storeNow(aCS));
		}
	}

	public String getId() {
		return id;
	}

	public Context getContext() {
		return fContext;
	}

	public String toString() {
		return fEntityName.getId() + "(" + id + ")";
	}

	public void dump(PrintStream aOut) {
		aOut.println("Architecture id=" + id);
		aOut.println("====================================");
		aOut.println();

		aOut.println("  Signal declarations:");

		int n = fDeclarations.size();
		for (int i = 0; i < n; i++) {
			BlockDeclarativeItem item = getDeclaration(i);
			item.dump(aOut);
		}

		// out_.println (" Concurrent statements:");
		//
		// for (Iterator i = css.iterator(); i.hasNext();) {
		// ConcurrentStatement cs = (ConcurrentStatement) i.next();
		// cs.dump();
		// }
	}

	public Name getEntityName() {
		return fEntityName;
	}

	public int getNumConcurrentStatements() {
		return fCSS.size();
	}

	public ConcurrentStatement getConcurrentStatement(int aIdx) {
		Long l = fCSS.get(aIdx);
		if (l == null) {
			return null;
		}
		ZDB zdb = getZDB();
		ConcurrentStatement cs = (ConcurrentStatement) zdb.load(l.longValue());
		if (cs == null) {
			return null;
		}
		cs.setParent(this, true);
		return cs;
	}

	@Override
	public int getNumChildren() {
		return getNumDeclarations() + getNumConcurrentStatements() + 1;
	}

	@Override
	public VHDLNode getChild(int aIdx) {

		if (aIdx == 0)
			return getContext();
		aIdx--;

		int n = getNumDeclarations();
		if (aIdx >= n) {
			return getConcurrentStatement(aIdx - n);
		}
		return getDeclaration(aIdx);
	}

	@Override
	public DeclarativeItem findDeclaration(String aId, ZamiaProject aZPrj) {

		try {
			if (ASTDeclarationSearch.dump) {
				logger.debug("SA: Architecture '%s' findDeclaration('%s')", toString(), aId);
			}
			DeclarativeItem decl = super.findDeclaration(aId, aZPrj);
			if (decl != null) {
				if (ASTDeclarationSearch.dump) {
					logger.debug("SA: super.findDeclaration succeeded: '%s'", decl);
				}
				return decl;
			}

			IGManager igm = aZPrj.getIGM();

			DMUID duuid = getDMUID();

			String signature = IGInstantiation.computeSignature(duuid, null);

			IGModule module = igm.findModule(signature);

			if (module == null) {
				logger.error("SA: IGModule for %s not found.", duuid);
				return null;
			}

			IGElaborationEnv cache = new IGElaborationEnv(aZPrj);

			Entity entity = findEntity(module.getContainer(), cache);

			if (entity != null) {
				if (ASTDeclarationSearch.dump) {
					logger.debug("SA: entity found, asking entity for declaration.");
				}
				decl = entity.findDeclaration(aId, aZPrj);
				if (decl != null) {
					return decl;
				} else {
					logger.error("SA: entity '%s' didn't find declaration for '%s'", entity, aId);
				}
			} else {
				logger.error("SA: failed to find entity '%s' in context", fEntityName);
			}

		} catch (ZamiaException e) {
			el.logZamiaException(e);
		}

		return null;
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aResult, ArrayList<SearchJob> aTODO) throws ZamiaException {

		int n = getNumDeclarations();

		for (int i = 0; i < n; i++) {
			BlockDeclarativeItem decl = getDeclaration(i);
			if (decl != null) {
				decl.findReferences(aId, aCat, aRefType, aDepth + 1, aZPrj, aContainer, aCache, aResult, aTODO);
			}
		}

		IGManager igm = aZPrj.getIGM();

		DMUID duuid = getDMUID();

		String signature = IGInstantiation.computeSignature(duuid, null);

		IGModule module = igm.findModule(signature);

		if (module == null) {
			logger.error("SA: IGModule for %s not found.", duuid);
			return;
		}

		IGElaborationEnv cache = new IGElaborationEnv(aZPrj);

		if (aDepth == 0) {

			Entity entity = findEntity(module.getContainer(), cache);

			if (entity != null) {
				entity.findReferences(aId, aCat, aRefType, aDepth, aZPrj, module.getContainer(), cache, aResult, aTODO);
			} else {
				logger.error("SA: Failed to find entity for %s", module);
			}
		}

		n = getNumConcurrentStatements();

		for (int i = 0; i < n; i++) {
			ConcurrentStatement cs = getConcurrentStatement(i);
			if (cs != null) {
				try {
					cs.findReferences(aId, aCat, aRefType, aDepth + 1, aZPrj, module.getContainer(), cache, aResult, aTODO);
				} catch (ZamiaException e) {
					el.logZamiaException(e);
				}
			}
		}
	}

	@Override
	public void collectIdentifiers(HashSetArray<String> aRes, ZamiaProject aZPrj) {
		super.collectIdentifiers(aRes, aZPrj);

		try {
			IGManager igm = aZPrj.getIGM();

			DMUID duuid = getDMUID();

			String signature = IGInstantiation.computeSignature(duuid, null);

			IGModule module = igm.findModule(signature);

			if (module == null) {
				logger.error("SA: IGModule for %s not found.", duuid);
				return;
			}

			IGElaborationEnv cache = new IGElaborationEnv(aZPrj);

			Entity entity = findEntity(module.getContainer(), cache);

			if (entity != null) {
				logger.debug("Architecture.collectIdentifiers(): entity found, asking entity for declaration.");

				entity.collectIdentifiers(aRes, aZPrj);
			} else {
				logger.error("SA: Failed to find entity for %s", module);
			}

		} catch (ZamiaException e) {
			el.logZamiaException(e);
		}
	}

	@Override
	public DMUID getDMUID(String aLibId) throws ZamiaException {
		return new DMUID(LUType.Architecture, aLibId, fEntityName.getId(), getId());
	}

	public Entity findEntity(IGContainer aContainer, IGElaborationEnv aCache) throws ZamiaException {
		// find entity in current work library

		IGLibraryImport li = aContainer.resolveLibrary("WORK");

		DMManager dum = aCache.getZamiaProject().getDUM();

		Entity entity = dum.findEntity(li.getRealId(), fEntityName.getId());

		return entity;
	}

	@Override
	public void computeIG(IGManager aIGM, IGDesignUnit aModule) {

		if (!(aModule instanceof IGModule)) {
			reportError("Architecture: Internal error: computeIG() expects an IGModule. Got instead: "+aModule);
			return;
		}
		
		final IGModule module = (IGModule) aModule;
		
		/*
		 * basic infrastructure for architecture elaboration
		 */

		IGStructure structure = module.getStructure();

		IGContainer container = structure.getContainer();

		ZamiaProject zprj = aIGM.getProject();

		IGElaborationEnv cache = new IGElaborationEnv(zprj);

		/*
		 * set up an interpreter environment which will be used to 
		 * elaborate generate statements and compute static constants
		 * 
		 * it will be bootstrapped by the actual generics
		 * 
		 */

		IGInterpreterCode ic = new IGInterpreterCode("Architecture " + this, getLocation());
		IGInterpreterRuntimeEnv env = new IGInterpreterRuntimeEnv(ic, zprj);
		env.pushContext(zprj.getDUM().getGlobalPackageContext());
		env.pushContext(structure.getInterpreterContext());

		cache.setInterpreterEnv(env);

		/*
		 * context
		 */

		fContext.computeIG(container, cache);

		Entity entity = null;

		try {
			entity = findEntity(container, cache);
		} catch (ZamiaException e) {
			reportError(e);
		}

		if (entity == null) {

			reportError("Entity " + fEntityName + " not found.");

			return;
		}

		/*
		 * entity ig
		 */

		entity.computeEntityIG(module, container, cache);

		container.storeOrUpdate();
		aModule.storeOrUpdate();
	}

	@Override
	public void computeStatementsIG(IGManager aIGM, IGModule aModule) {

		ZamiaProject zprj = aIGM.getProject();

		IGElaborationEnv cache = new IGElaborationEnv(zprj);

		IGStructure structure = aModule.getStructure();
		IGContainer container = structure.getContainer();

		/*
		 * set up an interpreter environment 
		 */

		IGInterpreterCode ic = new IGInterpreterCode("Architecture " + this, getLocation());
		IGInterpreterRuntimeEnv env = new IGInterpreterRuntimeEnv(ic, zprj);
		env.pushContext(zprj.getDUM().getGlobalPackageContext());
		env.pushContext(structure.getInterpreterContext());

		cache.setInterpreterEnv(env);

		int n = getNumDeclarations();
		for (int i = 0; i < n; i++) {

			BlockDeclarativeItem decl = getDeclaration(i);

			try {
				IGContainerItem item = decl.computeIG(null, container, cache);
				if (item instanceof IGObject) {
					IGObject obj = (IGObject) item;
					env.newObject(obj, decl.getLocation());
				}
			} catch (ZamiaException e) {
				reportError(e);
			} catch (Throwable t) {
				el.logException(t);
			}
		}

		n = getNumConcurrentStatements();
		for (int i = 0; i < n; i++) {
			ConcurrentStatement stmt = getConcurrentStatement(i);

			if (stmt != null) {
				try {
					stmt.computeIG(aModule.getDUUID(), structure.getContainer(), structure, cache);
				} catch (ZamiaException e) {
					reportError(e);
				} catch (Throwable t) {
					el.logException(t);
				}
			}
		}

		aModule.storeOrUpdate();
	}

	@Override
	public void dumpVHDL(int aIndent, PrintStream aOut) throws ZamiaException {
		printIndented("ARCHITECTURE " + getId() + " OF " + fEntityName + " IS", aIndent, aOut);
		aOut.println();
		int n = fDeclarations.size();
		for (int i = 0; i < n; i++) {
			BlockDeclarativeItem item = getDeclaration(i);
			item.dumpVHDL(aIndent + 2, aOut);
		}
		printIndented("BEGIN", aIndent, aOut);
		aOut.println();
		n = fCSS.size();
		for (int i = 0; i < n; i++) {
			ConcurrentStatement cs = getConcurrentStatement(i);
			cs.dumpVHDL(aIndent + 2, aOut);
		}
		printIndented("END ARCHITECTURE " + getId() + " ;", aIndent, aOut);
		aOut.println();
	}

}