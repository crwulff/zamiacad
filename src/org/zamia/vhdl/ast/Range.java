/*
 * Copyright 2004-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.ErrorReport;
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
import org.zamia.instgraph.IGRange;
import org.zamia.instgraph.IGType;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class Range extends ASTObject {

	public final static int DIR_UP = 1;

	public final static int DIR_DOWN = 2;

	private Operation fLeft, fRight;

	private Name fName;

	private boolean fAscending;

	public Range(Operation aLeft, int aDir, Operation aRight, ASTObject aParent, long aLocation) {
		super(aParent, aLocation);
		setLeft(aLeft);
		setRight(aRight);
		fAscending = (aDir == DIR_UP);
	}

	public Range(Name aName, ASTObject aParent, long aLocation) {
		super(aParent, aLocation);
		fName = aName;
		if (fName != null) {
			fName.setParent(this);
		}
	}

	private void setRight(Operation aRight) {
		fRight = aRight;
		fRight.setParent(this);
	}

	private void setLeft(Operation aLeft) {
		fLeft = aLeft;
		fLeft.setParent(this);
	}

	public Range(Operation aLeft, Operation aRight, boolean aAscending, ASTObject aParent, long aLocation) {
		super(aParent, aLocation);
		setLeft(aLeft);
		setRight(aRight);
		fAscending = aAscending;
	}

	public boolean isRange() {
		return fName == null && fLeft != fRight;
	}

	public boolean isAscending() {
		return fAscending;
	}

	public Operation getRight() {
		return fRight;
	}

	public Operation getLeft() {
		return fLeft;
	}

	public String toString() {
		if (fName != null)
			return fName.toString();
		if (fLeft != fRight) {
			if (isAscending())
				return "" + fLeft + " to " + fRight;
			return "" + fLeft + " downto " + fRight;
		}
		return fLeft.toString();
	}

	@Override
	public int getNumChildren() {
		return 3;
	}

	@Override
	public ASTObject getChild(int idx_) {
		switch (idx_) {
		case 0:
			return fLeft;
		case 1:
			return fRight;
		case 2:
			return fName;
		}
		return null;
	}

	public String getSimpleId() throws ZamiaException {

		Name n = getName();

		if (n == null) {

			if (!isRange()) {
				Operation l = getLeft();
				if (l instanceof OperationName) {
					n = ((OperationName) l).getName();
				}
			}
		}
		if (n == null) {
			throw new ZamiaException("Simple identifier expected.", this);
		}

		if (n.getNumExtensions() > 0)
			throw new ZamiaException("Simple identifier expected.", this);

		return n.getId();
	}

	public boolean isName() {
		return fName != null;
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {

		if (fName != null) {
			fName.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		} else {

			fLeft.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
			if (fRight != null && fLeft != fRight) {
				fRight.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
			}
		}

	}

	public Name getName() {
		return fName;
	}

	public IGOperation computeIG(IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {
		if (fName != null) {
			return fName.computeIGAsOperation(aTypeHint, aContainer, aEE, aCache, aErrorMode, aReport);
		}

		if (!isRange()) {
			return fLeft.computeIGOperation(aTypeHint, aContainer, aEE, aCache, aErrorMode, aReport);
		}

		IGType et = null;
		if (aTypeHint != null) {
			if (!aTypeHint.isRange()) {
				reportError("Range type hint expected here.", this, aErrorMode, aReport);
				return null;
			}
			et = aTypeHint.getElementType();
		}

		IGOperation zl = fLeft.computeIGOperation(et, aContainer, aEE, aCache, aErrorMode, aReport);
		if (zl == null) {
			return null;
		}
		IGOperation zr = fRight.computeIGOperation(et, aContainer, aEE, aCache, aErrorMode, aReport);
		if (zr == null) {
			return null;
		}
		IGOperation za = fAscending ? aContainer.findTrueValue() : aContainer.findFalseValue();

		if (aTypeHint != null) {
			IGRange range = new IGRange(aTypeHint, getLocation(), aEE.getZDB());
			range.setLeft(zl);
			range.setRight(zr);
			range.setAscending(za);

			return range;
		}

		return new IGRange(zl, zr, za, getLocation(), aEE.getZDB());
	}

}
