/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jul 23, 2005
 */

package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.ASTReferencesSearch;
import org.zamia.analysis.ast.MappedFormal;
import org.zamia.analysis.ast.MappedInterfaces;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGDUUID;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGInstantiation;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGMapping;
import org.zamia.instgraph.IGMappings;
import org.zamia.instgraph.IGModule;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGStructure;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.util.HashSetArray;
import org.zamia.util.Pair;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public abstract class InstantiatedUnit extends ConcurrentStatement {

	protected Name fName; // of component to be instantiated

	protected AssociationList fPMS, fGMS;

	public InstantiatedUnit(String aLabel, Name aName, VHDLNode aParent, long aLocation) {
		super(aLabel, aParent, aLocation);
		fName = aName;
		fName.setParent(this);
	}

	public Name getName() {
		return fName;
	}

	protected void printEntity() throws ZamiaException {
		logger.info("Entity of missing component (guess):");
		logger.info("entity " + fName + " is");
		logger.info("  port (");

		if (fPMS != null) {
			int n = fPMS.getNumAssociations();
			if (n > 0) {
				for (int i = 0; i < n; i++) {
					AssociationElement ae = fPMS.getAssociation(i);
					FormalPart formal = ae.getFormalPart();
					if (formal != null) {
						String portId = ae.getFormalPart().getName().getId();
						logger.info(portId + " : ? ");
					} else {
						logger.info("<implicit>");
					}
					logger.info(" ?? ");
					if (i < (n - 1)) {
						logger.info("; ");
					}
				}
			}
		}

		logger.info(");");
		logger.info("end " + fName + ";");
	}

	@Override
	public int getNumChildren() {
		return 3;
	}

	@Override
	public VHDLNode getChild(int idx_) {
		switch (idx_) {
		case 0:
			return fName;
		case 1:
			return fGMS;
		case 2:
			return fPMS;
		}
		return null;
	}

	@Override
	public String toString() {
		return fName.getId() + " port map (" + fPMS + ") generic map (" + fGMS + ")";
	}

	public void setGenericMapAspect(AssociationList aGMA) {
		fGMS = aGMA;
		if (fGMS != null)
			fGMS.setParent(this);
	}

	public void setPortMapAspect(AssociationList aPMA) {
		fPMS = aPMA;
		if (fPMS != null)
			fPMS.setParent(this);
	}

	public AssociationList getPMS() {
		return fPMS;
	}

	public AssociationList getGMS() {
		return fGMS;
	}

	protected void findReferences(Architecture aArch, String aId, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache, ReferenceSearchResult aResult,
			ArrayList<SearchJob> aTODO) throws ZamiaException {

		IGManager igm = aZPrj.getIGM();

		DMUID duuid = aArch.getDMUID();

		String signature = IGInstantiation.computeSignature(duuid, null);

		IGModule module = igm.findModule(signature);

		if (module == null) {
			logger.error("SA: IGModule for %s not found.", duuid);
			return;
		}

		IGElaborationEnv cache = new IGElaborationEnv(aZPrj);

		Entity entity = aArch.findEntity(module.getContainer(), cache);

		if (entity == null) {
			logger.error("SA: Failed to find entity for %s", module);
			return;
		}

		InterfaceList ports = entity.getPorts();

		if (fPMS != null) {
			if (ports != null) {

				MappedInterfaces mi = ASTReferencesSearch.map(ports, fPMS);

				HashSetArray<MappedFormal> l = mi.getFormals(aId);

				if (l != null) {
					int n = l.size();
					for (int i = 0; i < n; i++) {

						MappedFormal mf = l.get(i);
						InterfaceDeclaration intf = mf.fFormal;

						if (intf.getDir() != PortDir.IN) {
							aResult.add(new ReferenceSite(mf.fASTObject, RefType.Write));
						} else {
							aResult.add(new ReferenceSite(mf.fASTObject, RefType.Read));
						}

						if (aTODO != null) {

							duuid = aArch.getDMUID();

							aTODO.add(new SearchJob(intf.getId(), duuid, aDepth + 1, aResult));
						}
					}
				}
			}
		}

		if (fGMS != null) {
			fGMS.findReferences(aId, ObjectCat.Signal, RefType.Read, aDepth + 1, aZPrj, aContainer, aCache, aResult, aTODO);
		}

	}

	public final void computeIG(DMUID aDUUID, IGContainer aParentContainer, IGStructure aParentStructure, IGElaborationEnv aEE) throws ZamiaException {
		IGInstantiation inst = computeIGInstantiation(aDUUID, aParentContainer, aParentStructure, aEE);
		if (inst != null) {
			aParentStructure.addStatement(inst);
		}
	}
	
	public abstract IGInstantiation computeIGInstantiation(DMUID aDUUID, IGContainer aContainer, IGStructure aStructure, IGElaborationEnv aEE) throws ZamiaException;
	
	protected IGInstantiation instantiateIGModule(Architecture aArch, DMUID aParentDUUID, IGContainer aParentContainer, IGStructure aParentStructure, IGElaborationEnv aParentEE) {

		try {
			DMUID duuid = aArch.getDMUID();
			
			SourceLocation location = getLocation();

			ToplevelPath path = aParentStructure.getPath().append(getLabel());

			logger.info("   %s: %s", path, duuid);

			IGManager igm = aParentEE.getZamiaProject().getIGM();

			IGInstantiation inst = new IGInstantiation(aParentDUUID, duuid, getLabel(), location, aParentEE.getZDB());

			/*
			 * look up the corresponding IGModule
			 */

			ArrayList<Pair<String, IGStaticValue>> actualGenerics = new ArrayList<Pair<String, IGStaticValue>>();

			String signature = IGInstantiation.computeSignature(duuid, actualGenerics);

			IGModule module = igm.getOrCreateIGModule(path, aParentDUUID, duuid, signature, actualGenerics, false, location);

			/*
			 * compute generics
			 */

			try {
				if (fGMS != null) {

					IGContainer formalContainer = module.getStructure().getContainer();
					
					IGContainer formalGenericsContainer = new IGContainer(aParentContainer.getDBID(), getLocation(), aParentEE.getZDB());
					int n = formalContainer.getNumGenerics();
					for (int i = 0; i<n; i++) {
						IGObject intf = formalContainer.getGeneric(i);
						formalGenericsContainer.add(intf);
					}
					
					IGElaborationEnv formalEE = new IGElaborationEnv(aParentEE.getZamiaProject());
					IGOperationCache formalCache = new IGOperationCache();
					
					IGOperationCache actualCache = new IGOperationCache();

					ArrayList<IGObject> generics = formalContainer.getGenerics();

					ErrorReport report = new ErrorReport();
					IGMappings mappings = fGMS.map(formalGenericsContainer, formalEE, formalCache, aParentContainer, aParentEE, actualCache, generics, true, report, true);
					if (mappings == null) {
						throw new ZamiaException("Generics mapping failed:\n" + report, getLocation());
					}
					if (mappings.isFailed()) {
						ZamiaException msg = new ZamiaException("Generics mapping failed:\n" + report, getLocation());
						reportError(msg);
					}

					/*
					 * use our interpreter to compute actual generic values
					 */

					IGInterpreterRuntimeEnv env = aParentEE.getInterpreterEnv();
					IGInterpreterCode ic = new IGInterpreterCode("Generics computation for " + this, getLocation());
					env.enterContext();

					n = generics.size();
					for (int i = 0; i < n; i++) {
						IGObject generic = generics.get(i);
						env.newObject(generic, generic.computeSourceLocation());
					}

					n = mappings.getNumMappings();
					for (int i = 0; i < n; i++) {

						IGMapping mapping = mappings.getMapping(i);

						mapping.generateEntryCode(false, ic, mapping.computeSourceLocation());

						inst.addGeneric(mapping);
					}

					//System.out.println();
					//ic.dump(System.out);

					// now run the code

					env.call(ic, ASTErrorMode.EXCEPTION, null);
					env.resume(ASTErrorMode.EXCEPTION, null);
					env.rts();

					// now, retrieve the generics

					n = generics.size();
					for (int i = 0; i < n; i++) {
						IGObject generic = generics.get(i);
						IGStaticValue actualGeneric = env.getObjectValue(generic);
						inst.addActualGeneric(generic.getId(), actualGeneric);
						logger.info("      GENERIC %s => %s", generic.getId(), actualGeneric);
					}

					env.exitContext();
				}
			} catch (ZamiaException e) {
				reportError(e);
			} catch (Throwable t) {
				el.logException(t);
			}

			inst.computeSignature();

			// logger.info("   Signature is '%s'", inst.getSignature());

			try {
				module = igm.getOrCreateIGModule(path, aParentDUUID, duuid, inst.getSignature(), inst.getActualGenerics(), true, location);

				if (fPMS != null) {

					IGContainer formalContainer = module.getStructure().getContainer();
					
					if (formalContainer == null) {
						throw new ZamiaException ("Instantiated module doesn't have a container: " + module, location);
					}
					
					IGContainer formalIntfContainer = new IGContainer(aParentContainer.getDBID(), getLocation(), aParentEE.getZDB());
					int n = formalContainer.getNumInterfaces();
					for (int i = 0; i<n; i++) {
						IGObject intf = formalContainer.getInterface(i);
						formalIntfContainer.add(intf);
					}
					
					IGElaborationEnv formalEE = new IGElaborationEnv(aParentEE.getZamiaProject());
					IGOperationCache formalCache = new IGOperationCache();

					IGOperationCache actualCache = new IGOperationCache();

					ArrayList<IGObject> interfaces = formalContainer.getInterfaces();

					ErrorReport report = new ErrorReport();
					IGMappings mappings = fPMS.map(formalIntfContainer, formalEE, formalCache, aParentContainer, aParentEE, actualCache, interfaces, true, report, true);
					if (mappings == null) {
						throw new ZamiaException("Port mapping failed:\n" + report, getLocation());
					}
					if (mappings.isFailed()) {
						ZamiaException msg = new ZamiaException("Port mapping failed:\n" + report, getLocation());
						reportError(msg);
					}

					n = mappings.getNumMappings();
					for (int i = 0; i < n; i++) {

						IGMapping mapping = mappings.getMapping(i);

						inst.add(mapping);
					}
				}
			} catch (ZamiaException e) {
				reportError(e);
			} catch (Throwable t) {
				el.logException(t);
			}

			return inst;

		} catch (ZamiaException e) {
			reportError(e);
		}
		return null;
	}

	public final DMUID getChildDUUID(IGContainer aContainer, IGElaborationEnv aCache) {
		try {
			IGDUUID zduuid = fName.computeIGAsDesignUnit(aContainer, aCache, ASTErrorMode.RETURN_NULL, new ErrorReport());
			if (zduuid == null)
				return null;
			return zduuid.getDUUID();
		} catch (ZamiaException e) {
			el.logException(e);
		}
		return null;
	}

}