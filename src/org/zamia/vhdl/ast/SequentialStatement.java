/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jan 8, 2005
 */

package org.zamia.vhdl.ast;

import java.io.PrintStream;

import org.zamia.ZamiaException;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGSequenceOfStatements;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public abstract class SequentialStatement extends ASTObject {
	
	private String label;

	protected SequentialStatement (ASTObject aParent, long aLocation) {
		this (null, aParent, aLocation);
	}

	protected SequentialStatement (String aLabel, ASTObject aParent, long aLocation) {
		super (aParent, aLocation);
		label = aLabel;
	}
	
	public String getLabel() {
		return label;
	}

	public abstract void dumpVHDL(int aIndent, PrintStream aOut);

	public abstract void generateIG(IGSequenceOfStatements aSeq, IGContainer aContainer, IGElaborationEnv aCache) throws ZamiaException;

}
