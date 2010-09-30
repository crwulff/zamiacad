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
import java.util.HashMap;

import org.zamia.ERManager;
import org.zamia.SourceLocation;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaProject;
import org.zamia.ZamiaException.ExCat;
import org.zamia.analysis.SourceLocation2AST;
import org.zamia.instgraph.interpreter.IGInterpreterContext;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.vhdl.ast.InstantiatedUnit;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGStructure extends IGConcurrentStatement {

	private ArrayList<Long> fStatements = new ArrayList<Long>();

	private HashMap<String, Long> fLabeledStatements = new HashMap<String, Long>();

	private ArrayList<IGMapping> fMappings = new ArrayList<IGMapping>();

	private long fContainerDBID;

	private transient IGContainer fContainer = null;

	private ToplevelPath fPath;

	private IGInterpreterContext fInterpreterContext;

	public IGStructure(IGInterpreterContext aContext, ToplevelPath aPath, long aParentContainerDBID, String aLabel, SourceLocation aLocation, ZDB aZDB) {
		super(aLabel, aLocation, aZDB);

		fPath = aPath;
		fContainer = new IGContainer(aParentContainerDBID, aLocation, aZDB);
		fContainerDBID = aZDB.store(fContainer);
		fInterpreterContext = aContext;
	}

	public IGContainer getContainer() {
		if (fContainer == null) {
			fContainer = (IGContainer) getZDB().load(fContainerDBID);
		}

		return fContainer;
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

	public void updateInstantiations(HashSetArray<String> aDeleteNodes, IGElaborationEnv aEE) {

		IGInterpreterRuntimeEnv env = aEE.getInterpreterEnv();
		
		IGInterpreterContext context = getInterpreterContext();
		if (context != null) {
			env.pushContext(context);
		}

		ZamiaProject zprj = getZPrj();
		ERManager erm = zprj.getERM();
		
		int n = getNumStatements();
		for (int i = 0; i < n; i++) {

			IGConcurrentStatement stmt = getStatement(i);

			if (stmt instanceof IGStructure) {
				IGStructure struct = (IGStructure) stmt;
				struct.updateInstantiations(aDeleteNodes, aEE);
			} else if (stmt instanceof IGInstantiation) {
				IGInstantiation inst = (IGInstantiation) stmt;

				String signature = inst.getSignature();
				if (aDeleteNodes.contains(signature)) {

					SourceLocation location = inst.computeSourceLocation();

					logger.info("IGStructure: Updating instantiation %s at %s", inst, location);

					try {
						ASTObject asto = SourceLocation2AST.findNearestASTObject(location, true, zprj);

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

		if (context != null) {
			env.exitContext();
		}
		
		storeOrUpdate();
	}

	public IGInterpreterContext getInterpreterContext() {
		return fInterpreterContext;
	}

}
