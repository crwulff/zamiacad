/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.instgraph;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.instgraph.interpreter.IGCallStmt;
import org.zamia.instgraph.interpreter.IGEnterNewContextStmt;
import org.zamia.instgraph.interpreter.IGExitContextStmt;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGNewObjectStmt;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class IGOperationInvokeSubprogram extends IGOperation {

	private IGMappings fMappings;

	private long fSPDBID;

	private int fOpLine, fOpCol;

	public IGOperationInvokeSubprogram(IGMappings aMappings, IGSubProgram aSP, SourceLocation aSrc, ZDB aZDB) {
		this(aMappings, aSP, aSrc, aSrc, aZDB);
	}

	public IGOperationInvokeSubprogram(IGMappings aMappings, IGSubProgram aSP, SourceLocation aSrc, SourceLocation aOpLocation, ZDB aZDB) {
		super(aSP.getReturnType(), aSrc, aZDB);
		fMappings = aMappings;
		fOpLine = aOpLocation.fLine;
		fOpCol = aOpLocation.fCol;
		if (aMappings == null) {
			logger.error("IGOperationInvokeSubprogram: foobar. Sanity check failed.");
		}
		fSPDBID = save(aSP);
	}

	@Override
	public void computeAccessedItems(boolean aLeftSide, IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {

		if (fMappings == null) {
			return;
		}

		for (IGMapping mapping : fMappings) {
			IGOperation formal = mapping.getFormal();
			OIDir dir = OIDir.NONE;
			try {
				dir = formal.getDirection();
			} catch (ZamiaException e) {
				el.logException(e);
			}
			boolean formalIsLeftSide = dir == OIDir.IN;

			mapping.getFormal().computeAccessedItems(formalIsLeftSide, aFilterItem, aFilterType, aDepth, aAccessedItems);
			mapping.getActual().computeAccessedItems(!formalIsLeftSide, aFilterItem, aFilterType, aDepth, aAccessedItems);
		}

	}

	@Override
	public void generateCode(boolean aFromInside, IGInterpreterCode aCode) throws ZamiaException {

		aCode.add(new IGEnterNewContextStmt(computeSourceLocation(), getZDB()));

		IGSubProgram sub = getSub();

		IGContainer subContainer = sub.getContainer();

		// create and init interfaces

		int n = subContainer.getNumLocalItems();
		for (int i = 0; i < n; i++) {

			IGContainerItem item = subContainer.getLocalItem(i);

			if (!(item instanceof IGObject)) {
				continue;
			}

			IGObject obj = (IGObject) item;
			if (obj.getDirection() == OIDir.NONE) {
				continue;
			}
			aCode.add(new IGNewObjectStmt(obj, computeSourceLocation(), getZDB()));
		}

		// map interfaces

		for (IGMapping mapping : fMappings) {
			mapping.generateCode(aCode, computeSourceLocation());
		}

		aCode.add(new IGCallStmt(getSub(), computeSourceLocation(), computeOpSourceLocation(), getZDB()));

		aCode.add(new IGExitContextStmt(computeSourceLocation(), getZDB()));
	}

	public SourceLocation computeOpSourceLocation() {

		SourceLocation loc = computeSourceLocation();
		loc.fLine = fOpLine;
		loc.fCol = fOpCol;
		return loc;
	}

	@Override
	public OIDir getDirection() throws ZamiaException {
		return OIDir.NONE;
	}

	@Override
	public int getNumOperands() {
		return fMappings.size();
	}

	@Override
	public IGOperation getOperand(int aIdx) {
		return fMappings.get(aIdx).getActual();
	}

	@Override
	public String toString() {

		StringBuilder buf = new StringBuilder(getSub().getId() + "(");

		int n = fMappings.size();
		for (int i = 0; i < n; i++) {
			buf.append(fMappings.get(i));
			if (i < n - 1)
				buf.append(", ");
		}

		buf.append(")");

		return buf.toString();
	}

	@Override
	public String toHRString() {

		if (isBinaryOp()) {
			return "(" + fMappings.get(0).getActual().toHRString() + ")" + getSub().getId() + "(" + fMappings.get(1).getActual().toHRString() + ")";
		}

		StringBuilder buf = new StringBuilder(getSub().getId() + "(");

		int n = fMappings.size();
		for (int i = 0; i < n; i++) {
			buf.append(fMappings.get(i).toHRString());
			if (i < n - 1)
				buf.append(", ");
		}

		buf.append(")");

		return buf.toString();
	}

	private boolean isBinaryOp() {

		String id = getSub().getId();
		if (id.charAt(0) != '"') {
			return false;
		}

		int n = fMappings.size();

		return n == 2;
	}

	public IGSubProgram getSub() {
		return (IGSubProgram) getZDB().load(fSPDBID);
	}

	public int getScore() {
		return fMappings.getScore();
	}

	public int getNumMappings() {
		return fMappings.size();
	}

	public IGMapping getMapping(int aIdx) {
		return fMappings.get(aIdx);
	}

}
