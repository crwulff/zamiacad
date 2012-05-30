/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on May 28, 2007
 */

package org.zamia.vhdl.ast;

import java.io.PrintStream;
import java.util.ArrayList;

import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGProcess;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSequentialAssert;
import org.zamia.instgraph.IGStructure;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class ConcurrentAssertion extends ConcurrentStatement {

	private Assertion fAssertion;

	public ConcurrentAssertion(Assertion aAssertion, VHDLNode aParent, long aLocation) {
		super(aParent, aLocation);
		fAssertion = aAssertion;
		if (fAssertion != null) {
			fAssertion.setParent(this);
		}
	}

	@Override
	public void dumpVHDL(int aIndent, PrintStream aOut) {
		if (fPostponed) {
			printlnIndented("POSTPONED " + fAssertion.toVHDL(), aIndent, aOut);
		} else {
			printlnIndented(fAssertion.toVHDL(), aIndent, aOut);
		}
	}

	@Override
	public VHDLNode getChild(int aIdx) {
		return fAssertion;
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	public Assertion getAssertion() {
		return fAssertion;
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aEE,
			ReferenceSearchResult aRSR, ArrayList<SearchJob> aTODO) throws ZamiaException {
		fAssertion.findReferences(aId, aCat, aRefType, aDepth + 1, aZPrj, aContainer, aEE, aRSR, aTODO);
	}

	public void computeIG(DMUID aDUUID, IGContainer aContainer, IGStructure aStructure, IGElaborationEnv aEE) throws ZamiaException {

		/*
		 * let's simply turn this into a small process
		 */

		IGProcess proc = new IGProcess(fPostponed, aContainer.getDBID(), fLabel, getLocation(), aEE.getZDB());

		IGSequenceOfStatements sos = new IGSequenceOfStatements(fLabel, getLocation(), aEE.getZDB());
		proc.setStatementSequence(sos);

		IGSequentialAssert zAssert = fAssertion.computeIG(fLabel, aContainer, aEE, new IGOperationCache());

		sos.add(zAssert);

		proc.appendFinalWait(null);

		aStructure.addStatement(proc);
	}

}
