/*
 * Copyright 2005-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jun 19, 2005
 */

package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.DUManager;
import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
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
import org.zamia.instgraph.IGInstantiation;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGLibraryImport;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGModule;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGOperationDeref;
import org.zamia.instgraph.IGOperationRecordField;
import org.zamia.instgraph.IGPackage;
import org.zamia.instgraph.IGRecordField;
import org.zamia.instgraph.IGType;
import org.zamia.vhdl.ast.Suffix.SuffixType;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class NameExtensionSuffix extends NameExtension {

	private Suffix suffix;

	public NameExtensionSuffix(Suffix suffix_, ASTObject parent_, long location_) {
		super(parent_, location_);
		suffix = suffix_;
	}

	public Suffix getSuffix() {
		return suffix;
	}

	public String toString() {
		return "." + suffix;
	}

	@Override
	public int getNumChildren() {
		return 0;
	}

	@Override
	public ASTObject getChild(int idx_) {
		return null;
	}

	@Override
	public String toVHDL() {
		return "." + suffix;
	}

	@Override
	public void computeIG(IGItem aItem, SourceLocation aPrevLocation, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ArrayList<IGItem> aResult, ErrorReport aReport)
			throws ZamiaException {

		if (aItem instanceof IGLibraryImport) {
			IGLibraryImport libImport = (IGLibraryImport) aItem;

			ZamiaProject zprj = aEE.getZamiaProject();
			IGManager igm = zprj.getIGM();
			DUManager dum = zprj.getDUM();

			IGPackage igp = igm.findPackage(libImport.getRealId(), suffix.getId(), getLocation());
			if (igp != null) {
				aResult.add(igp);
				return;
			}

			Entity e = dum.findEntity(libImport.getRealId(), suffix.getId());
			if (e != null) {
				aResult.add(new IGDUUID(e.getDUUID(), getLocation(), aEE.getZDB()));
				return;
			}

			aReport.append("Couldn't find " + suffix + " in " + libImport, getLocation());
			return;
		}

		if (aItem instanceof IGPackage) {
			IGPackage pkg = (IGPackage) aItem;
			IGContainer container = pkg.getContainer();
			ArrayList<IGContainerItem> items = container.findLocalItems(suffix.getId());
			if (items == null) {
				throw new ZamiaException("Item " + suffix + " not found in package " + pkg, this);
			}

			int n = items.size();
			for (int i = 0; i < n; i++) {
				aResult.add(items.get(i));
			}
			return;
		}

		if (aItem instanceof IGDUUID) {

			IGDUUID igduuid = (IGDUUID) aItem;
			DUUID duuid = igduuid.getDUUID();

			//			if (duuid.getType() != LUType.Package) {
			//				aReport.append("Package expected here.", getLocation());
			//				return;
			//			}

			ZamiaProject zprj = aEE.getZamiaProject();
			IGManager igm = zprj.getIGM();

			IGPackage pkg = igm.findPackage(duuid.getLibId(), duuid.getId(), getLocation());
			if (pkg != null) {
				IGContainer container = pkg.getContainer();
				ArrayList<IGContainerItem> items = container.findLocalItems(suffix.getId());
				if (items == null) {
					aReport.append("Item " + suffix + " not found in package " + pkg, getLocation());
					return;
				}
				int n = items.size();
				for (int i = 0; i < n; i++) {
					aResult.add(items.get(i));
				}
				return;
			} else {

				// maybe this is an entity ?

				DUManager dum = zprj.getDUM();

				Architecture arch = dum.getArchitecture(duuid.getLibId(), duuid.getId(), duuid.getArchId());

				if (arch != null) {
					String signature = IGInstantiation.computeSignature(arch.getDUUID(), null);

					IGModule module = igm.findModule(signature);

					if (module != null) {
						IGContainer container = module.getContainer();
						ArrayList<IGContainerItem> items = container.findLocalItems(suffix.getId());
						if (items != null) {
							int n = items.size();
							for (int i = 0; i < n; i++) {
								aResult.add(items.get(i));
							}
							return;
						}
					}
				}
			}

			aReport.append("Library unit " + duuid + " not found.", getLocation());
			return;
		}

		IGItem item = aItem;

		if (!(item instanceof IGOperation)) {
			aReport.append("Operation expected here, " + item + " found instead.", getLocation());
			return;
		}

		IGOperation op = (IGOperation) item;

		IGType tIn = op.getType();

		if (tIn.isAccess()) {
			if (!(suffix.getType() == SuffixType.ALL)) {
				throw new ZamiaException("ALL expected here.", suffix);
			}

			IGType elementType = tIn.getElementType();

			aResult.add(new IGOperationDeref(op, elementType, getLocation(), aEE.getZDB()));
			return;
		}

		if (!tIn.isRecord()) {
			aReport.append("Tried to use suffix on something that is not a record type.", getLocation());
			return;
		}

		if (!(suffix.getType() == SuffixType.ID)) {
			throw new ZamiaException("Operation suffix: Identifier expected, found " + suffix + " instead.", suffix);
		}

		String id = suffix.getId();

		IGRecordField rf = tIn.findRecordField(id, getLocation());

		if (rf == null) {
			aReport.append("Record field " + id + " not found.", getLocation());
			return;
		}

		IGType elementType = rf.getType();

		aResult.add(new IGOperationRecordField(rf, op, elementType, getLocation(), aEE.getZDB()));
	}

	@Override
	public void findReferences(String aId, ObjectCat aCategory, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aResult, ArrayList<SearchJob> aTODO) throws ZamiaException {
	}

}
