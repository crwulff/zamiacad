/* 
 * Copyright 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 8, 2011
 */
package org.zamia.instgraph.synth.model;

import java.util.ArrayList;

import org.zamia.SourceLocation;
import org.zamia.instgraph.synth.IGSynth;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSMSequenceOfStatements extends IGSMStatement {

	private final ArrayList<IGSMStatement> fStmts = new ArrayList<IGSMStatement>();

	public IGSMSequenceOfStatements(String aLabel, SourceLocation aLocation, IGSynth aSynth) {
		super(aLabel, aLocation, aSynth);
	}

	private int getNumStatements() {
		return fStmts.size();
	}

	private IGSMStatement getStatement(int aIdx) {
		return fStmts.get(aIdx);
	}

	@Override
	public void dump(int aIndent) {
		int n = getNumStatements();
		for (int i = 0; i < n; i++) {
			IGSMStatement stmt = getStatement(i);
			stmt.dump(aIndent + 2);
		}
	}

	public void add(IGSMStatement aStatement) {
		fStmts.add(aStatement);
	}

}
