/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jun 19, 2005
 */

package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGDUUID;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGOperationDeref;
import org.zamia.instgraph.IGOperationIndex;
import org.zamia.instgraph.IGOperationInvokeSubprogram;
import org.zamia.instgraph.IGOperationRange;
import org.zamia.instgraph.IGOperationTypeConversion;
import org.zamia.instgraph.IGSubProgram;
import org.zamia.instgraph.IGType;
import org.zamia.vhdl.ast.DUUID.LUType;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class NameExtensionRange extends NameExtension {

	private ArrayList<Range> fRanges = new ArrayList<Range>();

	public NameExtensionRange(Range aRange, ASTObject aParent, long aLocation) {
		super(aParent, aLocation);
		add(aRange);
	}

	public void add(Range aRange) {
		if (aRange != null) {
			fRanges.add(aRange);
			aRange.setParent(this);
		}
	}

	public Range getRange(int aIdx) {
		return fRanges.get(aIdx);
	}

	public String toString() {
		StringBuilder buf = new StringBuilder("(");
		for (int i = 0; i < fRanges.size(); i++) {
			Range range = fRanges.get(i);
			buf.append(range.toString());

			if (i < fRanges.size() - 1)
				buf.append(", ");

		}
		buf.append(")");
		return buf.toString();
	}

	@Override
	public int getNumChildren() {
		return fRanges.size();
	}

	@Override
	public ASTObject getChild(int aIdx) {
		return fRanges.get(aIdx);
	}

	@Override
	public String toVHDL() {
		return toString();
	}

	public int getNumRanges() {
		return fRanges.size();
	}

	@Override
	public void computeIG(IGItem aItem, SourceLocation aPrevLocation, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ArrayList<IGItem> aResult, ErrorReport aReport)
			throws ZamiaException {
		IGItem obj = aItem;

		if (obj instanceof IGType) {

			IGType type = (IGType) obj;

			int nRanges = fRanges.size();

			if (nRanges == 1) {
				// might be a type conversion

				Range range = fRanges.get(0);
				IGOperation rObj = range.computeIG(null, aContainer, aEE, aCache, ASTErrorMode.RETURN_NULL, aReport);
				if (rObj != null) {
					IGType rT = rObj.getType();

					if (!rT.isRange()) {
						aResult.add(new IGOperationTypeConversion(rObj, type, getLocation(), aEE.getZDB()));
					}
				}
			}

			// subtyping ?

			for (int i = 0; i < nRanges; i++) {

				Range range = fRanges.get(i);

				IGType indexType = type.getIndexType();

				IGType rangeType = null;
				if (indexType != null) {
					IGOperation idxRange = indexType.getRange();
					if (idxRange != null) {
						rangeType = idxRange.getType();
					}
				}

				IGOperation rObj = range.computeIG(rangeType, aContainer, aEE, aCache, ASTErrorMode.RETURN_NULL, aReport);
				if (rObj == null) {
					return;
				}
				IGType rT = rObj.getType();

				if (!rT.isRange()) {
					aReport.append("Range expected here.", getLocation());
					return;
				}

				type = type.createSubtype(rObj, aEE.getInterpreterEnv(), getLocation());

			}

			aResult.add(type);
			return;
		}

		if (obj instanceof IGSubProgram) {

			IGSubProgram sp = (IGSubProgram) obj;

			int nRanges = fRanges.size();
			AssociationList al = new AssociationList(this, getLineCol());

			for (int i = 0; i < nRanges; i++) {
				Range range = fRanges.get(i);
				AssociationElement ae = new AssociationElement(this, getLineCol());

				if (range.isRange()) {
					aReport.append("Ranges are not supported here.", getLocation());
					return;
				}

				ae.setActualPart(range.isName() ? new OperationName(range.getName(), ae, range.getLineCol()) : range.getLeft());
				al.add(ae);
			}

			IGOperationInvokeSubprogram invocation = sp.generateInvocation(al, aContainer, aEE, aCache, aPrevLocation, aReport);
			if (invocation != null) {
				aResult.add(invocation);
			}
			return;
		}

		if (obj instanceof IGDUUID) {

			if (fRanges.size() != 1) {
				aReport.append("Architecture name expected here.", getLocation());
				return;
			}

			Range r = fRanges.get(0);
			Name name = r.getName();
			if (name == null) {
				aReport.append("Architecture name expected here.", getLocation());
				return;
			}

			DUUID entityDUUID = ((IGDUUID) obj).getDUUID();
			DUUID archDUUID = new DUUID(LUType.Architecture, entityDUUID.getLibId(), entityDUUID.getId(), name.getId());

			aResult.add(new IGDUUID(archDUUID, getLocation(), aEE.getZDB()));
			return;
		}

		if (!(obj instanceof IGOperation)) {
			aReport.append("Operation expected here, found " + obj + " instead.", getLocation());
			return;
		}

		IGOperation op = (IGOperation) obj;

		int nRanges = fRanges.size();
		for (int i = 0; i < nRanges; i++) {

			IGType type = op.getType();

			while (type.isAccess()) {
				IGType ast = type.getElementType();
				op = new IGOperationDeref(op, ast, getLocation(), aEE.getZDB());
				type = ast;
			}

			if (!type.isArray()) {
				aReport.append("Tried to index something that is not an array: " + op, getLocation());
				return;
			}

			Range range = fRanges.get(i);

			//IGOperation rObj = range.computeIG(null, aContainer, aEE, aCache, ASTErrorMode.RETURN_NULL, aReport);

			IGType indexType = type.getIndexType();

			IGOperation rObj = range.computeIG(indexType, aContainer, aEE, aCache, ASTErrorMode.RETURN_NULL, aReport);

			if (rObj == null) {
				IGType rangeType = null;
				if (indexType != null) {
					IGOperation idxRange = indexType.getRange();
					if (idxRange != null) {
						rangeType = idxRange.getType();
						rObj = range.computeIG(rangeType, aContainer, aEE, aCache, ASTErrorMode.RETURN_NULL, aReport);
					}
				}
			}

			if (rObj == null) {
				aReport.append("Couldn't compute range: " + range, getLocation());
				return;
			}

			IGType rType = rObj.getType();

			if (rType.isDiscrete()) {

				IGOperation zExp = (IGOperation) rObj;

				// IGType tExp = zExp.getType();
				IGType subtype;
				// if (tExp instanceof IGTypeRef) {
				//
				// IGType t = ((IGTypeRef) tExp).getRefType();
				//
				// if (!(t instanceof IGTypeDiscrete)) {
				// throw new
				// ZamiaException("Discrete range/type expected here.", range);
				// }
				//
				// IGTypeDiscrete td = (IGTypeDiscrete) t;
				//
				// subtype = typeArray.createSubtype(td.getRange(),
				// td.getDeclaration(), container_, this);
				//
				// } else {
				subtype = type.getElementType();
				// }

				op = new IGOperationIndex(zExp, op, subtype, getLocation(), aEE.getZDB());

				type = subtype;

			} else if (rType.isRange()) {

				IGType resType = type.createSubtype(rObj, aEE.getInterpreterEnv(), getLocation());

				op = new IGOperationRange(rObj, op, resType, getLocation(), aEE.getZDB());

				type = resType;
			} else {
				aReport.append("Range or expression expected here:" + range, range.getLocation());
				return;
			}
		}
		aResult.add(op);
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aResult, ArrayList<SearchJob> aTODO) throws ZamiaException {
		int n = getNumRanges();
		for (int i = 0; i < n; i++) {
			Range range = getRange(i);
			range.findReferences(aId, aCat, aRefType, aDepth + 1, aZPrj, aContainer, aCache, aResult, aTODO);
		}
	}
}
