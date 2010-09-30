/*
 * Copyright 2004-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.vhdl.ast;

import java.io.PrintStream;
import java.util.ArrayList;

import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGProcess;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSequentialStatement;
import org.zamia.instgraph.IGSequentialWait;
import org.zamia.instgraph.IGStructure;
import org.zamia.util.HashSetArray;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class SequentialProcess extends ConcurrentStatement {

	public static final boolean dump = false;

	private ArrayList<Name> fSensitivityList; // of Name

	private SequenceOfStatements fStatementSequence;

	private ArrayList<BlockDeclarativeItem> fDeclarations = new ArrayList<BlockDeclarativeItem>();

	public SequentialProcess(ASTObject aParent, long aLocation) {
		super(aParent, aLocation);
		fStatementSequence = null;
	}

	public void setStatementSequence(SequenceOfStatements aStatementSequence) {
		fStatementSequence = aStatementSequence;
		fStatementSequence.setParent(this);
	}

	public void addSensitivity(Name aName) {
		if (fSensitivityList == null) {
			fSensitivityList = new ArrayList<Name>(1);
		}
		fSensitivityList.add(aName);
		aName.setParent(this);
	}

	public void add(BlockDeclarativeItem aItem) {
		fDeclarations.add(aItem);
		aItem.setParent(this);
	}

	public SequenceOfStatements getStatementSequence() {
		return fStatementSequence;
	}

	@Override
	public int getNumChildren() {

		int sln = fSensitivityList != null ? fSensitivityList.size() : 0;

		return fDeclarations.size() + sln + 1;
	}

	@Override
	public ASTObject getChild(int aIdx) {
		switch (aIdx) {
		case 0:
			return fStatementSequence;
		}
		aIdx -= 1;
		int n = fDeclarations.size();
		if (aIdx >= n) {

			aIdx -= n;
			return fSensitivityList.get(aIdx);
		}
		return fDeclarations.get(aIdx);
	}

	public int getNumDeclarations() {
		return fDeclarations.size();
	}

	public BlockDeclarativeItem getDeclaration(int aIdx) {
		return fDeclarations.get(aIdx);
	}

	private void dumpVHDL(int aIndent, PrintStream aOut, SequenceOfStatements aSeq) {

		String pstr = fPostponed ? "POSTPONED " : "";

		if (fLabel != null)
			printlnIndented(fLabel + ": " + pstr + "PROCESS ", aIndent, aOut);
		else
			printlnIndented(pstr + "PROCESS ", aIndent, aOut);

		int n = fDeclarations.size();
		for (int i = 0; i < n; i++) {
			BlockDeclarativeItem decl = fDeclarations.get(i);
			decl.dump(aOut);
		}

		printlnIndented("BEGIN", aIndent, aOut);

		aSeq.dumpVHDL(aIndent + 2, aOut);

		printlnIndented("END PROCESS;", aIndent, aOut);
	}

	@Override
	public void dumpVHDL(int aIndent, PrintStream aOut) {
		dumpVHDL(aIndent, aOut, fStatementSequence);
	}

	@Override
	public DeclarativeItem findDeclaration(String aId, ZamiaProject aZPrj) {

		int n = fDeclarations.size();
		for (int i = 0; i < n; i++) {
			BlockDeclarativeItem decl = fDeclarations.get(i);
			if (decl.getId().equals(aId))
				return decl;
		}

		return super.findDeclaration(aId, aZPrj);
	}

	@Override
	public String toString() {
		if (fLabel == null)
			return "unnamed process";
		return fLabel + ": process";
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {

		// do we have a declaration that shadows this id ?
		// or do we need to report the declaration ?

		int n = fDeclarations.size();
		for (int i = 0; i < n; i++) {

			BlockDeclarativeItem decl = fDeclarations.get(i);

			String id = decl.getId();

			if (id.equals(id_)) {
				if (depth_ > 0) {
					return;
				} else {
					result_.add(new ReferenceSite(decl, RefType.Declaration));
				}
			}
		}

		fStatementSequence.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
	}

	@Override
	public void collectIdentifiers(HashSetArray<String> identifiers_, ZamiaProject zprj_) {

		int n = fDeclarations.size();
		for (int i = 0; i < n; i++) {
			identifiers_.add(fDeclarations.get(i).getId());
		}

		super.collectIdentifiers(identifiers_, zprj_);
	}

	@Override
	public void computeIG(DUUID aDUUID, IGContainer aContainer, IGStructure aStructure, IGElaborationEnv aEE) throws ZamiaException {
		IGProcess proc = new IGProcess(fPostponed, aContainer.getDBID(), fLabel, getLocation(), aEE.getZDB());

		IGContainer container = proc.getContainer();

		// declarations:

		int n = fDeclarations.size();
		for (int i = 0; i < n; i++) {
			BlockDeclarativeItem item = fDeclarations.get(i);
			item.computeIG(null, container, aEE);
		}

		// statements

		IGSequenceOfStatements zs = new IGSequenceOfStatements(null, getLocation(), aEE.getZDB());
		fStatementSequence.generateIG(zs, container, aEE);

		proc.setStatementSequence(zs);

		if (fSensitivityList != null) {

			n = fSensitivityList.size();

			ArrayList<IGOperation> sensList = new ArrayList<IGOperation>(n);

			for (int i = 0; i < n; i++) {

				Name name = fSensitivityList.get(i);

				IGOperation op = name.computeIGAsOperation(null, container, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);

				sensList.add(op);
			}

			proc.appendFinalWait(sensList);

		} else {

			n = zs.getNumStatements();

			IGSequentialStatement lastStmt = n > 0 ? zs.getStatement(n - 1) : null;

			if (lastStmt == null || (!(lastStmt instanceof IGSequentialWait))) {
				proc.appendFinalWait(null);
			}
		}

		aStructure.addStatement(proc);
	}

}