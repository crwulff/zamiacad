/* 
 * Copyright 2007-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */

package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.vhdl.ast.VHDLNode;


/**
 * Represents elaborated array types
 * 
 * @author Guenter Bartsch
 */

@SuppressWarnings("serial")
public class ZILTypeArray extends ZILType {

	private ZILType fElementType;

	private ZILTypeDiscrete fIndexType;

	private boolean fUnconstrained;

	public ZILTypeArray(ZILTypeDiscrete aIndexType, ZILType aElementType, boolean aUnconstrained, ZILTypeDeclaration aDeclaration, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aDeclaration, aContainer, aSrc);
		fElementType = aElementType;
		fIndexType = aIndexType;
		fUnconstrained = aUnconstrained;
	}

	public ZILType getElementType() {
		return fElementType;
	}

	@Override
	protected ZILType getSimplifiedType() {
		// if (indexType != null && indexType.getCardinality() == 1)
		// return elementType;
		return super.getSimplifiedType();
	}

	@Override
	protected boolean computeIsCompatible(ZILType aType) {
		if (aType == this)
			return true;

		if (!(aType instanceof ZILTypeArray)) {

			// because of type simplification, an open array is
			// compatible with its base type

			// if (indexType == null)
			// return elementType.isCompatible(t2);
			//			
			return false;
		}

		ZILTypeArray at = (ZILTypeArray) aType;

		ZILTypeDiscrete idx1 = fIndexType;
		ZILTypeDiscrete idx2 = at.getIndexType();

		if (idx1 == null || idx2 == null) {
			return at.getElementType().isCompatible(fElementType);
		}
		// } else if (idx2 == null)
		// return false;
		boolean res = at.getElementType().isCompatible(fElementType) && idx1.isCompatible(idx2);
		if (res) {
			return true;
		}
		// FIXME FIXME
		// if (res) {
		// return idx1.getCardinality() == idx2.getCardinality();
		// }
		// System.out.println ("not equal");
		return at.getElementType().isCompatible(fElementType) && at.getIndexType().isCompatible(fIndexType);
	}

	public ZILTypeDiscrete getIndexType() {
		return fIndexType;
	}

	@Override
	public boolean isLogic() {
		return fElementType.isLogic();
	}

	@Override
	public String toString() {
		return "ARRAY " + fIndexType + " OF " + fElementType;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public ZILType createSubtype(ZILRange aRange, ZILTypeDeclaration aDeclaration, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {

		ZILTypeDiscrete idxType = null;
		if (fIndexType == null) {
			idxType = new ZILTypeInteger(aRange, ZILType.intType, aDeclaration, aContainer, aSrc);
		} else {
			idxType = (ZILTypeDiscrete) fIndexType.createSubtype(aRange, aDeclaration, aContainer, aSrc);
		}

		return new ZILTypeArray(idxType, fElementType, false, null, aContainer, aSrc);
	}

	@Override
	protected ZILType computeEnableType() {
		return new ZILTypeArray(fIndexType, fElementType.getEnableType(), fUnconstrained, null, null, null);
	}

	@Override
	public ZILType convertType(ZILType aType, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {

		if (!(aType instanceof ZILTypeArray)) {
			throw new ZamiaException("Type conversion between array and non-array types is not allowed.", getSrc());
		}

		ZILTypeArray sta = (ZILTypeArray) aType;

		// just check - will throw an exception if it fails
		fElementType.convertType(sta.getElementType(), aContainer, aSrc);

		if (fIndexType == null) {

			ZILTypeDiscrete it = sta.getIndexType();

			if (it == null)
				return this;

			return createSubtype(it.getRange(), getDeclaration(), aContainer, aSrc);
		}

		return this;
	}

	@Override
	public ZILType qualifyType(ZILType aType, ZILIContainer aContainer, VHDLNode aSrc) throws ZamiaException {

		if (!(aType instanceof ZILTypeArray)) {
			throw new ZamiaException("Type qualification between array type " + this + " and non-array type " + aType + " is not allowed.", getSrc());
		}

		ZILTypeArray sta = (ZILTypeArray) aType;

		ZILType et = fElementType.qualifyType(sta.getElementType(), aContainer, aSrc);

		ZILTypeDiscrete it = sta.getIndexType();
		if (it != null || fIndexType == null) {
			return sta;
		}

		return new ZILTypeArray(fIndexType, et, fUnconstrained, null, aContainer, aSrc);
	}

	@Override
	public boolean isOpen() {
		return fIndexType == null;
	}

	@Override
	public boolean isAtomic() {
		return false;
	}

	public boolean isUnconstrained() {
		return fUnconstrained;
	}
}
