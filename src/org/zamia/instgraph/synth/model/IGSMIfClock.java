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
import org.zamia.ZamiaException;
import org.zamia.instgraph.synth.IGBindings;
import org.zamia.instgraph.synth.IGClock;
import org.zamia.instgraph.synth.IGSynth;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSMIfClock extends IGSMStatement {

	private final IGSMSequenceOfStatements fThenStmt;
	private final IGClock fClock;

	public IGSMIfClock(IGClock aClock, IGSMSequenceOfStatements aThenStmt, String aLabel, SourceLocation aLocation, IGSynth aSynth) {
		super (aLabel, aLocation, aSynth);
		
		fThenStmt = aThenStmt;
		fClock = aClock;
	}

	@Override
	public void dump(int aIndent) {
		// TODO Auto-generated method stub

	}

	@Override
	public IGBindings computeBindings(IGBindings aBindingsBefore, IGClock aClock, IGSynth aSynth) throws ZamiaException {
		
		if (aClock != null) {
			throw new ZamiaException ("Error: multiple clocks detected", getLocation());
		}
		
		return fThenStmt.computeBindings(aBindingsBefore, fClock, aSynth);
	}

}
