/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Aug 8, 2005
 */

package org.zamia.vhdl.ast;

import java.io.PrintStream;
import java.util.ArrayList;

import org.zamia.ErrorReport;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGContainerItem;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGMapping;
import org.zamia.instgraph.IGMappings;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGOperationObject;
import org.zamia.instgraph.IGProcess;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSequentialAssignment;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGStructure;
import org.zamia.instgraph.IGObject.IGObjectCat;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGInterpreterContext;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.util.HashSetArray;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class Block extends ConcurrentStatement {

	private Operation fGuard;

	private ArrayList<ConcurrentStatement> fCSS = new ArrayList<ConcurrentStatement>(5);

	private ArrayList<BlockDeclarativeItem> fDecls = new ArrayList<BlockDeclarativeItem>(5);

	private InterfaceList fGenerics;

	private InterfaceList fPorts;

	private AssociationList fPMS;

	private AssociationList fGMS;

	public Block(String aLabel, Operation aGuard, VHDLNode aParent, long aLocation) {
		super(aLabel, aParent, aLocation);
		fGuard = aGuard;
	}

	public void add(ConcurrentStatement aStmt) {
		if (aStmt != null) {
			fCSS.add(aStmt);
			aStmt.setParent(this);
		}
	}

	public void add(BlockDeclarativeItem aItem) {
		if (aItem != null) {
			fDecls.add(aItem);
			aItem.setParent(this);
		}
	}

	public void setGenerics(InterfaceList aGenerics) {
		fGenerics = aGenerics;
		fGenerics.setParent(this);
	}

	public void setGenericMap(AssociationList aGMS) {
		fGMS = aGMS;
		if (fGMS != null) {
			fGMS.setParent(this);
		}
	}

	public void setPorts(InterfaceList aPorts) {
		fPorts = aPorts;
		fPorts.setParent(this);
	}

	public void setPortMap(AssociationList aPMS) {
		fPMS = aPMS;
		if (fPMS != null) {
			fPMS.setParent(this);
		}
	}

	@Override
	public int getNumChildren() {
		int n1 = fDecls != null ? fDecls.size() : 0;
		int n2 = fGenerics != null ? fGenerics.getNumInterfaces() : 0;
		int n3 = fPorts != null ? fPorts.getNumInterfaces() : 0;
		int n4 = fCSS != null ? fCSS.size() : 0;
		return n1 + n2 + n3 + n4 + 1;
	}

	@Override
	public VHDLNode getChild(int aIdx) {
		int n1 = fDecls != null ? fDecls.size() : 0;
		int n2 = fGenerics != null ? fGenerics.getNumInterfaces() : 0;
		int n3 = fPorts != null ? fPorts.getNumInterfaces() : 0;
		int n4 = fCSS != null ? fCSS.size() : 0;
		if (aIdx >= n1) {
			aIdx -= n1;
			if (aIdx >= n2) {
				aIdx -= n2;
				if (aIdx >= n3) {
					aIdx -= n3;
					if (aIdx >= n4) {
						return fGuard;
					} else
						return fCSS.get(aIdx);

				} else
					return fPorts.get(aIdx);

			} else
				return fGenerics.get(aIdx);
		} else
			return fDecls.get(aIdx);
	}

	@Override
	public void dumpVHDL(int aIndent, PrintStream aOut) throws ZamiaException {

		printIndented("BLOCK ", aIndent, aOut);
		if (fGuard != null) {
			aOut.print("(" + fGuard.toVHDL() + ")");
		}
		aOut.println(" IS");

		if (fGenerics != null) {
			printlnIndented("GENERIC (", aIndent + 1, aOut);
			fGenerics.dumpVHDL(aIndent + 2, aOut);
			printlnIndented(")", aIndent + 1, aOut);

			if (fGMS != null) {
				printlnIndented("GENERIC MAP (", aIndent + 1, aOut);
				fGMS.dumpVHDL(aIndent + 2, aOut);
				printlnIndented(")", aIndent + 1, aOut);
			}
			printlnIndented(";", aIndent + 1, aOut);
		}

		if (fPorts != null) {
			printlnIndented("PORT (", aIndent + 1, aOut);
			fPorts.dumpVHDL(aIndent + 2, aOut);
			printlnIndented(")", aIndent + 1, aOut);

			if (fPMS != null) {
				printlnIndented("PORT MAP (", aIndent + 1, aOut);
				fPMS.dumpVHDL(aIndent + 2, aOut);
				printlnIndented(")", aIndent + 1, aOut);
			}
			printlnIndented(";", aIndent + 1, aOut);
		}

		int n = fDecls.size();
		for (int i = 0; i < n; i++) {
			BlockDeclarativeItem decl = fDecls.get(i);
			decl.dumpVHDL(aIndent + 1, aOut);
		}

		printlnIndented("BEGIN", aIndent, aOut);

		n = fCSS.size();
		for (int i = 0; i < n; i++) {
			ConcurrentStatement stmt = fCSS.get(i);
			stmt.dumpVHDL(aIndent + 1, aOut);
		}
		printlnIndented("END BLOCK ;", aIndent, aOut);
	}

	@Override
	public void computeIG(DMUID aDUUID, IGContainer aContainer, IGStructure aStructure, IGElaborationEnv aEE) throws ZamiaException {

		String label = getLabel();
		ToplevelPath path = aStructure.getPath();
		if (label != null) {
			path = path.append(label);
		}

		IGElaborationEnv blockEE = new IGElaborationEnv(aEE.getZamiaProject());

		IGInterpreterRuntimeEnv env = aEE.getInterpreterEnv();
		blockEE.setInterpreterEnv(env);

		IGInterpreterContext context = env.enterContext();

		IGStructure blockStructure = new IGStructure(context, path, aContainer.getDBID(), getLabel(), getLocation(), aEE.getZDB());

		IGContainer blockContainer = blockStructure.getContainer();
		/*
		 *  generics
		 */

		if (fGenerics != null) {

			int n = fGenerics.getNumInterfaces();
			for (int i = 0; i < n; i++) {
				try {
					InterfaceDeclaration interf = fGenerics.get(i);

					IGObject igg = (IGObject) interf.computeIG(null, blockContainer, blockEE);

					blockContainer.addGeneric(igg);

					env.newObject(igg, interf.getLocation());

				} catch (ZamiaException e) {
					reportError(e);
				} catch (Throwable t) {
					el.logException(t);
				}
			}
		}

		if (fGMS != null) {

			ArrayList<IGObject> generics = blockContainer.getGenerics();

			ErrorReport report = new ErrorReport();
			IGMappings mappings = fGMS.map(blockContainer, blockEE, new IGOperationCache(), aContainer, aEE, new IGOperationCache(), generics, true, report, true);
			if (mappings == null) {
				throw new ZamiaException("Failed to compute block generic mappings:\n" + report, this);
			}

			/*
			 * use our interpreter to compute actual generic values
			 */

			IGInterpreterCode ic = new IGInterpreterCode("Generics computation for " + this, getLocation());

			int n = generics.size();
			for (int i = 0; i < n; i++) {
				IGObject generic = generics.get(i);
				env.newObject(generic, generic.computeSourceLocation());
			}

			n = mappings.getNumMappings();
			for (int i = 0; i < n; i++) {

				IGMapping mapping = mappings.getMapping(i);

				mapping.generateInstantiationCode(false, ic, getLocation());

				//inst.addGeneric(mapping);
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

				//actualGenerics.add(new Pair<String, IGStaticValue>(generic.getId(), actualGeneric));

				logger.info("      BLOCK GENERIC %s => %s", generic.getId(), actualGeneric);
			}

		}

		/*
		 *  ports
		 */

		if (fPorts != null) {
			int n = fPorts.getNumInterfaces();
			for (int i = 0; i < n; i++) {
				try {
					InterfaceDeclaration interf = (InterfaceDeclaration) fPorts.get(i);

					IGContainerItem igi = interf.computeIG(null, blockContainer, blockEE);

					blockContainer.addInterface((IGObject) igi);

					if (igi instanceof IGObject) {
						env.newObject((IGObject) igi, interf.getLocation());
					}

				} catch (ZamiaException e) {
					reportError(e);
				} catch (Throwable t) {
					el.logException(t);
				}
			}
		}

		if (fPMS != null) {

			ArrayList<IGObject> interfaces = blockContainer.getInterfaces();

			ErrorReport report = new ErrorReport();
			IGMappings mappings = fPMS.map(blockContainer, blockEE, new IGOperationCache(), aContainer, aEE, new IGOperationCache(), interfaces, true, report, true);
			if (mappings == null) {
				throw new ZamiaException("Failed to compute block port mappings:\n" + report, this);
			}

			int n = mappings.getNumMappings();
			for (int i = 0; i < n; i++) {

				IGMapping mapping = mappings.getMapping(i);

				blockStructure.add(mapping);
			}
		}

		int n = fDecls.size();
		for (int i = 0; i < n; i++) {
			BlockDeclarativeItem decl = fDecls.get(i);

			try {
				IGContainerItem item = decl.computeIG(null, blockContainer, blockEE);

				if (item != null) {
					if (item instanceof IGObject) {
						IGObject obj = (IGObject) item;
						env.newObject(obj, decl.getLocation());
					}
				}

			} catch (ZamiaException e) {
				reportError(e);
			} catch (Throwable t) {
				el.logException(t);
			}

		}

		if (fGuard != null) {

			IGOperation guardOp = fGuard.computeIGOperation(blockContainer.findBoolType(), blockContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);

			IGObject guardSignal = new IGObject(OIDir.NONE, null, IGObjectCat.SIGNAL, guardOp.getType(), "GUARD", fGuard.getLocation(), aEE.getZDB());

			blockContainer.add(guardSignal);
			env.newObject(guardSignal, fGuard.getLocation());

			/*
			 * let's simply turn this into a small process
			 */

			IGProcess proc = new IGProcess(false, blockContainer.getDBID(), null, getLocation(), aEE.getZDB());

			IGSequenceOfStatements sos = new IGSequenceOfStatements(label, getLocation(), aEE.getZDB());
			proc.setStatementSequence(sos);

			IGSequentialAssignment assignment = new IGSequentialAssignment(guardOp, new IGOperationObject(guardSignal, fGuard.getLocation(), aEE.getZDB()), true, null, null,
					fGuard.getLocation(), aEE.getZDB());

			sos.add(assignment);

			proc.appendFinalWait(null);

			blockStructure.addStatement(proc);
		}

		n = fCSS.size();
		for (int i = 0; i < n; i++) {
			ConcurrentStatement stmt = fCSS.get(i);

			try {
				stmt.computeIG(aDUUID, blockContainer, blockStructure, blockEE);
			} catch (ZamiaException e) {
				reportError(e);
			} catch (Throwable t) {
				el.logException(t);
			}
		}

		env.exitContext();

		aStructure.addStatement(blockStructure);
	}

	@Override
	public DeclarativeItem findDeclaration(String aId, ZamiaProject aZPrj) {

		int n = fDecls.size();
		for (int i = 0; i < n; i++) {
			BlockDeclarativeItem decl = fDecls.get(i);
			if (decl.getId().equals(aId))
				return decl;
		}

		return super.findDeclaration(aId, aZPrj);
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {

		// do we have a declaration that shadows this id ?
		// or do we need to report the declaration ?

		int n = fDecls.size();
		for (int i = 0; i < n; i++) {

			BlockDeclarativeItem decl = fDecls.get(i);

			String id = decl.getId();

			if (id.equals(id_)) {
				if (depth_ > 0) {
					return;
				} else {
					result_.add(new ReferenceSite(decl, RefType.Declaration));
				}
			}
		}

		if (fGuard != null) {
			fGuard.findReferences(id_, category_, RefType.Read, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		}

		n = fCSS.size();
		for (int i = 0; i < n; i++) {
			ConcurrentStatement cs = fCSS.get(i);
			cs.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		}
	}

	@Override
	public void collectIdentifiers(HashSetArray<String> identifiers_, ZamiaProject zprj_) {

		int n = fDecls.size();
		for (int i = 0; i < n; i++) {
			identifiers_.add(fDecls.get(i).getId());
		}

		super.collectIdentifiers(identifiers_, zprj_);
	}

	public int getNumConcurrentStatements() {
		return fCSS.size();
	}

	public ConcurrentStatement getConcurrentStatement(int aIdx) {
		return fCSS.get(aIdx);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();

		String l = getLabel();
		if (l != null) {
			buf.append(l + ": ");
		}

		buf.append("BLOCK ");
		if (fGuard != null) {
			buf.append("(" + fGuard + ") ");
		}

		buf.append("IS ");
		if (fGenerics != null) {
			buf.append("GENERIC " + fGenerics + " ");
		}
		if (fGMS != null) {
			buf.append("GENERIC MAP " + fGMS + " ");
		}
		if (fPorts != null) {
			buf.append("PORT " + fPorts + " ");
		}
		if (fPMS != null) {
			buf.append("PORT MAP " + fPMS + " ");
		}

		return buf.toString();
	}
}
