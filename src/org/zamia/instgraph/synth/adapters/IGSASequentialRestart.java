/* 
 * Copyright 2010,2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 23, 2010
 */
package org.zamia.instgraph.synth.adapters;

import org.zamia.ZamiaException;
import org.zamia.instgraph.IGSequentialStatement;
import org.zamia.instgraph.synth.IGClock;
import org.zamia.instgraph.synth.IGObjectRemapping;
import org.zamia.instgraph.synth.IGStmtSynthAdapter;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.instgraph.synth.model.IGSMSequenceOfStatements;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSASequentialRestart extends IGStmtSynthAdapter {

	@Override
	public void preprocess(IGSequentialStatement aStmt, IGObjectRemapping aOR, IGSMSequenceOfStatements aPreprocessedSOS, String aReturnVarName, IGClock aClock, IGSynth aSynth)
			throws ZamiaException {
	}

}
