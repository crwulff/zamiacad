/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jul 16, 2005
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
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGSequentialAssignment;
import org.zamia.instgraph.IGType;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class WaveformElement extends ASTObject {

	private Operation fValue, fAfter;

	public WaveformElement(Operation aValue, Operation aAfter, ASTObject aParent, long aLocation) {
		super(aParent, aLocation);
		fValue = aValue;
		fAfter = aAfter;
		if (fValue != null)
			fValue.setParent(this);
		if (fAfter != null)
			fAfter.setParent(this);
	}

	public Operation getValue() {
		return fValue;
	}

	public Operation getDelay() {
		return fAfter;
	}

	@Override
	public int getNumChildren() {
		return 2;
	}

	@Override
	public ASTObject getChild(int aIdx) {
		switch (aIdx) {
		case 0:
			return fValue;
		case 1:
			return fAfter;
		}
		return null;
	}

	public String toVHDL() {
		if (fAfter != null)
			return fValue.toVHDL() + " AFTER " + fAfter.toVHDL();
		return fValue.toVHDL();
	}

	@Override
	public String toString() {
		return toVHDL();
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aRSR, ArrayList<SearchJob> aTODO) throws ZamiaException {

		if (fAfter != null)
			fAfter.findReferences(aId, aCat, RefType.Read, aDepth + 1, aZPrj, aContainer, aCache, aRSR, aTODO);

		fValue.findReferences(aId, aCat, aRefType, aDepth + 1, aZPrj, aContainer, aCache, aRSR, aTODO);
	}

	public IGSequentialAssignment computeIGAssignment(Target aTarget, IGType aTargetType, boolean aInertial, IGOperation aReject, IGContainer aContainer, IGElaborationEnv aEE)
			throws ZamiaException {

		IGOperation zValue = fValue.computeIGOperation(aTargetType, aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);

		IGType valueType = zValue.getType();
		if (!valueType.isAssignmentCompatible(aTargetType, getLocation())) {
			throw new ZamiaException("Type mismatch.", fValue);
		}

		IGOperation zTarget = aTarget.computeIG(aTargetType, aContainer, aEE, ASTErrorMode.EXCEPTION, null);

		IGSequentialAssignment ssa = new IGSequentialAssignment(zValue, zTarget, aInertial, aReject, null, getLocation(), aEE.getZDB());

		if (fAfter != null) {
			ssa.setDelay(fAfter.computeIGOperation(aContainer.findTimeType(), aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null));
		}

		return ssa;
	}
}
