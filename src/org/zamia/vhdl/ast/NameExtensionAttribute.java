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
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationAttribute;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGOperationAttribute.AttrOp;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class NameExtensionAttribute extends NameExtension {

	private String fId;

	private Signature fSignature;

	private Operation fExp;

	public NameExtensionAttribute(String aId, Signature aSignature, Operation aExp, VHDLNode aParent, long aLocation) {
		super(aParent, aLocation);

		fId = aId;
		fSignature = aSignature;
		if (fSignature != null)
			fSignature.setParent(this);
		fExp = aExp;
		if (fExp != null)
			fExp.setParent(this);
	}

	public String getId() {
		return fId;
	}

	@Override
	public int getNumChildren() {
		return 2;
	}

	@Override
	public VHDLNode getChild(int aIdx) {
		switch (aIdx) {
		case 0:
			return fExp;
		case 1:
			return fSignature;
		}

		return null;
	}

	@Override
	public String toVHDL() {

		StringBuilder buf = new StringBuilder("'" + fId);

		if (fSignature != null) {
			buf.append("[" + fSignature + "]");
		}

		if (fExp != null) {
			buf.append("(" + fExp + ")");
		}

		return buf.toString();
	}

	@Override
	public String toString() {
		return toVHDL();
	}

	public Operation getExpression() {
		return fExp;
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aResult, ArrayList<SearchJob> aTODO) throws ZamiaException {
		if (fExp != null) {
			fExp.findReferences(aId, aCat, aRefType, aDepth + 1, aZPrj, aContainer, aCache, aResult, aTODO);
		}
	}


	@Override
	public void computeIG(IGItem aItem, SourceLocation aPrevLocation, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ArrayList<IGItem> aResult, ErrorReport aReport) throws ZamiaException {
		/*
		 * first, user-defined attributes
		 */

		// FIXME: implement
		//		IGAttribute attr = aItem.getAttribute(id);
		//
		//		if (attr != null) {
		//			return attr.getValue();
		//		}
		/*
		 * now, handle predefined attributes
		 */

		AttrOp attrOp = null;
		IGOperation op = null;

		IGType typeHint = null;
		if (aItem instanceof IGOperation) {
			typeHint = ((IGOperation) aItem).getType();
		} else if (aItem instanceof IGType) {
			typeHint = (IGType) aItem;
		} else if (aItem instanceof IGObject) {
			typeHint = ((IGObject) aItem).getType();
		} else {
			reportError("Unknown item in attribute computation: " + aItem, this, ASTErrorMode.RETURN_NULL, aReport);
			return;
		}

		if (typeHint.isAccess()) {
			typeHint = typeHint.getElementType();
		}
		
		IGType idxType = null;
		if (typeHint.isArray()) {
			idxType = typeHint.getIndexType();
		}

		IGType type = null;

		if (fId.equals("LEFT")) {
			op = getIGOpNoSig(aContainer.findIntType(), false, aContainer, aEE, aCache);
			attrOp = AttrOp.LEFT;
			type = idxType != null ? idxType : typeHint;
		} else if (fId.equals("RIGHT")) {
			op = getIGOpNoSig(aContainer.findIntType(), false, aContainer, aEE, aCache);
			attrOp = AttrOp.RIGHT;
			type = idxType != null ? idxType : typeHint;
		} else if (fId.equals("LOW")) {
			op = getIGOpNoSig(aContainer.findIntType(), false, aContainer, aEE, aCache);
			attrOp = AttrOp.LOW;
			type = idxType != null ? idxType : typeHint;
		} else if (fId.equals("HIGH")) {
			op = getIGOpNoSig(aContainer.findIntType(), false, aContainer, aEE, aCache);
			attrOp = AttrOp.HIGH;
			type = idxType != null ? idxType : typeHint;
		} else if (fId.equals("ASCENDING")) {
			op = getIGOpNoSig(aContainer.findIntType(), false, aContainer, aEE, aCache);
			attrOp = AttrOp.ASCENDING;
			type = aContainer.findBoolType();
		} else if (fId.equals("IMAGE")) {
			op = getIGOpNoSig(typeHint, true, aContainer, aEE, aCache);
			attrOp = AttrOp.IMAGE;
			type = aContainer.findStringType();
		} else if (fId.equals("VALUE")) {
			op = getIGOpNoSig(typeHint, true, aContainer, aEE, aCache);
			attrOp = AttrOp.VALUE;
			type = typeHint.getOriginalType();
		} else if (fId.equals("POS")) {
			op = getIGOpNoSig(typeHint, true, aContainer, aEE, aCache);
			attrOp = AttrOp.POS;
			type = aContainer.findIntType();
		} else if (fId.equals("VAL")) {
			op = getIGOpNoSig(aContainer.findIntType(), true, aContainer, aEE, aCache);
			attrOp = AttrOp.VAL;
			type = typeHint.getOriginalType();
		} else if (fId.equals("SUCC")) {
			op = getIGOpNoSig(typeHint, true, aContainer, aEE, aCache);
			attrOp = AttrOp.SUCC;
			type = typeHint.getOriginalType();
		} else if (fId.equals("PRED")) {
			op = getIGOpNoSig(typeHint, true, aContainer, aEE, aCache);
			attrOp = AttrOp.PRED;
			type = typeHint.getOriginalType();
		} else if (fId.equals("LEFTOF")) {
			op = getIGOpNoSig(typeHint, true, aContainer, aEE, aCache);
			attrOp = AttrOp.LEFTOF;
			type = typeHint.getOriginalType();
		} else if (fId.equals("RIGHTOF")) {
			op = getIGOpNoSig(typeHint, true, aContainer, aEE, aCache);
			attrOp = AttrOp.RIGHTOF;
			type = typeHint.getOriginalType();
		} else if (fId.equals("BASE")) {
			checkNoExpNoSig();
			attrOp = AttrOp.BASE;
			type = typeHint.getOriginalType();
		} else if (fId.equals("DELAYED")) {
			op = getIGOpNoSig(aContainer.findTimeType(), false, aContainer, aEE, aCache);
			attrOp = AttrOp.DELAYED;
			type = typeHint.getOriginalType();
		} else if (fId.equals("STABLE")) {
			op = getIGOpNoSig(aContainer.findTimeType(), false, aContainer, aEE, aCache);
			attrOp = AttrOp.STABLE;
			type = aContainer.findBoolType();
		} else if (fId.equals("QUIET")) {
			op = getIGOpNoSig(aContainer.findTimeType(), false, aContainer, aEE, aCache);
			attrOp = AttrOp.QUIET;
			type = aContainer.findBoolType();
		} else if (fId.equals("TRANSACTION")) {
			checkNoExpNoSig();
			attrOp = AttrOp.TRANSACTION;
			type = aContainer.findBitType();
		} else if (fId.equals("EVENT")) {
			checkNoExpNoSig();
			attrOp = AttrOp.EVENT;
			type = aContainer.findBoolType();
		} else if (fId.equals("ACTIVE")) {
			checkNoExpNoSig();
			attrOp = AttrOp.ACTIVE;
			type = aContainer.findBoolType();
		} else if (fId.equals("LAST_EVENT")) {
			checkNoExpNoSig();
			attrOp = AttrOp.LAST_EVENT;
			type = aContainer.findTimeType();
		} else if (fId.equals("LAST_ACTIVE")) {
			checkNoExpNoSig();
			attrOp = AttrOp.LAST_ACTIVE;
			type = aContainer.findTimeType();
		} else if (fId.equals("LAST_VALUE")) {
			checkNoExpNoSig();
			attrOp = AttrOp.LAST_VALUE;
			type = typeHint.getOriginalType();
		} else if (fId.equals("DRIVING")) {
			checkNoExpNoSig();
			attrOp = AttrOp.DRIVING;
			type = aContainer.findBoolType();
		} else if (fId.equals("DRIVING_VALUE")) {
			checkNoExpNoSig();
			attrOp = AttrOp.DRIVING_VALUE;
			type = typeHint.getOriginalType();
		} else if (fId.equals("SIMPLE_NAME")) {
			attrOp = AttrOp.SIMPLE_NAME;
			type = aContainer.findStringType();
		} else if (fId.equals("PATH_NAME")) {
			attrOp = AttrOp.PATH_NAME;
			type = aContainer.findStringType();
		} else if (fId.equals("INSTANCE_NAME")) {
			attrOp = AttrOp.INSTANCE_NAME;
			type = aContainer.findStringType();
		} else if (fId.equals("RANGE")) {
			attrOp = AttrOp.RANGE;
			op = getIGOpNoSig(aContainer.findIntType(), false, aContainer, aEE, aCache);
			type = idxType != null ? idxType.getRange().getType() : typeHint.getRange().getType();
		} else if (fId.equals("REVERSE_RANGE")) {
			attrOp = AttrOp.REVERSE_RANGE;
			op = getIGOpNoSig(aContainer.findIntType(), false, aContainer, aEE, aCache);
			type = idxType != null ? idxType.getRange().getType() : typeHint.getRange().getType();
		} else if (fId.equals("LENGTH")) {
			attrOp = AttrOp.LENGTH;
			op = getIGOpNoSig(aContainer.findIntType(), false, aContainer, aEE, aCache);
			type = idxType != null ? idxType : typeHint;
		}
		if (attrOp == null) {
			throw new ZamiaException("Unknown attribute " + getId(), this);
		}

		aResult.add(new IGOperationAttribute(attrOp, aItem, op, type, getLocation(), aEE.getZDB()));
	}

	private void checkNoExpNoSig() throws ZamiaException {
		if (fExp != null)
			throw new ZamiaException("Predifined attribute " + fId + " doesn't want an expression", fExp);
		if (fSignature != null)
			throw new ZamiaException("Predifined attribute " + fId + " doesn't want a signature", fSignature);
	}

	private IGOperation getIGOpNoSig(IGType aTypeHint, boolean aMandatory, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache) throws ZamiaException {
		if (fSignature != null)
			throw new ZamiaException("Predifined attribute " + fId + " doesn't want a signature", fSignature);

		if (fExp == null) {
			if (aMandatory)
				throw new ZamiaException("Attribute needs an expression", this);
			else
				return null;
		}

		return fExp.computeIGOperation(aTypeHint, aContainer, aEE, aCache, ASTErrorMode.EXCEPTION, null);
	}


}
