/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on May 16, 2009
 */
package org.zamia.instgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.zamia.ASTNode;
import org.zamia.ERManager;
import org.zamia.SourceLocation;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaException;
import org.zamia.ZamiaException.ExCat;
import org.zamia.ZamiaProject;
import org.zamia.analysis.SourceLocation2AST;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.util.HashSetArray;
import org.zamia.util.ZdbList;
import org.zamia.vhdl.ast.InstantiatedUnit;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGStructure extends IGConcurrentStatement implements Scope {

	private ZdbList<IGConcurrentStatement> fStatements = new ZdbList<>();

	private HashMap<String, Long> fLabeledStatements = new HashMap<String, Long>();

	private ArrayList<IGMapping> fMappings = new ArrayList<IGMapping>();

	private long fContainerDBID;

	private transient IGContainer fContainer = null;

	private ToplevelPath fPath;

	public IGStructure(ToplevelPath aPath, long aParentContainerDBID, String aLabel, SourceLocation aLocation, ZDB aZDB) {
		super(aLabel, aLocation, aZDB);

		fPath = aPath;
		fContainer = new IGContainer(aParentContainerDBID, aLocation, aZDB);
		fContainerDBID = aZDB.store(fContainer);
	}

	public IGContainer getContainer() {
		if (fContainer == null) {
			fContainer = (IGContainer) getZDB().load(fContainerDBID);
		}

		return fContainer;
	}
	
	public IGStructure getStructure() {
		return this;
	}

	public void addStatement(IGConcurrentStatement aStmt) {
		long dbid = aStmt.storeOrUpdate();
		fStatements.add(dbid);
		String label = aStmt.getLabel();
		if (label != null) {
			fLabeledStatements.put(label, dbid);
		}
	}

	private void setStatement(int aIdx, IGConcurrentStatement aStmt) {
		long dbid = aStmt.storeOrUpdate();
		fStatements.set(aIdx, dbid);
		String label = aStmt.getLabel();
		if (label != null) {
			fLabeledStatements.put(label, dbid);
		}
	}

	public int getNumStatements() {
		return fStatements.size();
	}

	public IGConcurrentStatement getStatement(int idx) {
		return (IGConcurrentStatement) getZDB().load(fStatements.get(idx));
	}

	public Iterable<IGConcurrentStatement> getStatements() {
		return fStatements.zdbIterator(getZDB());
	}
	
	public IGConcurrentStatement findStatement(String aLabel) {
		Long dbid = fLabeledStatements.get(aLabel);

		if (dbid == null) {
			return null;
		}

		return (IGConcurrentStatement) getZDB().load(dbid.longValue());
	}

	public ToplevelPath getPath() {
		return fPath;
	}

	public void add(IGMapping aMapping) {
		fMappings.add(aMapping);
	}

	public int getNumMappings() {
		return fMappings.size();
	}

	public IGMapping getMapping(int aIdx) {
		return fMappings.get(aIdx);
	}

	@Override
	public IGItem findChild(String aLabel) {

		IGItem child = findStatement(aLabel);
		if (child != null) {
			return child;
		}

		IGContainer container = getContainer();

		if (container == null) {
			return null;
		}

		ArrayList<IGContainerItem> items = container.findLocalItems(aLabel);
		if (items == null) {
			return null;
		}
		int n = items.size();
		if (n == 0) {
			return null;
		}
		if (n > 1) {
			logger.warn("Warning: IGStructure %s: more than one item labeled %s found.", this, aLabel);
		}
		return items.get(0);
	}

	@Override
	public IGItem getChild(int aIdx) {
		int idx = aIdx;
		if (idx == 0)
			return getContainer();
		idx--;
		int n = fMappings.size();
		if (idx < n) {
			return fMappings.get(idx);
		}
		idx -= n;
		return getStatement(idx);
	}

	@Override
	public int getNumChildren() {
		return 1 + fStatements.size() + fMappings.size();
	}

	@Override
	public String toString() {
		String label = getLabel();
		if (label == null) {
			label = "";
		} else {
			label = label + ": ";
		}
		return label + "IGStructure (" + computeSourceLocation() + ")";
	}

	public void updateInstantiations(HashSetArray<String> aDeleteNodes, IGElaborationEnv aEE, ArrayList<IGStaticValue> aActualGenerics) {

		IGInterpreterRuntimeEnv env = aEE.getInterpreterEnv();

		initEnv(getContainer(), env, aActualGenerics);

		boolean hasLocalContext = env.hasContextFor(this);
		if (hasLocalContext) { // FIXME: todo: never occurs, at least in IncrementalUpdateIGTest
			env.pushContextFor(this);
		}

		ZamiaProject zprj = getZPrj();
		ERManager erm = zprj.getERM();

		int n = getNumStatements();
		for (int i = 0; i < n; i++) {

			IGConcurrentStatement stmt = getStatement(i);

			if (stmt instanceof IGStructure) {
				IGStructure struct = (IGStructure) stmt;
				struct.updateInstantiations(aDeleteNodes, aEE, aActualGenerics);
			} else if (stmt instanceof IGInstantiation) {
				IGInstantiation inst = (IGInstantiation) stmt;

				String signature = inst.getSignature();
				if (aDeleteNodes.contains(signature)) {

					SourceLocation location = inst.computeSourceLocation();

					logger.info("IGStructure: Updating instantiation %s at %s", inst, location);

					try {
						ASTNode asto = SourceLocation2AST.findNearestASTNode(location, true, zprj);

						while (asto != null) {
							if (asto instanceof InstantiatedUnit) {
								break;
							}
							asto = asto.getParent();
						}

						if (asto instanceof InstantiatedUnit) {

							InstantiatedUnit iu = (InstantiatedUnit) asto;

							erm.removeErrors(iu, ExCat.INTERMEDIATE);

							inst = iu.computeIGInstantiation(inst.getDUUID(), getContainer(), this, aEE);

							if (inst != null) {
								setStatement(i, inst);
							} else {
								logger.error("IGStructure: ERROR: Failed to re-elaborate instantiation: %s", asto);
							}

						} else {
							logger.error("IGStructure: ERROR: AST object is not an instantiation stmt: %s", asto);
						}
					} catch (Throwable e) {
						el.logException(e);
					}
				}
			}
		}

		if (hasLocalContext) {
			env.exitContext();
		}

		storeOrUpdate();
	}

	private void initEnv(IGContainer aContainer, IGInterpreterRuntimeEnv aEnv, ArrayList<IGStaticValue> aActualGenerics) {

		HashSet<Long> processedItems = new HashSet<Long>(aContainer.getNumLocalItems());

		// generics

		int nActualGenerics = aActualGenerics.size();
		int n = aContainer.getNumGenerics();
		for (int i = 0; i < n; i++) {

			IGObject igg = aContainer.getGeneric(i);

			try {

				processedItems.add(igg.getDBID());

				aEnv.newObject(igg, ASTErrorMode.EXCEPTION, null, igg.computeSourceLocation());

				if (i < nActualGenerics) {
					IGStaticValue actualGeneric = aActualGenerics.get(i);
					aEnv.setObjectValue(igg, actualGeneric, actualGeneric.computeSourceLocation());
				}

			} catch (ZamiaException e) {
				logger.error("IGStructure: ERROR: Failed to init environment with generic: %s", igg);
			} catch (Throwable t) {
				el.logException(t);
			}
		}

		// ports

		n = aContainer.getNumInterfaces();
		for (int i = 0; i < n; i++) {

			IGContainerItem igi = aContainer.getInterface(i);

			try {

				processedItems.add(igi.getDBID());

				if (igi instanceof IGObject) {
					aEnv.newObject((IGObject) igi, ASTErrorMode.EXCEPTION, null, igi.computeSourceLocation());
				}

			} catch (ZamiaException e) {
				logger.error("IGStructure: ERROR: Failed to init environment with port: %s", igi);
			} catch (Throwable t) {
				el.logException(t);
			}
		}

		// declarations:

		n = aContainer.getNumLocalItems();
		for (int i = 0; i < n; i++) {

			IGContainerItem item = aContainer.getLocalItem(i);

			if (processedItems.contains(item.getDBID())) {
				continue;
			}

			try {

				if (item instanceof IGObject) {
					IGObject obj = (IGObject) item;
					aEnv.newObject(obj, ASTErrorMode.EXCEPTION, null, item.computeSourceLocation());
				}

			} catch (ZamiaException e) {
				logger.error("IGStructure: ERROR: Failed to init environment with declaration: %s", item);
			} catch (Throwable t) {
				el.logException(t);
			}
		}


	}

}