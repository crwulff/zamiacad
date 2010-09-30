/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jul 16, 2005
 */

package org.zamia.vhdl.ast;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;

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
import org.zamia.instgraph.IGOperationArrayAggregate;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGOperationRecordAggregate;
import org.zamia.instgraph.IGRecordField;
import org.zamia.instgraph.IGType;


/**
 * Represents VHDL aggregates, e.g. (0=>'1',1=>'0',others=>'Z')
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class Aggregate extends ASTObject {

	private ArrayList<ElementAssociation> fEAs; // of ElementAssociation

	private Operation fOthers;

	public Aggregate(ASTObject aParent, long aLocation) {
		super(aParent, aLocation);
		fEAs = new ArrayList<ElementAssociation>();
	}

	public void add(ElementAssociation aEA) throws ZamiaException {

		// others? implicit / explicit mode?

		ArrayList<Range> choices = aEA.getChoices();

		boolean othersDetected = false;

		if (choices != null) {
			if (choices.get(0) == null) {
				if (fOthers != null)
					throw new ZamiaException("Duplicate others value detected.", aEA.getLocation());

				fOthers = aEA.getExpression();
				othersDetected = true;
			}
		}

		if (!othersDetected) {
			fEAs.add(aEA);
		}
		aEA.setParent(this);
	}

	public Operation getOthers() {
		return fOthers;
	}

	public int getNumElements() {
		return fEAs.size();
	}

	public ElementAssociation getElement(int aIdx) {
		return fEAs.get(aIdx);
	}

	public String toString() {
		StringBuilder buf = new StringBuilder("Aggregate(");

		int n = fEAs.size();
		for (int i = 0; i < n; i++) {
			buf.append(fEAs.get(i));
			if (i < (n - 1))
				buf.append(", ");
		}
		if (fOthers != null)
			buf.append(", others => " + fOthers);

		return buf.toString() + ")@" + Integer.toHexString(hashCode());
	}

	public String toVHDL() {
		StringBuilder buf = new StringBuilder("(");

		int n = fEAs.size();
		for (int i = 0; i < n; i++) {

			ElementAssociation ea = fEAs.get(i);

			ArrayList<Range> choices = ea.getChoices();
			if (choices != null) {
				int num = 0;
				num = choices.size();

				for (int j = 0; j < num; j++) {
					Range range = choices.get(j);
					if (range != null) {
						buf.append(range + ",");
					}
				}
				buf.append(" => ");
			}
			Operation o = ea.getExpression();
			buf.append(o.toVHDL());
			if (i < (n - 1))
				buf.append(", ");
		}
		if (fOthers != null)
			buf.append(", others => " + fOthers.toVHDL());

		return buf.toString() + ")";
	}

	@Override
	public int getNumChildren() {
		return fEAs.size() + 1;
	}

	@Override
	public ASTObject getChild(int aIdx) {
		if (aIdx == 0)
			return fOthers;
		return fEAs.get(aIdx - 1);
	}

	public void dump(PrintStream aOut, int aIndent) {
		for (int i = 0; i < aIndent; i++)
			aOut.print(" ");

		aOut.println("Aggregate");

		int n = getNumElements();
		for (int i = 0; i < n; i++) {
			ElementAssociation element = fEAs.get(i);

			element.getExpression().dump(aOut, aIndent + 2);

		}
	}

	public void setOthers(Operation others_) {
		fOthers = others_;
		fOthers.setParent(this);
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
		if (fOthers != null) {
			fOthers.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		}

		int n = getNumElements();
		for (int i = 0; i < n; i++) {

			ElementAssociation element = getElement(i);

			element.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		}
	}

	/**
	 * is this really an aggregate or just an expression with extra parenthesis?
	 * 
	 * @return true if this is a true aggregate, false otherwise
	 */
	public boolean isAggregate() {
		if (getNumElements() == 1 && getOthers() == null) {
			ElementAssociation ea = getElement(0);
			if (ea.getChoices() == null) {
				return false;
			}
		}
		return true;
	}

	public IGOperation computeIG(IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		if (aTypeHint == null) {
			reportError("Cannot determine aggregate type.", this, aErrorMode, aReport);
			return null;
		}

		ErrorReport report = aReport;
		if (report == null) {
			report = new ErrorReport();
		}

		IGType type = aTypeHint;

		// must be either an array or record type

		IGOperation result = null;

		if (type.isArray()) {

			IGType indexType = type.getIndexType();
			IGType elementType = type.getElementType();

			int n = getNumElements();

			Operation oothers = getOthers();
			IGOperation others = null;
			if (oothers != null) {
				others = oothers.computeIGOperation(elementType, aContainer, aEE, aCache, aErrorMode, report);
				if (others == null) {
					return null;
				}
			}

			IGOperationArrayAggregate igAggregate = new IGOperationArrayAggregate(type, getLocation(), aEE.getZDB());
			result = igAggregate;

			for (int i = 0; i < n; i++) {

				ElementAssociation ea = getElement(i);

				IGOperation obj = ea.getExpression().computeIGOperation(elementType, aContainer, aEE, aCache, aErrorMode, report);
				if (obj == null) {
					return null;
				}

				ArrayList<Range> choices = ea.getChoices();

				if (choices != null) {

					int num = choices.size();

					for (int j = 0; j < num; j++) {
						Range range = choices.get(j);

						// the type hint is crucial here:
						// could be the index type if we have a single choice
						// could also be the corresponding range type if we have
						// a true range here => try both

						IGOperation igRange = range.computeIG(indexType, aContainer, aEE, aCache, ASTErrorMode.RETURN_NULL, report);
						if (igRange == null) {

							IGOperation indexRange = indexType.getRange();
							if (indexRange != null) {

								IGType rangeType = indexRange.getType();//new IGType(TypeCat.RANGE, null, null, null, indexType, null, false, getLocation(), aCache.getZDB());

								igRange = range.computeIG(rangeType, aContainer, aEE, aCache, aErrorMode, report);
							}
							if (igRange == null) {
								return null;
							}
						}

						igAggregate.add((IGOperation) igRange, obj);

					}
				} else {
					igAggregate.add(obj);
				}
			}

			if (others != null) {
				igAggregate.setOthers(others);
			}

		} else if (type.isRecord()) {

			IGOperationRecordAggregate opRecordAggregate = new IGOperationRecordAggregate(type, getLocation(), aEE.getZDB());
			result = opRecordAggregate;

			int n = getNumElements();
			int nFields = aTypeHint.getNumRecordFields(getLocation());
			HashSet<String> assignedEntries = new HashSet<String>(nFields);

			boolean seenExplicit = false;
			for (int i = 0; i < n; i++) {

				ElementAssociation ea = getElement(i);
				if (ea.isImplicit()) {
					if (seenExplicit) {
						throw new ZamiaException("Illegal mix of explicit and positional assiciations in record aggregate.", getLocation());
					}

					if (i >= nFields) {
						reportError("Too many positional arguments in record aggregate.", getLocation(), aErrorMode, aReport);
						return null;
					}

					IGRecordField rf = type.getRecordField(i, getLocation());

					IGType elementType = rf.getType();

					IGOperation obj = ea.getExpression().computeIGOperation(elementType, aContainer, aEE, aCache, aErrorMode, report);
					if (obj == null) {
						return null;
					}

					opRecordAggregate.set(rf, obj);

					assignedEntries.add(rf.getId());

				} else {

					seenExplicit = true;

					ArrayList<Range> choices = ea.getChoices();
					int num = choices.size();

					for (int j = 0; j < num; j++) {
						Range range = choices.get(j);

						if (range.isRange())
							throw new ZamiaException("Ranges not supported in record aggregates.", ea);

						Operation l = range.getLeft();
						if (!(l instanceof OperationName))
							throw new ZamiaException("Identifier expected here.", l);
						Name name = ((OperationName) l).getName();
						if (name.getNumExtensions() > 0)
							throw new ZamiaException("Identifier expected here.", name);

						String id = name.getId();

						IGRecordField rf = type.findRecordField(id, getLocation());

						if (rf == null) {
							reportError("Unknown field " + id, name, aErrorMode, aReport);
							return null;
						}

						IGType elementType = rf.getType();

						IGOperation obj = ea.getExpression().computeIGOperation(elementType, aContainer, aEE, aCache, aErrorMode, report);
						if (obj == null) {
							return null;
						}

						opRecordAggregate.set(rf, obj);
						assignedEntries.add(rf.getId());

					}
				}
			}

			// others

			Operation others = getOthers();

			for (int i = 0; i < nFields; i++) {
				IGRecordField rf = aTypeHint.getRecordField(i, getLocation());

				String id = rf.getId();

				if (assignedEntries.contains(id)) {
					continue;
				}

				if (others == null) {
					reportError("Field " + id + " not assigned and no others given.", getLocation(), aErrorMode, aReport);
					return null;
				}

				IGOperation oothers = others.computeIGOperation(rf.getType(), aContainer, aEE, aCache, aErrorMode, report);
				if (oothers == null) {
					return null;
				}
				opRecordAggregate.set(rf, oothers);
			}

		} else {
			reportError("For aggregates, only array and record types are supported.", this, aErrorMode, aReport);
			return null;
		}

		return result;
	}
}
