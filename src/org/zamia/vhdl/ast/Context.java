/*
 * Copyright 2004-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.DUManager;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGLibraryImport;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class Context extends ASTObject {

	private ArrayList<LibraryClause> fLibraries = new ArrayList<LibraryClause>();

	private ArrayList<Use> fUses = new ArrayList<Use>(); // of Use

	public Context(ASTObject parent_, long location_) {
		super(parent_, 0l);
	}

	public void addLibrary(String id_, long l_) {
		LibraryClause lc = new LibraryClause(id_, this, l_);
		fLibraries.add(lc);
	}

	public void addUse(Use aUse) {
		fUses.add(aUse);
		aUse.setParent(this);
	}

	public void addUse(Name aName) throws ZamiaException {
		fUses.add(new Use(aName, this, aName.getLineCol()));
	}

	public int getNumLibraries() {
		return fLibraries.size();
	}

	public LibraryClause getLibrary(int i) {
		return fLibraries.get(i);
	}

	public int getNumUses() {
		return fUses.size();
	}

	public Use getUse(int i) {
		return fUses.get(i);
	}

	@Override
	public ASTObject getChild(int idx_) {
		int n = fLibraries.size();
		if (idx_ >= n) {
			idx_ -= n;
			return fUses.get(idx_);
		}
		return fLibraries.get(idx_);
	}

	@Override
	public int getNumChildren() {
		return fLibraries.size() + fUses.size();
	}

	@Override
	public DeclarativeItem findDeclaration(String id_, ZamiaProject zprj_) {

		DUManager dum = zprj_.getDUM();

		// int n = libraries.size();
		// for (int i = 0; i<n; i++) {
		// LibraryClause lc = libraries.get(i);
		//			
		// if (lc.getId().equals(id_)) {
		// return lc;
		// }
		// }

		int n = fUses.size();
		for (int i = 0; i < n; i++) {
			Use use = fUses.get(i);

			String libId = use.getLibId();

			if (id_.equals(libId)) {
				Library lib = dum.getLibrary(libId);
				return lib;
			}

			String pkgId = use.getPackageId();
			if (pkgId != null) {

				try {
					if (pkgId.equals("ALL")) {

						Entity entity = dum.findEntity(libId, id_);
						if (entity != null)
							return entity;

						VHDLPackage pkg = dum.findPackage(libId, id_);
						if (pkg != null)
							return pkg;
						continue;
					} else {
						String itemId = use.getItemId();
						if (itemId == null)
							continue;

						VHDLPackage pkg = dum.findPackage(libId, pkgId);
						if (pkg != null) {

							if (itemId.equals("ALL")) {
								BlockDeclarativeItem item = pkg.getDeclaration(id_);
								if (item != null)
									return item;
							} else {
								if (itemId.equals(id_)) {
									BlockDeclarativeItem item = pkg.getDeclaration(id_);
									if (item != null)
										return item;

								}
							}
						}

					}
				} catch (ZamiaException e) {
				}
			}
		}

		return null;
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) {

		// FIXME: todo

		logger.warn("%s: findReferences not implemented yet.", getClass());

	}

	public void computeIG(IGContainer aContainer, IGElaborationEnv aEE) {
		// process library-statements

		int n = getNumLibraries();
		for (int i = 0; i < n; i++) {
			LibraryClause lc = getLibrary(i);

			String libId = lc.getId();
			String realId = libId;

			if (libId.equals("WORK")) {
				realId = getLibId();
			}

			IGLibraryImport li = new IGLibraryImport(libId, realId, lc.getLocation(), aEE.getZDB());

			try {
				aContainer.add(li);
			} catch (ZamiaException e) {
				reportError(e);
			}
		}

		// elaborate use-statements

		n = getNumUses();
		for (int i = 0; i < n; i++) {
			Use use = getUse(i);
			try {
				use.computeIG(null, aContainer, aEE);
			} catch (ZamiaException e) {
				reportError(e);
			}
		}
	}

}