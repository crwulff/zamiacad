/*
 * Copyright 2006-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.vhdl.ast;

import java.io.PrintStream;
import java.util.ArrayList;

import org.zamia.DMManager;
import org.zamia.ERManager;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGContainerItem;
import org.zamia.instgraph.IGDUUID;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGLibraryImport;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGPackage;
import org.zamia.instgraph.IGPackageImport;
import org.zamia.vhdl.ast.DMUID.LUType;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class Use extends BlockDeclarativeItem {
	private String fLibId, fPackageId, fItemId;

	public Use(Name aName, VHDLNode aParent, long aLocation) throws ZamiaException {
		super(null, aParent, aLocation);

		aName.setParent(this);

		switch (aName.getNumExtensions()) {
		case 0:
			fPackageId = aName.getId();
			break;

		case 1:
			fLibId = aName.getId();
			NameExtension ext = aName.getExtension(0);
			if (!(ext instanceof NameExtensionSuffix))
				throw new ZamiaException("Illegal name in use.", getLocation());
			NameExtensionSuffix suffix = (NameExtensionSuffix) ext;
			fPackageId = suffix.getSuffix().getId();
			break;

		case 2:
			fLibId = aName.getId();
			ext = aName.getExtension(0);
			if (!(ext instanceof NameExtensionSuffix))
				throw new ZamiaException("Illegal name in use.", getLocation());
			suffix = (NameExtensionSuffix) ext;
			fPackageId = suffix.getSuffix().getId();
			ext = aName.getExtension(1);
			if (!(ext instanceof NameExtensionSuffix))
				throw new ZamiaException("Illegal name in use.", getLocation());
			suffix = (NameExtensionSuffix) ext;
			fItemId = suffix.getSuffix().getId();
			break;

		default:
			throw new ZamiaException("Illegal name in use.", getLocation());
		}
	}

	public String getItemId() {
		return fItemId;
	}

	public String getLibId() {
		return fLibId;
	}

	public String getPackageId() {
		return fPackageId;
	}

	@Override
	public VHDLNode getChild(int aIdx) {
		return null;
	}

	@Override
	public int getNumChildren() {
		return 0;
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aResult, ArrayList<SearchJob> aTODO) {

		// FIXME: todo

		logger.warn("%s: findReferences not implemented yet.", getClass());

	}

	@Override
	public String toString() {
		return "use " + fLibId + "." + fPackageId + "." + fItemId;
	}

	@Override
	public void dump(PrintStream aOut) {
		aOut.print(this);
	}

	@Override
	public IGContainerItem computeIG(ArrayList<IGContainerItem> aSpecItems, IGContainer aContainer, IGElaborationEnv aCache) throws ZamiaException {
		ZamiaProject zprj = aCache.getZamiaProject();
		ERManager erm = zprj.getERM();
		DMManager dum = zprj.getDUM();
		IGManager igm = zprj.getIGM();

		Library l = null;
		if (fLibId != null) {

			String libId = fLibId;
			IGLibraryImport li2 = aContainer.resolveLibrary(fLibId);
			if (li2 == null) {
				String msg = "Warning: Library " + fLibId + " not declared.";
				logger.warn(msg);
				erm.addError(new ZamiaException(msg, getLocation()));
			} else {
				libId = li2.getRealId();
			}
			l = dum.getLibrary(libId);
		} else {
			l = dum.getLibrary(super.getLibId());
		}

		if (l == null) {
			String msg = "Warning: Library " + fLibId + " not declared.";
			throw new ZamiaException(msg, this);
		}

		String pkgId = fPackageId;
		if (!pkgId.equals("ALL")) {

			Entity e = null;

			try {
				e = dum.findEntity(l.getId(), pkgId);
			} catch (ZamiaException e1) {
				reportError(e1);
			}

			if (e == null) {

				IGPackage pkg = igm.findPackage(l.getId(), pkgId, getLocation());

				if (pkg == null) {

					String msg = "Warning: Library unit " + pkgId + " not found in " + l;
					throw new ZamiaException(msg, this);

				} else {

					if (fItemId == null) {

						// use lib.pkg;

						IGPackageImport pi = new IGPackageImport(l.getId(), pkgId, null, false, getLocation(), aCache.getZDB());

						try {
							aContainer.add(pi);
						} catch (ZamiaException e1) {
							reportError(e1);
						}

					} else if (fItemId.equals("ALL")) {

						IGPackageImport pi = new IGPackageImport(l.getId(), pkgId, null, true, getLocation(), aCache.getZDB());

						try {
							aContainer.add(pi);
						} catch (ZamiaException e1) {
							reportError(e1);
						}

					} else {

						IGPackageImport pi = new IGPackageImport(l.getId(), pkgId, fItemId, false, getLocation(), aCache.getZDB());

						try {
							aContainer.add(pi);
						} catch (ZamiaException e1) {
							reportError(e1);
						}

					}
				}

			} else {
				try {
					aContainer.add(new IGDUUID(e.getDMUID(), getLocation(), aCache.getZDB()));
				} catch (ZamiaException e1) {
					reportError(e1);
				}
			}
		} else {

			// use foo_lib.all;

			int m = l.getNumDUs();
			for (int j = 0; j < m; j++) {

				DMUID duuid = l.getDU(j);

				if (duuid.getType() == LUType.Architecture) {
					continue;
				}

				try {
					aContainer.add(new IGDUUID(duuid, getLocation(), aCache.getZDB()));
				} catch (ZamiaException e) {
					reportError(e);
				}

			}
		}
		return null;
	}
}