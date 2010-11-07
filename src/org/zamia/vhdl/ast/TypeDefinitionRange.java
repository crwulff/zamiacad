/*
 * Copyright 2004-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.vhdl.ast;

import org.zamia.ErrorReport;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.IGType.TypeCat;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class TypeDefinitionRange extends TypeDefinition {

	private Range range; /* constrained array / range */

	public TypeDefinitionRange(Range range_, VHDLNode parent_, long location_) {
		super(parent_, location_);
		setRange(range_);
	}

	public void setRange(Range range_) {
		range = range_;
		range.setParent(this);
	}

	public Range getRange() {
		return range;
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	@Override
	public VHDLNode getChild(int idx_) {
		return range;
	}

	@Override
	public String toString() {
		return range.toString();
	}

	@Override
	public IGType computeIG(IGContainer aContainer, IGElaborationEnv aEE) {
		IGType res = null;

		try {
			IGOperation zRange = range.computeIG(null, aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);

			IGType t = zRange.getType();

			if (!t.isRange()) {
				reportError("Range expected here.");
				return res;
			}

			// check range element to see 
			// integer or a real type here

			IGType et = t.getElementType();

			switch (et.getCat()) {
			case INTEGER:
				res = new IGType(TypeCat.INTEGER, null, zRange, null, null, null, false, getLocation(), aEE.getZDB());

				IGTypeStatic sType = res.computeStaticType(aEE.getInterpreterEnv(), ASTErrorMode.RETURN_NULL, new ErrorReport());
				if (sType != null) {
					res = sType;
				}

				addBuiltinIntOperators(res, aContainer, getLocation());

				break;
			case REAL:
				res = new IGType(TypeCat.REAL, null, zRange, null, null, null, false, getLocation(), aEE.getZDB());

				sType = res.computeStaticType(aEE.getInterpreterEnv(), ASTErrorMode.RETURN_NULL, new ErrorReport());
				if (sType != null) {
					res = sType;
				}

				addBuiltinRealOperators(res, aContainer, getLocation());

				break;
			default:
				reportError("Integer or real typed range expected");
				return res;
			}

		} catch (ZamiaException e) {
			reportError(e);
		} catch (Throwable t) {
			el.logException(t);
		}

		if (res == null) {
			res = IGType.createErrorType(aEE.getZDB());
		}
		//res.storeOrUpdate();

		return res;
	}
}