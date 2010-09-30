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
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSequentialWait;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class SequentialWait extends SequentialStatement {

	private ArrayList<Name> sensitivityList;

	private Operation timeoutClause, conditionClause;

	public SequentialWait(String label_, ASTObject parent_, long location_) {
		super(label_, parent_, location_);
	}

	public void setConditionClause(Operation op_) {
		conditionClause = op_;
		if (conditionClause != null)
			conditionClause.setParent(this);
	}

	public void setTimeoutClause(Operation op_) {
		timeoutClause = op_;
		if (timeoutClause != null)
			timeoutClause.setParent(this);
	}

	public void setSensitivityList(ArrayList<Name> l_) {
		sensitivityList = l_;
		if (sensitivityList != null) {
			int n = sensitivityList.size();
			for (int i = 0; i < n; i++) {
				Name name = sensitivityList.get(i);
				name.setParent(this);
			}
		}
	}

	@Override
	public int getNumChildren() {
		if (sensitivityList == null)
			return 2;
		return sensitivityList.size() + 2;
	}

	@Override
	public ASTObject getChild(int idx_) {
		switch (idx_) {
		case 0:
			return conditionClause;
		case 1:
			return timeoutClause;
		}
		return sensitivityList.get(idx_ - 2);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("WAIT ");

		if (sensitivityList != null) {
			buf.append("ON ");
			int n = sensitivityList.size();
			for (int i = 0; i < n; i++) {
				Name name = sensitivityList.get(i);

				buf.append(name.toVHDL());
				if (i < (n - 1)) {
					buf.append(", ");
				} else {
					buf.append(" ");
				}
			}
		}

		if (conditionClause != null) {
			buf.append("UNTIL ");
			buf.append(conditionClause.toVHDL());
			buf.append(" ");
		}

		if (timeoutClause != null) {
			buf.append("FOR ");
			buf.append(timeoutClause.toVHDL());
			buf.append(" ");
		}

		return buf.toString();
	}

	@Override
	public void dumpVHDL(int indent_, PrintStream out_) {
		printlnIndented(toString() + ";", indent_, out_);
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {

		if (conditionClause != null)
			conditionClause.findReferences(id_, category_, RefType.Read, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		if (timeoutClause != null)
			timeoutClause.findReferences(id_, category_, RefType.Read, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);

		if (sensitivityList != null) {

			int n = sensitivityList.size();
			for (int i = 0; i < n; i++) {
				sensitivityList.get(i).findReferences(id_, category_, RefType.Read, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
			}
		}
	}

	@Override
	public void generateIG(IGSequenceOfStatements aSeq, IGContainer aContainer, IGElaborationEnv aEE) throws ZamiaException {
		IGOperation zTimeoutClause = timeoutClause != null ? timeoutClause.computeIGOperation(aContainer.findTimeType(), aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null) : null;
		IGOperation zConditionClause = conditionClause != null ? conditionClause.computeIGOperation(aContainer.findBoolType(), aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null) : null;

		ArrayList<IGOperation> zSensitivityList = null;

		if (sensitivityList != null) {
			int n = sensitivityList.size();

			zSensitivityList = new ArrayList<IGOperation>(n);

			for (int i = 0; i < n; i++) {

				Name name = sensitivityList.get(i);

				IGOperation op = name.computeIGAsOperation(null, aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);
				zSensitivityList.add(op);
			}
		}

		aSeq.add(new IGSequentialWait(zTimeoutClause, zConditionClause, zSensitivityList, getLabel(), getLocation(), aEE.getZDB()));
	}
}