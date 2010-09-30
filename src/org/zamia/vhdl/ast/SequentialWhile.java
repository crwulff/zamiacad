/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jul 24, 2005
 */

package org.zamia.vhdl.ast;

import org.zamia.ZamiaException;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSequentialLoop;
import org.zamia.instgraph.IGType;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class SequentialWhile extends SequentialLoop {

	private Operation fCond;

	public SequentialWhile(Operation aCond, String aLabel, ASTObject aParent, long aLocation) {
		super(aLabel, aParent, aLocation);

		fCond = aCond;
		fCond.setParent(this);

	}

	@Override
	public void generateIG(IGSequenceOfStatements aSeq, IGContainer aContainer, IGElaborationEnv aEE) throws ZamiaException {

		IGType b = aContainer.findBoolType();
		IGOperation zilCond = fCond.computeIGOperation(b, aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);

		IGSequentialLoop zilLoop = new IGSequentialLoop(zilCond, aContainer.getDBID(), getLabel(), getLocation(), aEE.getZDB());

		IGSequenceOfStatements zilBody = new IGSequenceOfStatements(null, getLocation(), aEE.getZDB());
		body.generateIG(zilBody, zilLoop.getContainer(), aEE);

		zilLoop.setBody(zilBody);

		aSeq.add(zilLoop);

	}
}
