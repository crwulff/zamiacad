/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Mar 22, 2009
 */
package org.zamia.instgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.zamia.DMManager;
import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGObject.IGObjectCat;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.instgraph.IGSubProgram.IGBuiltin;
import org.zamia.instgraph.IGType.TypeCat;
import org.zamia.vhdl.ast.ConfigurationSpecification;
import org.zamia.vhdl.ast.DMUID;
import org.zamia.vhdl.ast.DMUID.LUType;
import org.zamia.vhdl.ast.TypeDefinition;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGContainer extends IGItem {

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	public static final String UNIVERSAL_INTEGER_ID = "$$universal_integer$$";

	public static final String UNIVERSAL_REAL_ID = "$$universal_real$$";

	private Set<IGLibraryImport> fImportedLibs = new HashSet<IGLibraryImport>();

	private ArrayList<IGPackageImport> fImportedPackages = new ArrayList<IGPackageImport>();

	private HashMap<String, ArrayList<Long>> fLocalItemMap = new HashMap<String, ArrayList<Long>>();

	private ArrayList<Long> fLocalItems = new ArrayList<Long>();

	private ArrayList<Long> fInterfaces = new ArrayList<Long>();

	private ArrayList<Long> fGenerics = new ArrayList<Long>();

	private ArrayList<ConfigurationSpecification> fConfSpecs = new ArrayList<ConfigurationSpecification>();

	private long fParentDBID = 0;

	private long fReturnTypeDBID;

	public IGContainer(long aParentDBID, SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);
		fParentDBID = aParentDBID;
	}

	public void setReturnType(IGType aType) {
		fReturnTypeDBID = save(aType);
	}

	public IGType getReturnType() {
		if (fReturnTypeDBID != 0)
			return (IGType) getZDB().load(fReturnTypeDBID);
		if (fParentDBID != 0) {
			IGContainer parent = (IGContainer) getZDB().load(fParentDBID);
			return parent.getReturnType();
		}
		return null;
	}

	public void addGeneric(IGObject aInterface) throws ZamiaException {
		add(aInterface);

		fGenerics.add(aInterface.getDBID());
	}

	public void addInterface(IGObject aInterface) throws ZamiaException {
		add(aInterface);

		fInterfaces.add(aInterface.getDBID());
	}

	public int getNumGenerics() {
		return fGenerics.size();
	}

	public IGObject getGeneric(int aIdx) {
		return (IGObject) getZDB().load(fGenerics.get(aIdx));
	}

	public ArrayList<IGObject> getGenerics() {
		int n = getNumGenerics();
		ArrayList<IGObject> res = new ArrayList<IGObject>(n);
		for (int i = 0; i < n; i++) {
			res.add(getGeneric(i));
		}
		return res;
	}

	public int getNumInterfaces() {
		return fInterfaces.size();
	}

	public IGObject getInterface(int aIdx) {
		return (IGObject) getZDB().load(fInterfaces.get(aIdx));
	}

	public ArrayList<IGObject> getInterfaces() {
		int n = getNumInterfaces();
		ArrayList<IGObject> res = new ArrayList<IGObject>(n);
		for (int i = 0; i < n; i++) {
			res.add(getInterface(i));
		}
		return res;
	}

	public void add(IGContainerItem aItem) throws ZamiaException {

		String id = aItem.getId();

		if (!(aItem instanceof IGPackageImport) && (!(aItem instanceof IGLibraryImport))) {

			if (id == null) {
				logger.error("IGContainer: sanity check failed. tried to add anonymous item.");
			}

			ArrayList<Long> items = fLocalItemMap.get(id);
			if (items == null) {
				items = new ArrayList<Long>(1);
				fLocalItemMap.put(id, items);
			}

			long dbid = aItem.store();

			//			int n = items.size();
			//			for (int i = 0; i < n; i++) {
			//				if (items.get(i).longValue() == dbid) {
			//					logger.error("IGContainer: foobar: sanity check failed: added the same object twice!");
			//				}
			//			}

			items.add(dbid);
			fLocalItems.add(dbid);

			if (aItem instanceof IGLibraryImport) {
				fImportedLibs.add((IGLibraryImport) aItem);
			}
		} else {
			if (aItem instanceof IGLibraryImport) {
				fImportedLibs.add((IGLibraryImport) aItem);
			} else if (aItem instanceof IGPackageImport) {
				fImportedPackages.add((IGPackageImport) aItem);
			}
		}

		storeOrUpdate();
	}

	public IGResolveResult resolve(String aId) {
		IGResolveResult res = new IGResolveResult();
		resolveP(aId, res);
		return res;
	}

	private void resolveP(String aId, IGResolveResult aResult) {

		/*
		 * first, local items
		 */

		IGResolveResult res = aResult;

		ArrayList<Long> itemsL = fLocalItemMap.get(aId);
		if (itemsL != null) {

			int n = itemsL.size();
			for (int i = 0; i < n; i++) {

				IGItem item = (IGItem) getZDB().load(itemsL.get(i).longValue());
				res.addItem(item);
			}

			if (!res.isEmpty() && !res.isContainsSubPrograms())
				return;
		}

		/*
		 * then check the parent, if any
		 */

		if (fParentDBID != 0) {
			IGContainer parent = (IGContainer) getZDB().load(fParentDBID);

			parent.resolveP(aId, res);

			if (!res.isEmpty() && !res.isContainsSubPrograms())
				return;
		}

		/*
		 * finally the context
		 */

		ZamiaProject zprj = (ZamiaProject) getZDB().getOwner();
		DMManager dum = zprj.getDUM();
		IGManager igm = zprj.getIGM();

		for (IGLibraryImport li : fImportedLibs) {

			DMUID duuid = new DMUID(LUType.Entity, li.getRealId(), aId, null);

			if (dum.hasDM(duuid)) {
				res.addItem(new IGDUUID(duuid, null, getZDB()));
			}

			if (li.getId().equals(aId)) {
				res.addItem(li);
			}
		}

		int n = fImportedPackages.size();
		for (int i = n - 1; i >= 0; i--) {
			IGPackageImport pi = fImportedPackages.get(i);

			String itemId = pi.getItemId();
			if (itemId != null && !itemId.equals(aId)) {
				continue;
			}
			if (itemId == null && !pi.isAll() && !pi.getId().equals(aId)) {
				continue;
			}

			IGPackage pkg = igm.findPackage(pi.getLibId(), pi.getId(), null);
			if (pkg == null) {
				continue;
			}

			IGContainer pkgContainer = pkg.getContainer();

			if (itemId != null) {
				ArrayList<IGContainerItem> itemsC = pkgContainer.findLocalItems(aId);
				if (itemsC != null) {
					int m = itemsC.size();
					for (int j = 0; j < m; j++) {
						res.addItem(itemsC.get(j));
					}
				}

			} else {
				if (pi.isAll()) {

					ArrayList<IGContainerItem> itemsC = pkgContainer.findLocalItems(aId);
					if (itemsC != null) {
						int m = itemsC.size();
						for (int j = 0; j < m; j++) {
							res.addItem(itemsC.get(j));
						}
					}

				} else {
					DMUID dmuid = new DMUID(LUType.Package, pi.getLibId(), pi.getId(), null);
					res.addItem(new IGDUUID(dmuid, null, getZDB()));
				}
			}
		}
	}

	/*
	 * some convenience resolution functions
	 */

	public IGType resolveType(String aId) {

		IGResolveResult res = resolve(aId);

		int n = res.getNumResults();
		for (int i = 0; i < n; i++) {
			IGItem item = res.getResult(i);
			if (item instanceof IGType) {
				return (IGType) item;
			}
		}

		return null;
	}

	public IGLibraryImport resolveLibrary(String aId) {
		IGResolveResult res = resolve(aId);

		int n = res.getNumResults();
		for (int i = 0; i < n; i++) {
			IGItem item = res.getResult(i);
			if (item instanceof IGLibraryImport) {
				return (IGLibraryImport) item;
			}
		}

		return null;
	}

	public IGObject resolveObject(String aId) {
		IGResolveResult res = resolve(aId);

		int n = res.getNumResults();
		for (int i = 0; i < n; i++) {
			IGItem item = res.getResult(i);
			if (item instanceof IGObject) {
				return (IGObject) item;
			}
		}

		return null;
	}

	/*
	 * convenience access to standard.vhdl types
	 */

	public IGType findBitType() {
		return resolveType("BIT");
	}

	public IGStaticValue findTrueValue() {
		IGType t = findBoolType();

		if (t == null)
			return null;

		return t.findEnumLiteral("TRUE");
	}

	public IGStaticValue findFalseValue() {
		IGType t = findBoolType();

		if (t == null)
			return null;

		return t.findEnumLiteral("FALSE");
	}

	public IGType findBoolType() {
		return resolveType("BOOLEAN");
	}

	public IGType findTimeType() {
		return resolveType("TIME");
	}

	public IGType findStringType() {
		return resolveType("STRING");
	}

	public void declareUniversalTypes() {
		try {

			// bootstrap universal integer

			IGTypeStatic intTypeS = new IGTypeStatic(TypeCat.INTEGER, null, null, null, null, false, null, getZDB());
			intTypeS.setId(UNIVERSAL_INTEGER_ID);

			IGStaticValue siLeft = new IGStaticValueBuilder(intTypeS, null, null).setNum(-2147483648l).buildConstant();
			IGStaticValue siRight = new IGStaticValueBuilder(intTypeS, null, null).setNum(2147483647l).buildConstant();
			IGStaticValue siAscending = findTrueValue();

			intTypeS.setLeft(siLeft, null);
			intTypeS.setRight(siRight, null);
			intTypeS.setAscending(siAscending, null);
			intTypeS.setUniversal(true);
			intTypeS.storeOrUpdate();

			add(intTypeS);

			TypeDefinition.addBuiltinIntOperators(intTypeS, this, null);

			IGTypeStatic realTypeS = new IGTypeStatic(TypeCat.REAL, null, null, null, null, false, null, getZDB());
			realTypeS.setId(UNIVERSAL_REAL_ID);

			siLeft = new IGStaticValueBuilder(realTypeS, null, null).setReal(Double.MIN_VALUE).buildConstant();
			siRight = new IGStaticValueBuilder(realTypeS, null, null).setReal(Double.MAX_VALUE).buildConstant();
			siAscending = findTrueValue();

			realTypeS.setLeft(siLeft, null);
			realTypeS.setRight(siRight, null);
			realTypeS.setAscending(siAscending, null);
			realTypeS.setUniversal(true);
			realTypeS.storeOrUpdate();

			add(realTypeS);

			TypeDefinition.addBuiltinRealOperators(realTypeS, this, null);

		} catch (ZamiaException e) {
		}
	}

	public IGType findIntType() {
		IGType intType = resolveType("INTEGER");
		return intType;
	}

	public IGType findUniversalIntType() {
		return resolveType(UNIVERSAL_INTEGER_ID);
	}

	public IGType findUniversalRealType() {
		return resolveType(UNIVERSAL_REAL_ID);
	}

	public IGType findRealType() {
		IGType realType = resolveType("REAL");

		if (realType == null) { // happens once in standard.vhdl
			return resolveType(UNIVERSAL_REAL_ID);
		}

		return realType;
	}

	public IGType findSeverityLevelType() {
		return resolveType("SEVERITY_LEVEL");
	}

	public IGType findOpenKindType() {
		return resolveType("FILE_OPEN_KIND");
	}

	public IGType findCharType() {
		return resolveType("CHARACTER");
	}

	public IGType findBitVectorType() {
		return resolveType("BIT_VECTOR");
	}

	public IGType findFileOpenKindType() {
		return resolveType("FILE_OPEN_KIND");
	}

	public IGType findFileOpenStatusType() {
		return resolveType("FILE_OPEN_STATUS");
	}

	public int getNumLocalItems() {
		return fLocalItems.size();
	}

	public IGContainerItem getLocalItem(int aIdx) {
		return (IGContainerItem) getZDB().load(fLocalItems.get(aIdx));
	}

	public int getNumPackageImports() {
		return fImportedPackages.size();
	}

	public IGPackageImport getPackageImport(int aIdx) {
		return fImportedPackages.get(aIdx);
	}

	@Override
	public IGItem getChild(int aIdx) {
		return getLocalItem(aIdx);
	}

	@Override
	public int getNumChildren() {
		return getNumLocalItems();
	}

	public ArrayList<IGContainerItem> findLocalItems(String aId) {

		ArrayList<Long> itemsL = fLocalItemMap.get(aId);
		if (itemsL == null) {
			return null;
		}

		int n = itemsL.size();
		ArrayList<IGContainerItem> items = new ArrayList<IGContainerItem>(n);
		for (int i = 0; i < n; i++) {
			Long dbid = itemsL.get(i);
			items.add((IGContainerItem) getZDB().load(dbid.longValue()));
		}
		return items;
	}

	public void removeInterfaces() {
		fLocalItemMap = new HashMap<String, ArrayList<Long>>();
		fInterfaces = new ArrayList<Long>();
	}

	public void add(ConfigurationSpecification aConfSpec) {
		fConfSpecs.add(aConfSpec);
	}

	public int getNumConfSpecs() {
		return fConfSpecs.size();
	}

	public ConfigurationSpecification getConfSpec(int aIdx) {
		return fConfSpecs.get(aIdx);
	}

	/*
	 * convenience
	 */

	public void addBuiltinOperator(String aLabel, IGType aTA, IGType aTB, IGType aTR, IGBuiltin aBuiltin, SourceLocation aLocation) throws ZamiaException {

		if (aTR == null) {
			throw new ZamiaException("Internal error: operand without return type detected.", aLocation);
		}

		IGSubProgram sub = new IGSubProgram(store(), aTR, aLabel, aLocation, getZDB());

		sub.setBuiltin(aBuiltin);

		IGContainer container = sub.getContainer();

		IGObject intf = new IGObject(OIDir.IN, null, IGObjectCat.CONSTANT, aTA, "a", aLocation, getZDB());
		container.addInterface(intf);
		intf = new IGObject(OIDir.IN, null, IGObjectCat.CONSTANT, aTB, "b", aLocation, getZDB());
		container.addInterface(intf);

		sub.computeSignatures();

		sub.storeOrUpdate();
		container.storeOrUpdate();
		add(sub);
	}

	public void addBuiltinOperator(String aLabel, IGType aTA, IGType aTR, IGBuiltin aBuiltin, SourceLocation aLocation) throws ZamiaException {

		IGSubProgram sub = new IGSubProgram(store(), aTR, aLabel, aLocation, getZDB());

		sub.setBuiltin(aBuiltin);

		IGContainer container = sub.getContainer();

		IGObject intf = new IGObject(OIDir.IN, null, IGObjectCat.CONSTANT, aTA, "a", aLocation, getZDB());
		container.addInterface(intf);

		sub.computeSignatures();

		sub.storeOrUpdate();
		container.storeOrUpdate();
		add(sub);
	}

}
