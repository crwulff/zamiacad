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

import org.zamia.SourceLocation;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGInterpreterContext;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.util.HashSetArray;
import org.zamia.util.PathName;
import org.zamia.util.ZStack;
import org.zamia.vhdl.ast.DUUID;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGModule extends IGDesignUnit {

	private IGStructure fStructure;

	private ArrayList<IGStaticValue> fActualGenerics;
	
	private boolean fStatementsElaborated = false;

	public IGModule(ToplevelPath aPath, DUUID aDUUID, SourceLocation aLocation, ZDB aZDB) {
		super(aDUUID, aLocation, aZDB);
		fStructure = new IGStructure(new IGInterpreterContext(), aPath, 0, "", aLocation, aZDB);
		fActualGenerics = new ArrayList<IGStaticValue>();
	}

	public IGStructure getStructure() {
		return fStructure;
	}

	public boolean isStatementsElaborated() {
		return fStatementsElaborated;
	}
	
	public void setStatementsElaborated(boolean aStatementsElaborated) {
		fStatementsElaborated = aStatementsElaborated;
	}
	
	@Override
	public String toString() {
		return "IGModule(duuid=" + getDUUID() + ")";
	}

	static class VisitJob {
		IGStructure fStructure;

		PathName fPath;

		public VisitJob(PathName aPath, IGStructure aStructure) {
			fStructure = aStructure;
			fPath = aPath;
		}

		public PathName getPath() {
			return fPath;
		}

		public IGStructure getStructure() {
			return fStructure;
		}
	}

	public void accept(IGStructureVisitor aVisitor, int aMaxDepth) throws ZamiaException {

		IGManager igm = getIGM();

		ZStack<VisitJob> stack = new ZStack<VisitJob>();

		stack.push(new VisitJob(new PathName(""), fStructure));

		while (!stack.isEmpty()) {

			VisitJob job = stack.pop();

			IGStructure structure = job.getStructure();
			PathName path = job.getPath();

			if (path.getNumSegments() > aMaxDepth) {
				continue;
			}
			
			aVisitor.visit(structure, path);

			int n = structure.getNumStatements();
			for (int i = 0; i < n; i++) {
				IGConcurrentStatement stmt = structure.getStatement(i);

				if (stmt instanceof IGInstantiation) {

					IGInstantiation inst = (IGInstantiation) stmt;

					IGModule module = igm.findModule(inst.getSignature());
					if (module == null) {
						throw new ZamiaException("IGModule: accept(): ERROR: detected missing module: "+ inst);
					}

					if (!module.isStatementsElaborated()) {
						throw new ZamiaException("IGModule: accept(): ERROR: detected uninstantiaded module: "+ inst);
					}
					
					stack.push(new VisitJob(path.append(inst.getLabel()), module.getStructure()));
				} else if (stmt instanceof IGStructure) {
					
					String label = stmt.getLabel();
					if (label != null) {
						stack.push(new VisitJob(path.append(label), (IGStructure) stmt));
					} else {
						stack.push(new VisitJob(path, (IGStructure) stmt));
					}
				}
			}
		}
	}

	public IGItem findChild(String aLabel) {

		return getStructure().findChild(aLabel);
	}

	@Override
	public IGItem getChild(int aIdx) {
		return fStructure;
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	@Override
	public IGContainer getContainer() {
		return getStructure().getContainer();
	}

	public int getNumActualGenerics() {
		return fActualGenerics.size();
	}

	public void addActualGeneric(IGStaticValue aValue) {
		fActualGenerics.add(aValue);
	}

	public IGStaticValue getActualGeneric(int aIdx) {
		return fActualGenerics.get(aIdx);
	}

	public void updateInstantiations(HashSetArray<String> aDeleteNodes) {
		ZamiaProject zprj = getZPrj();
		
		IGInterpreterCode ic = new IGInterpreterCode("IGModule " + this, computeSourceLocation());
		IGInterpreterRuntimeEnv env = new IGInterpreterRuntimeEnv(ic, zprj);
		env.pushContext(zprj.getDUM().getGlobalPackageContext());

		IGElaborationEnv ee = new IGElaborationEnv(getZPrj());
		ee.setInterpreterEnv(env);
		
		fStructure.updateInstantiations(aDeleteNodes, ee);
	}
}
