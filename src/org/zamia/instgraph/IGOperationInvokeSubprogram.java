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

	private IGSubProgram fSP;

	public IGOperationInvokeSubprogram(IGMappings aMappings, IGSubProgram aSP, SourceLocation aSrc, ZDB aZDB) {
		super(aSP.getReturnType(), aSrc, aZDB);
		fMappings = aMappings;
		if (aMappings == null) {
			logger.error("IGOperationInvokeSubprogram: foobar. Sanity check failed.");
		}
		fSP = aSP;
	}

	@Override
	public void computeAccessedItems(boolean aLeftSide, IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {
		
		if (fMappings == null) {
			return;
		}
		
		int n = fMappings.getNumMappings();
		
		for (int i = 0; i<n; i++) {
			
			IGMapping mapping = fMappings.getMapping(i);

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

		IGSubProgram sub = fSP;

		IGContainer subContainer = sub.getContainer();

		// create and init interfaces

		int n = subContainer.getNumLocalItems();
		for (int i = 0; i < n; i++) {
			
			IGContainerItem item = subContainer.getLocalItem(i);
			
			if (!(item instanceof IGObject)) {
				continue;
			}
			
			IGObject obj = (IGObject) item;
//			if (obj.getInitialValue() == null) {
//				continue;
//			}
			if (obj.getDirection() == OIDir.NONE) {
				continue;
			}
			aCode.add(new IGNewObjectStmt(obj, computeSourceLocation(), getZDB()));
		}

		// map interfaces
		
		n = fMappings.getNumMappings();
		for (int i = 0; i < n; i++) {
			IGMapping mapping = fMappings.getMapping(i);
			mapping.generateEntryCode(aCode, computeSourceLocation());
		}

		// create and init local variables
		
		n = subContainer.getNumLocalItems();
		for (int i = 0; i < n; i++) {
			
			IGContainerItem item = subContainer.getLocalItem(i);
			
			if (!(item instanceof IGObject)) {
				continue;
			}
			
			IGObject obj = (IGObject) item;
//			if (obj.getInitialValue() == null) {
//				continue;
//			}
			if (obj.getDirection() != OIDir.NONE) {
				continue;
			}
			aCode.add(new IGNewObjectStmt(obj, computeSourceLocation(), getZDB()));
		}
		
		aCode.add(new IGCallStmt(fSP, computeSourceLocation(), getZDB()));

		n = fMappings.getNumMappings();
		for (int i = 0; i < n; i++) {
			IGMapping mapping = fMappings.getMapping(i);
			mapping.generateExitCode(aCode, computeSourceLocation());
		}

		aCode.add(new IGExitContextStmt(computeSourceLocation(), getZDB()));
	}

	@Override
	public IGObject generateCodeRef(boolean aFromInside, boolean aCheckDirection, IGInterpreterCode aCode) throws ZamiaException {
		// FIXME
		throw new ZamiaException("Sorry, not implemented yet.");
	}

	@Override
	public OIDir getDirection() throws ZamiaException {
		return OIDir.NONE;
	}

	@Override
	public int getNumOperands() {
		return fMappings.getNumMappings();
	}

	@Override
	public IGOperation getOperand(int aIdx) {
		return fMappings.getMapping(aIdx).getActual();
	}

	@Override
	public String toString() {

		StringBuilder buf = new StringBuilder(fSP.getId() + "(");

		int n = fMappings.getNumMappings();
		for (int i = 0; i < n; i++) {
			buf.append(fMappings.getMapping(i));
			if (i < n - 1)
				buf.append(", ");
		}

		buf.append(")");
		
		return buf.toString();
	}

	public IGSubProgram getSub() {
		return fSP;
	}

	public int getScore() {
		return fMappings.getScore();
	}

}
