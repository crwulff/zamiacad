/*
 * Copyright 2004-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGSubProgram;
import org.zamia.instgraph.IGType;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class TypeDefinitionSubType extends TypeDefinition {

	private Name typeMark; /* name of referenced type */

	private ArrayList<DiscreteRange> constraints;

	private Name resF;

	public TypeDefinitionSubType(Name typeMark_, VHDLNode parent_, long location_) {
		super(parent_, location_);
		setTypeMark(typeMark_);
	}

	public void setResolutionFunction(Name name_) {
		resF = name_;
	}

	public void setTypeMark(Name name_) {
		typeMark = name_;
		typeMark.setParent(this);
	}

	/**
	 * set range constraints
	 * 
	 * @param list_
	 *            ArrayList of Ranges
	 */
	public void setConstraint(ArrayList<DiscreteRange> list_) {
		constraints = list_;
		if (constraints != null) {
			int n = constraints.size();
			for (int i = 0; i < n; i++) {
				constraints.get(i).setParent(this);
			}
		}
	}

	public void dump() {
		System.out.println("TypeMark: " + typeMark);
		if (constraints != null) {
			int n = constraints.size();
			for (int i = 0; i < n; i++) {
				DiscreteRange dr = constraints.get(i);
				System.out.println("  Constraint: " + dr);
			}
		} else {
			System.out.println("  no constraints.");
		}
	}

	@Override
	public int getNumChildren() {
		if (constraints == null)
			return 1;
		return 1 + constraints.size();
	}

	@Override
	public VHDLNode getChild(int idx_) {
		if (idx_ == 0)
			return typeMark;
		idx_--;
		return constraints.get(idx_);
	}

	public ArrayList<DiscreteRange> getConstraints() {
		return constraints;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(typeMark.toVHDL());
		if (constraints != null) {
			int n = constraints.size();
			for (int i = 0; i < n; i++) {
				DiscreteRange constraint = constraints.get(i);
				buf.append(" ");
				buf.append(constraint);
			}
		}

		return buf.toString();
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) {

		// FIXME: todo

		logger.warn("%s: findReferences not implemented yet.", getClass());

	}

	@Override
	public IGType computeIG(IGContainer aContainer, IGElaborationEnv aEE) {
		IGType t = null;
		try {

			IGSubProgram rf = null;
			if (resF != null) {
				rf = resF.computeIGAsResF(aContainer, aEE, ASTErrorMode.EXCEPTION, null);

				if (rf == null) {
					throw new ZamiaException("Function expected here.", resF);
				}
			}

			t = typeMark.computeIGAsType(aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);

			if (resF != null) {
				t = t.createSubtype(rf, null, getLocation());
			}

			if (constraints != null) {

				int n = constraints.size();

				for (int i = 0; i < n; i++) {

					DiscreteRange dr = constraints.get(0);

					IGType rType = t.isArray() ? t.getIndexType().getRange().getType() : t.getRange().getType();

					IGItem igRange = dr.computeIG(rType, aContainer, aEE, new IGOperationCache());

					if (!(igRange instanceof IGOperation)) {
						reportError("Range expected here.");
						return t;
					}

					IGOperation opr = (IGOperation) igRange;
					IGType oprT = opr.getType();
					if (!oprT.isRange()) {
						reportError("Range expected here.");
						return t;
					}

					t = t.createSubtype(opr, aEE.getInterpreterEnv(), getLocation());
				}
			}
		} catch (ZamiaException e) {
			reportError(e);
		} catch (Throwable t1) {
			el.logException(t1);
		}

		if (t == null) {
			t = IGType.createErrorType(aEE.getZDB());
		}

		return t;
	}

	/**
	 * used in discrete range (could be a simple range)
	 */
	public IGItem computeIGItem(IGContainer aContainer, IGElaborationEnv aEE) throws ZamiaException {

		if (resF == null && constraints == null) {

			ArrayList<IGItem> items = typeMark.computeIG(null, aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);
			int n = items.size(); // FIXME 
			for (int i = 0; i<n; i++) {
				return items.get(i);
			}
		}

		return computeIG(aContainer, aEE);
	}

	public Name getName() {
		return typeMark;
	}

}