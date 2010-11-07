/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jul 24, 2005
 */

package org.zamia.vhdl.ast;

import java.io.PrintStream;

import org.zamia.ZamiaException;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSequentialLoop;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGObject.IGObjectCat;
import org.zamia.instgraph.IGObject.OIDir;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class SequentialFor extends SequentialLoop {

	private String fVarId;

	private Range fRange;

	public SequentialFor(String aVarId, Range aRange, String aLabel, VHDLNode aParent, long aLocation) {
		super(aLabel, aParent, aLocation);
		fVarId = aVarId;
		fRange = aRange;
		fRange.setParent(this);
	}

	@Override
	public void generateIG(IGSequenceOfStatements aSeq, IGContainer aContainer, IGElaborationEnv aCache) throws ZamiaException {

		IGOperation obj = fRange.computeIG(null, aContainer, aCache, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);

		IGType objT = obj.getType();

		if (!objT.isRange()) {
			throw new ZamiaException("Range expected here.", fRange);
		}

		IGObject var = new IGObject(OIDir.NONE, null, IGObjectCat.VARIABLE, objT.getElementType(), fVarId, getLocation(), aCache.getZDB());
		IGSequentialLoop zilLoop = new IGSequentialLoop(var, obj, aContainer.getDBID(), getLabel(), getLocation(), aCache.getZDB());

		IGSequenceOfStatements zilBody = new IGSequenceOfStatements(null, getLocation(), aCache.getZDB());
		body.generateIG(zilBody, zilLoop.getContainer(), aCache);

		zilLoop.setBody(zilBody);

		aSeq.add(zilLoop);

	}

	@Override
	public void dumpVHDL(int aIndent, PrintStream aOut) {
		printlnIndented("for " + fVarId + " in " + fRange + " loop", aIndent, aOut);
		body.dumpVHDL(aIndent + 2, aOut);
		printlnIndented("end loop;", aIndent, aOut);
	}

}
