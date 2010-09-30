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
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSequentialAssignment;
import org.zamia.instgraph.IGType;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class Waveform extends ASTObject {

	private ArrayList<WaveformElement> fElements; // of WaveformElement

	private boolean fUnaffected = false;

	public Waveform(ASTObject aParent, long aLocation) {
		super(aParent, aLocation);
		fElements = new ArrayList<WaveformElement>();
	}

	public void setUnaffected(boolean aUnaffected) {
		fUnaffected = aUnaffected;
	}

	public void add(WaveformElement aWE) {
		fElements.add(aWE);
		aWE.setParent(this);
	}

	public int getNumElements() {
		return fElements.size();
	}

	public WaveformElement getElement(int aIdx) {
		return fElements.get(aIdx);
	}

	@Override
	public int getNumChildren() {
		return fElements.size();
	}

	@Override
	public ASTObject getChild(int aIdx) {
		return fElements.get(aIdx);
	}

	public String toVHDL() {
		StringBuilder buf = new StringBuilder();
		int n = getNumElements();
		for (int i = 0; i < n; i++) {
			buf.append(getElement(i).toVHDL());
			if (i < n - 1)
				buf.append(", ");
		}
		return buf.toString();
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {

		int n = getNumElements();
		for (int i = 0; i < n; i++) {

			WaveformElement element = getElement(i);

			element.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		}
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();

		int n = getNumElements();
		for (int i = 0; i < n; i++) {
			WaveformElement we = getElement(i);
			buf.append(we);
			buf.append(" ");
		}

		return buf.toString();
	}

	public void generateIGSequence(Target aTarget, DelayMechanism aDM, IGSequenceOfStatements aStmts, IGContainer aContainer, IGElaborationEnv aEE) throws ZamiaException {

		if (!fUnaffected) {

			int n = getNumElements();

			ErrorReport report = new ErrorReport();

			IGType targetType = aTarget.computeType(null, aContainer, aEE, ASTErrorMode.RETURN_NULL, report);
			if (targetType == null) {
				// see if we can get a typeHint from the right side of the assignment

				for (int i = 0; i < n; i++) {

					WaveformElement we = getElement(i);

					Operation value = we.getValue();
					if (value == null) {
						continue;
					}

					IGType typeHint = value.computeType(null, aContainer, aEE, new IGOperationCache(), ASTErrorMode.RETURN_NULL, report);
					if (typeHint == null) {
						continue;
					}

					targetType = aTarget.computeType(typeHint, aContainer, aEE, ASTErrorMode.RETURN_NULL, report);

					if (targetType != null) {
						break;
					}
				}

				if (targetType == null) {
					// produce error message
					throw new ZamiaException("Couldn't compute target type\n" + report, this);
				}
			}

			IGOperation zReject = null;
			boolean inertial = true;
			if (aDM != null) {
				inertial = aDM.isInertial();
				Operation reject = aDM.getRejectTime();
				if (reject != null) {

					IGType timeType = aContainer.findTimeType();

					zReject = reject.computeIGOperation(timeType, aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);
				}
			}

			for (int j = 0; j < n; j++) {

				WaveformElement we = getElement(j);

				IGSequentialAssignment ssa = we.computeIGAssignment(aTarget, targetType, inertial, zReject, aContainer, aEE);
				aStmts.add(ssa);

				// delay mechanism only applies to the first specified waveform, 
				// all subsequent waveforms schedule transport delay

				inertial = false;
				zReject = null;

			}

		}
	}
}
