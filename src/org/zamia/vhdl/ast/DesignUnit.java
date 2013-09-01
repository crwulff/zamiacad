/*
 * Copyright 2006-2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.vhdl.ast;

import java.util.ArrayList;
import java.util.HashMap;

import org.zamia.DMManager;
import org.zamia.IDesignModule;
import org.zamia.SourceFile;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGContainerItem;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public abstract class DesignUnit extends DeclarativeItem implements IDesignModule {

	protected transient DMUID fDUUID = null;

	private transient ZDB fZDB;

	private final String fLibId;

	protected final Context fContext;

	private final String fZPrjID;

	// we need both because of subprogram overloading:
	// (multiple subprograms with same id)
	// declarationMap is only used in findDeclaration

	protected HashMap<String, BlockDeclarativeItem> fDeclarationMap;

	public ArrayList<BlockDeclarativeItem> fDeclarations;

	public DesignUnit(Context aContext, String aId, SourceFile aSF, long aLocation, String aLibId, ZDB aZDB) {
		super(aId, null, aLocation);
		fContext = aContext;
		setSource(aSF);
		fContext.setParent(this);
		fLibId = aLibId;

		fZDB = aZDB;
		ZamiaProject zprj = (ZamiaProject) aZDB.getOwner();
		fZPrjID = zprj.getId();

		fDeclarationMap = new HashMap<String, BlockDeclarativeItem>(5);
		fDeclarations = new ArrayList<BlockDeclarativeItem>(5);
	}

	public DesignUnit(String aId, SourceFile aSF, long aLocation, String aLibId, ZDB aZDB) {
		this(new Context(null, aLocation), aId, aSF, aLocation, aLibId, aZDB);
	}

	public ZDB getZDB() {

		if (fZDB == null) {
			ZamiaProject zprj = ZamiaProject.lookupProject(fZPrjID);
			if (zprj != null) {
				fZDB = zprj.getZDB();
			}
		}

		return fZDB;
	}

	public ZamiaProject getZPrj() {
		ZDB db = getZDB();
		return (ZamiaProject) db.getOwner();
	}

	@Override
	public String getLibId() {
		return fLibId;
	}

	public Context getContext() {
		return fContext;
	}

	public SourceFile getSourceFile() {
		return getSource();
	}

	@Override
	public DesignUnit getDesignUnit() {
		return this;
	}

	public int getNumDeclarations() {
		return fDeclarations.size();
	}

	public BlockDeclarativeItem getDeclaration(int aIdx) {
		return fDeclarations.get(aIdx);
		//		Long l = fDeclarations.get(aIdx);
		//		if (l == null) {
		//			return null;
		//		}
		//		ZDB zdb = getZDB();
		//		BlockDeclarativeItem item = (BlockDeclarativeItem) zdb.load(l.longValue());
		//		if (item == null) {
		//			return null;
		//		}
		//		item.setParent(this, true);
		//		return item;
	}

	public BlockDeclarativeItem getDeclaration(String aId) {
		return fDeclarationMap.get(aId);
		//		Long l = fDeclarationMap.get(aId);
		//		if (l == null) {
		//			return null;
		//		}
		//		ZDB zdb = getZDB();
		//		BlockDeclarativeItem item = (BlockDeclarativeItem) zdb.load(l.longValue());
		//		if (item == null) {
		//			return null;
		//		}
		//		item.setParent(this, true);
		//		return item;
	}

	public void add(BlockDeclarativeItem aItem) throws ZamiaException {

		if (aItem != null) {
			fDeclarations.add(aItem);
			fDeclarationMap.put(aItem.getId(), aItem);
			aItem.setParent(this, true);
		}

		//		if (aItem != null) {
		//			aItem.setParent(null, true);
		//			ZDB zdb = getZDB();
		//			long dbid = zdb.storeNow(aItem);
		//			fDeclarations.add(dbid);
		//			fDeclarationMap.put(aItem.getId(), dbid);
		//		}
	}

	@Override
	public DeclarativeItem findDeclaration(String aId, ZamiaProject aZPrj) {

		DeclarativeItem decl1 = getDeclaration(aId);
		if (decl1 != null)
			return decl1;

		// enum ?
		for (BlockDeclarativeItem decl : fDeclarations) {

			if (decl instanceof TypeDeclaration) {
				TypeDeclaration td = (TypeDeclaration) decl;

				TypeDefinition def = td.getType();
				if (def instanceof TypeDefinitionEnum) {
					TypeDefinitionEnum enumdef = (TypeDefinitionEnum) def;

					int m = enumdef.getNumLiterals();
					for (int j = 0; j < m; j++) {
						OperationLiteral l = enumdef.getLiteral(j);

						if (l.getImage().equals(aId)) {
							return td;
						}
					}
				}

			}
		}

		if (aId.equals("WORK")) {
			DMManager dum = getZPrj().getDUM();
			return dum.getLibrary(fLibId);
		}

		decl1 = fContext.findDeclaration(aId, aZPrj);
		if (decl1 != null)
			return decl1;

		return super.findDeclaration(aId, aZPrj);
	}

	@Override
	public final DMUID getDMUID() throws ZamiaException {

		if (fDUUID != null) {
			return fDUUID;
		}

		fDUUID = getDMUID(fLibId);

		return fDUUID;
	}

	@Override
	public final IGContainerItem computeIG(ArrayList<IGContainerItem> aSpecItems, IGContainer aContainer, IGElaborationEnv aCache) throws ZamiaException {
		throw new ZamiaException("Internal error: DesignUnit.computeIG() called");
	}

	@Override
	public void collectIdentifiers(HashSetArray<String> aIdentifiers, ZamiaProject aZPrj) {

		aIdentifiers.add(getId());

		for (BlockDeclarativeItem decl : fDeclarations) {
			aIdentifiers.add(decl.getId());
		}

		super.collectIdentifiers(aIdentifiers, aZPrj);
	}
}
