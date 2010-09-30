/* 
 * Copyright 2007-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Sep 23, 2007
 */
package org.zamia.vhdl.ast;

import java.io.PrintStream;
import java.util.ArrayList;

import org.zamia.DUManager;
import org.zamia.ErrorReport;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGDUUID;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGInstantiation;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGOperationInvokeSubprogram;
import org.zamia.instgraph.IGProcess;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSequentialProcedureCall;
import org.zamia.instgraph.IGStructure;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class ConcurrentProcedureCall extends ConcurrentStatement {

	private Name fName;

	public ConcurrentProcedureCall(Name aName, String aLabel, ASTObject aParent, long aLocation) {
		super(aLabel, aParent, aLocation);
		fName = aName;
		fName.setParent(this);
	}

	@Override
	public String toString() {

		StringBuilder buf = new StringBuilder();

		if (fPostponed) {
			buf.append("POSTPONED ");
		}

		buf.append(fName.toVHDL());

		buf.append(";");

		return buf.toString();
	}

	@Override
	public void dumpVHDL(int aIndent, PrintStream aOut) {
		printlnIndented(toString(), aIndent, aOut);
	}

	@Override
	public ASTObject getChild(int aIdx) {
		return fName;
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aEE,
			ReferenceSearchResult aResult, ArrayList<SearchJob> aTODO) throws ZamiaException {
		fName.findReferences(aId, aCat, aRefType, aDepth + 1, aZPrj, aContainer, aEE, aResult, aTODO);
	}

	public void computeIG(DUUID aDUUID, IGContainer aContainer, IGStructure aStruct, IGElaborationEnv aEE) throws ZamiaException {

		/*
		 * let's simply turn this into a small process
		 */

		ErrorReport report = new ErrorReport();
		
		IGOperationInvokeSubprogram inv = fName.computeIGAsProcedure(aContainer, aEE, ASTErrorMode.RETURN_NULL, report);

		if (inv == null) {

			// might be a component instantiation without any mappings

			IGDUUID du = fName.computeIGAsDesignUnit(aContainer, aEE, ASTErrorMode.EXCEPTION, null);

			if (du == null) {
				throw new ZamiaException("Subprogram invocation expected here.\n" + report, this);
			} else {

				DUUID duuid = du.getDUUID();
				
				IGInstantiation inst = new IGInstantiation(aDUUID, duuid, getLabel(), getLocation(), aEE.getZDB());
				inst.computeSignature();
				aStruct.addStatement(inst);

				ToplevelPath path = aStruct.getPath().append(getLabel());

				ZamiaProject zprj = aEE.getZamiaProject();
				DUManager dum = zprj.getDUM();
				IGManager igm = zprj.getIGM();
				
				Architecture arch = dum.getArchitecture(duuid.getLibId(), duuid.getId(), duuid.getArchId());

				if (arch == null) {
					throw new ZamiaException ("Architecture not found for "+duuid, getLocation());
				}
				
				igm.getOrCreateIGModule(path, aDUUID, arch.getDUUID(), inst.getSignature(), inst.getActualGenerics(), true, getLocation());
				
			}
		} else {

			IGProcess proc = new IGProcess(fPostponed, aContainer.getDBID(), fLabel, getLocation(), aEE.getZDB());

			IGSequenceOfStatements sos = new IGSequenceOfStatements(fLabel, getLocation(), aEE.getZDB());
			proc.setStatementSequence(sos);

			sos.add(new IGSequentialProcedureCall(inv, null, getLocation(), aEE.getZDB()));

			proc.appendFinalWait(null);

			aStruct.addStatement(proc);
		}
	}
}
