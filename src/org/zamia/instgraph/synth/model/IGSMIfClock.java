/* 
 * Copyright 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 8, 2011
 */
package org.zamia.instgraph.synth.model;

import org.zamia.SourceLocation;
import org.zamia.instgraph.synth.IGClock;
import org.zamia.instgraph.synth.IGSynth;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSMIfClock extends IGSMStatement {

	public IGSMIfClock(IGClock aClock, IGSMSequenceOfStatements aThenStmt, String aLabel, SourceLocation aLocation, IGSynth aSynth) {
		super (aLabel, aLocation, aSynth);
	}

	@Override
	public void dump(int aIndent) {
		// TODO Auto-generated method stub

	}

}
