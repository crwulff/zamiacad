/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jul 25, 2008
 */
package org.zamia.analysis.ast;

import java.util.ArrayList;
import java.util.HashSet;

import org.zamia.ASTNode;
import org.zamia.DMManager;
import org.zamia.ExceptionLogger;
import org.zamia.IDesignModule;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGDUUID;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGInstantiation;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGModule;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.vhdl.ast.Architecture;
import org.zamia.vhdl.ast.AssociationElement;
import org.zamia.vhdl.ast.AssociationList;
import org.zamia.vhdl.ast.Block;
import org.zamia.vhdl.ast.BlockDeclarativeItem;
import org.zamia.vhdl.ast.ComponentInstantiation;
import org.zamia.vhdl.ast.ConcurrentStatement;
import org.zamia.vhdl.ast.DMUID;
import org.zamia.vhdl.ast.DeclarativeItem;
import org.zamia.vhdl.ast.Entity;
import org.zamia.vhdl.ast.EntityInstantiation;
import org.zamia.vhdl.ast.FormalPart;
import org.zamia.vhdl.ast.GenerateStatement;
import org.zamia.vhdl.ast.InstantiatedUnit;
import org.zamia.vhdl.ast.InterfaceDeclaration;
import org.zamia.vhdl.ast.InterfaceList;
import org.zamia.vhdl.ast.Name;
import org.zamia.vhdl.ast.Operation;
import org.zamia.vhdl.ast.TypeDeclaration;
import org.zamia.vhdl.ast.VariableDeclaration;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class ASTReferencesSearch {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public enum ObjectCat {
		Variable, Signal, Type, Entity
	}

	private ZamiaProject fZPrj;

	private DMManager fDUM;

	private IGManager fIGM;

	private ASTReferencesSearch(ZamiaProject zprj_) {
		fZPrj = zprj_;
		fDUM = fZPrj.getDUM();
		fIGM = fZPrj.getIGM();
	}

	/**
	 * Simple, syntax-based mapping of associations to a given interface list
	 * 
	 * no type-checking, no real name extension processing
	 * 
	 * @param aFormals
	 * @param aActuals
	 */

	public static MappedInterfaces map(InterfaceList aInterfaces, AssociationList aActuals) {

		MappedInterfaces res = new MappedInterfaces();

		if (aInterfaces == null)
			return res;
		if (aActuals == null)
			return res;

		int pIdx = 0;
		int nInterfaces = aInterfaces.getNumInterfaces();
		int nParams = aActuals.getNumAssociations();

		// phase one: positional elements

		while (pIdx < nInterfaces) {
			InterfaceDeclaration interf = aInterfaces.get(pIdx);

			if (pIdx >= nParams)
				break;

			AssociationElement ae = aActuals.getAssociation(pIdx);

			FormalPart formal = ae.getFormalPart();
			if (formal != null)
				break;

			Operation actual = ae.getActualPart();

			//logger.debug("ReferencesSearch: map: %s => %s", interf, actual);

			res.add(interf, actual);

			pIdx++;
		}

		if (pIdx < nParams) {
			AssociationElement ae = aActuals.getAssociation(pIdx);

			FormalPart formal = ae.getFormalPart();
			if (formal == null) {
				logger.debug("ReferencesSearch:    *** EXPLICIT MAP: too many implicit parameters. ***");
				// too many parameters
				return res;
			}
		}

		// phase two: explicit associations
		while (pIdx < nParams) {

			AssociationElement ae = aActuals.getAssociation(pIdx);

			FormalPart formal = ae.getFormalPart();
			if (formal == null) {
				logger.error("ReferencesSearch: %s: Illegal mix of explicit and positional parameters at assoc #%d: %s", ae.getLocation(), pIdx, ae);
				continue;
			}

			Name formalName = formal.getName();

			String id = formalName.getId();

			InterfaceDeclaration interf = aInterfaces.get(id);

			if (interf == null) {
				pIdx++;
				continue;
			}

			Operation actual = ae.getActualPart();

			//logger.debug("ReferencesSearch: map: %s => %s", interf, actual);

			res.add(interf, actual);

			pIdx++;
		}

		return res;
	}

	private HashSetArray<IdDUUIDTuple> searchInstantiators(DMUID aChildDUUID, String aPortID) throws ZamiaException {

		logger.debug("SA: ASTReferencesSearch.searchInstantiators(): looking for instantiators of %s, port id is '%s'", aChildDUUID, aPortID);

		HashSetArray<IdDUUIDTuple> res = new HashSetArray<IdDUUIDTuple>();

		HashSetArray<DMUID> instantiators = fIGM.findInstantiators(aChildDUUID.getUID());

		if (instantiators == null) {
			return null;
		}

		int m = instantiators.size();

		for (int j = 0; j < m; j++) {

			DMUID parentDUUID = instantiators.get(j);

			logger.debug("SA: ASTReferencesSearch.searchInstantiators(): Found instantiator: '%s'", parentDUUID);

			IDesignModule parentDU = fDUM.getDM(parentDUUID);
			if (!(parentDU instanceof Architecture)) {
				logger.error("SA: ASTReferencesSearch.searchInstantiators(): Architecture for %s not found: %s.", parentDUUID, parentDU);
				continue;
			}

			Architecture parentArch = (Architecture) parentDU;

			String parentSignature = IGInstantiation.computeSignature(parentDUUID, null);

			IGModule parentModule = fIGM.findModule(parentSignature);

			if (parentModule == null) {
				logger.error("SA: ASTReferencesSearch.searchInstantiators(): IGModule for %s not found.", parentDUUID);
				continue;
			}

			IGElaborationEnv cache = new IGElaborationEnv(fZPrj);

			int n = parentArch.getNumConcurrentStatements();
			for (int i = 0; i < n; i++) {
				ConcurrentStatement cs = parentArch.getConcurrentStatement(i);

				searchInstantiationsUp(cs, parentDUUID, aChildDUUID, aPortID, parentModule.getContainer(), cache, res);
			}
		}
		return res;
	}

	private void searchInstantiationsUp(ConcurrentStatement aStmt, DMUID aParentDUUID, DMUID aWantedChildDUUID, String aFormalId, IGContainer aContainer, IGElaborationEnv aCache,
			HashSetArray<IdDUUIDTuple> aRes) throws ZamiaException {

		DMUID wantedChildEntityDUUID = aWantedChildDUUID.getEntityDUUID();

		if (aStmt instanceof InstantiatedUnit) {

			InstantiatedUnit iu = (InstantiatedUnit) aStmt;

			DMUID childDUUID = iu.getChildDUUID(aContainer, aCache);
			DMUID childEntityDUUID = childDUUID.getEntityDUUID();
			if (childEntityDUUID.equals(wantedChildEntityDUUID)) {

				// compute mapping

				AssociationList pms = iu.getPMS();

				Entity childEntity = (Entity) fDUM.getDM(childEntityDUUID);
				if (childEntity == null) {
					logger.error("SA: ASTReferencesSearch.searchInstantiationsUp: Entity of %s not found.", childDUUID);
					return;
				}

				InterfaceList interfaces = childEntity.getPorts();

				MappedInterfaces mapping = map(interfaces, pms);

				HashSetArray<String> actuals = mapping.getActuals(aFormalId);

				if (actuals != null) {
					int n = actuals.size();
					for (int i = 0; i < n; i++) {
						String actualId = actuals.get(i);
						aRes.add(new IdDUUIDTuple(actualId, aParentDUUID));
					}
				}
			}

		} else if (aStmt instanceof Block) {

			Block block = (Block) aStmt;
			int n = block.getNumConcurrentStatements();
			for (int i = 0; i < n; i++) {
				searchInstantiationsUp(block.getConcurrentStatement(i), aParentDUUID, aWantedChildDUUID, aFormalId, aContainer, aCache, aRes);
			}

		} else if (aStmt instanceof GenerateStatement) {

			GenerateStatement generate = (GenerateStatement) aStmt;
			int n = generate.getNumConcurrentStatements();
			for (int i = 0; i < n; i++) {
				searchInstantiationsUp(generate.getConcurrentStatement(i), aParentDUUID, aWantedChildDUUID, aFormalId, aContainer, aCache, aRes);
			}
		}
	}

	private ArrayList<IdDUUIDTuple> searchInitialSignalDeclarations(DMUID aDUUID, String aPortId) throws ZamiaException {

		ArrayList<IdDUUIDTuple> globalResult = new ArrayList<IdDUUIDTuple>();

		logger.debug("SA: searchInitialSignalDeclaration(): uid=%s, portId=%s", aDUUID, aPortId);

		HashSetArray<IdDUUIDTuple> done = new HashSetArray<IdDUUIDTuple>();
		HashSetArray<IdDUUIDTuple> todo = new HashSetArray<IdDUUIDTuple>();

		todo.add(new IdDUUIDTuple(aPortId, aDUUID));

		while (!todo.isEmpty()) {

			IdDUUIDTuple cur = todo.get(0);
			if (done.contains(cur)) {
				continue;
			}
			todo.remove(cur);
			done.add(cur);

			DMUID duuid = cur.fDUUID;

			HashSetArray<IdDUUIDTuple> instantiators = searchInstantiators(duuid, cur.fId);

			if (instantiators != null) {
				int m = instantiators.size();
				if (m > 0) {
					for (int j = 0; j < m; j++) {
						todo.add(instantiators.get(j));
					}
				} else {
					globalResult.add(cur);
				}
			} else {
				globalResult.add(cur);
			}
		}

		return globalResult;
	}

	private void searchSignalReferences(String aId, DMUID aDUUID, boolean aSearchUpward, boolean aSearchDownward, ReferenceSearchResult aResult) throws ZamiaException {

		logger.debug("SA: ReferencesSearch.searchSignalReferences (id='%s', duuid='%s', searchUpward=%b, searchDownward=%b)", aId, aDUUID, aSearchUpward, aSearchDownward);

		String id = aId;
		DMUID duuid = aDUUID;

		if (aSearchUpward) {

			ArrayList<IdDUUIDTuple> decls = searchInitialSignalDeclarations(duuid, id);

			int n = decls.size();
			if (n > 0) {

				for (int i = 0; i < n; i++) {

					IdDUUIDTuple decl = decls.get(i);

					searchSignalReferences(decl.fId, decl.fDUUID, false, true, aResult);
				}
			}

			return;
		}

		ArrayList<SearchJob> todo = new ArrayList<SearchJob>();

		todo.add(new SearchJob(id, aDUUID, 0, aResult));

		HashSet<SearchJob> done = new HashSet<SearchJob>();

		while (!todo.isEmpty()) {

			SearchJob job = todo.remove(0);

			if (done.contains(job))
				continue;

			done.add(job);

			duuid = job.fDUUID;
			id = job.fID;
			ReferenceSearchResult parent = job.parent;

			Architecture arch = (Architecture) fDUM.getDM(duuid);

			ReferenceSearchResult rss = new ReferenceSearchResult(arch.toString() + ": " + id, arch.getLocation(), arch.getId().length());

			parent.add(rss);

			logger.debug("SA: ReferencesSearch.searchSignalReferences(): Looking for references to '%s' in '%s'", id, arch);

			arch.findReferences(id, ObjectCat.Signal, RefType.ReadWrite, job.fDepth, fZPrj, null, null, rss, aSearchDownward ? todo : null);

		}
	}

	private void searchInstantiationSites(DMUID aChildDUUID, ReferenceSearchResult aResult) throws ZamiaException {

		logger.debug("SA: ReferencesSearch.searchInstantiationSites(duuid='%s')", aChildDUUID);

		HashSetArray<DMUID> instantiators = fIGM.findInstantiators(aChildDUUID.getUID());

		if (instantiators == null) {
			logger.debug("SA: ReferencesSearch.searchInstantiationSites(): Was never instantiated: '%s'", aChildDUUID);
			return;
		}

		int m = instantiators.size();

		for (int j = 0; j < m; j++) {

			DMUID parentDUUID = instantiators.get(j);

			logger.debug("SA: ReferencesSearch.searchInstantiationSites(): Found instantiator: '%s'", parentDUUID);

			IDesignModule parentDU = fDUM.getDM(parentDUUID);
			if (!(parentDU instanceof Architecture)) {
				logger.error("SA: ReferencesSearch.searchInstantiationSites(): Architecture for %s not found: %s.", parentDUUID, parentDU);
				continue;
			}

			Architecture parentArch = (Architecture) parentDU;

			String parentSignature = IGInstantiation.computeSignature(parentDUUID, null);

			IGModule parentModule = fIGM.findModule(parentSignature);

			if (parentModule == null) {
				logger.error("SA: ReferencesSearch.searchInstantiationSites(): IGModule for %s not found.", parentDUUID);
				continue;
			}

			IGElaborationEnv cache = new IGElaborationEnv(fZPrj);

			int n = parentArch.getNumConcurrentStatements();
			for (int i = 0; i < n; i++) {
				ConcurrentStatement cs = parentArch.getConcurrentStatement(i);

				searchInstantiationSites(cs, parentDUUID, aChildDUUID, parentModule.getContainer(), cache, aResult);
			}
		}
	}

	private void searchInstantiationSites(ConcurrentStatement aStmt, DMUID aParentDUUID, DMUID aWantedChildDUUID, IGContainer aContainer, IGElaborationEnv aEE,
			ReferenceSearchResult aRes) throws ZamiaException {

		DMUID wantedChildEntityDUUID = aWantedChildDUUID.getEntityDUUID();

		if (aStmt instanceof InstantiatedUnit) {

			InstantiatedUnit iu = (InstantiatedUnit) aStmt;

			DMUID childDUUID = null;

			try {
				if (iu instanceof EntityInstantiation) {
					childDUUID = iu.getChildDUUID(aContainer, aEE);
				} else if (iu instanceof ComponentInstantiation) {

					Name n = ((ComponentInstantiation) iu).getName();

					IGDUUID zduuid = n.computeIGAsDesignUnit(aContainer, aEE, ASTErrorMode.EXCEPTION, null);

					childDUUID = zduuid.getDUUID().getEntityDUUID();
				}
			} catch (Throwable t) {
				el.logException(t);
			}
			if (childDUUID != null) {
				DMUID childEntityDUUID = childDUUID.getEntityDUUID();

				if (childEntityDUUID.equals(wantedChildEntityDUUID)) {
					aRes.add(new ReferenceSite(iu, RefType.Instantiation));
				}
			}
		} else if (aStmt instanceof Block) {

			Block block = (Block) aStmt;
			int n = block.getNumConcurrentStatements();
			for (int i = 0; i < n; i++) {
				searchInstantiationSites(block.getConcurrentStatement(i), aParentDUUID, aWantedChildDUUID, aContainer, aEE, aRes);
			}

		} else if (aStmt instanceof GenerateStatement) {

			GenerateStatement generate = (GenerateStatement) aStmt;
			int n = generate.getNumConcurrentStatements();
			for (int i = 0; i < n; i++) {
				searchInstantiationSites(generate.getConcurrentStatement(i), aParentDUUID, aWantedChildDUUID, aContainer, aEE, aRes);
			}

		}
	}

	public static ReferenceSearchResult search(DeclarativeItem aDeclaration, boolean aSearchUpward, boolean aSearchDownward, ZamiaProject aZPrj) throws ZamiaException {
		ASTReferencesSearch astrs = new ASTReferencesSearch(aZPrj);
		return astrs.search(aDeclaration, aSearchUpward, aSearchDownward);
	}

	private ReferenceSearchResult search(DeclarativeItem aDeclaration, boolean aSearchUpward, boolean aSearchDownward) throws ZamiaException {

		logger.debug("SA: ReferencesSearch.search (declaration='%s', searchUpward=%b, searchDownward=%b)", aDeclaration, aSearchUpward, aSearchDownward);

		String title = "Search for " + aDeclaration;
		if (aSearchUpward) {
			title += " (Global)";
		} else {
			if (aSearchDownward) {
				title += " (Local + Down)";
			} else {
				title += " (Local)";
			}
		}

		ReferenceSearchResult result = new ReferenceSearchResult(title, null, 0);

		if (aDeclaration instanceof Architecture) {

			Architecture arch = (Architecture) aDeclaration;

			searchInstantiationSites(arch.getDMUID(), result);

		} else if (aDeclaration instanceof Entity) {

			Entity entity = (Entity) aDeclaration;

			DMUID duuid = entity.getDMUID();

			DMUID archDUUID = fDUM.getArchDUUID(duuid);

			searchInstantiationSites(archDUUID, result);

		} else if (aDeclaration instanceof InterfaceDeclaration) {

			InterfaceDeclaration idecl = (InterfaceDeclaration) aDeclaration;

			ASTNode parent = idecl.getParent();
			if (parent instanceof InterfaceList) {
				parent = parent.getParent();
			}

			if (parent instanceof Entity) {

				Entity entity = (Entity) parent;

				logger.debug("SA: ReferencesSearch.search(): '%s' is an interface declaration that belongs to entity '%s'.", idecl, entity);

				Architecture arch = fDUM.getArchitecture(entity.getLibId(), entity.getId());

				if (arch == null) {
					logger.error("SA: ReferencesSearch.search(): arch not found for entity: " + entity);
					return null;
				}

				DMUID duuid = arch.getDMUID();

				searchSignalReferences(idecl.getId(), duuid, aSearchUpward, aSearchDownward, result);

			} else {

				// FIXME: what now?

				logger.error("SA: ReferencesSearch.search(): Looking for an interface declaration, but parent is not an entity!");
			}

		} else if (aDeclaration instanceof BlockDeclarativeItem) {

			BlockDeclarativeItem bdi = (BlockDeclarativeItem) aDeclaration;

			ASTNode scope = bdi.getParent();

			String id = bdi.getId();

			ObjectCat cat = ObjectCat.Signal;

			if (bdi instanceof VariableDeclaration) {
				cat = ObjectCat.Variable;
			} else if (bdi instanceof TypeDeclaration) {
				cat = ObjectCat.Type;
			}

			if (cat == ObjectCat.Signal && scope instanceof Architecture) {

				Architecture arch = (Architecture) scope;

				DMUID duuid = arch.getDMUID();

				searchSignalReferences(id, duuid, false, aSearchDownward, result);
			} else {
				if (scope instanceof VHDLNode) {
					VHDLNode vhdlscope = (VHDLNode) scope;
					
					vhdlscope.findReferences(id, cat, RefType.ReadWrite, 0, fZPrj, null, null, result, null);
				}
			}

		} else {

			logger.error("SA: ReferencesSearch.search(): unhandled declaration type: " + aDeclaration);
		}

		return result;
	}

}
