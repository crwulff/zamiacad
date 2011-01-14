/* 
 * Copyright 2010,2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 23, 2010
 */
package org.zamia.instgraph.synth.adapters;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSequentialStatement;
import org.zamia.instgraph.IGSequentialWait;
import org.zamia.instgraph.synth.IGBindings;
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

public class IGSASequenceOfStatements extends IGStmtSynthAdapter {

	@Override
	public void preprocess(IGSequentialStatement aStmt, IGObjectRemapping aOR, IGSMSequenceOfStatements aPreprocessedSOS, String aReturnVarName, IGClock aClock, IGSynth aSynth)
			throws ZamiaException {

		IGSequenceOfStatements seq = (IGSequenceOfStatements) aStmt;

		int n = seq.getNumStatements();
		for (int i = 0; i < n; i++) {
			IGSequentialStatement stmt = seq.getStatement(i);

			aSynth.getSynthAdapter(stmt).preprocess(stmt, aOR, aPreprocessedSOS, aReturnVarName, aClock, aSynth);
		}
	}

	@Override
	public void inlineSubprograms(IGSequentialStatement aStmt, IGObjectRemapping aOR, IGSequenceOfStatements aInlinedSOS, String aReturnVarName, IGSynth aSynth)
			throws ZamiaException {

		IGSequenceOfStatements seq = (IGSequenceOfStatements) aStmt;

		int n = seq.getNumStatements();
		for (int i = 0; i < n; i++) {
			IGSequentialStatement stmt = seq.getStatement(i);

			aSynth.getSynthAdapter(stmt).inlineSubprograms(stmt, aOR, aInlinedSOS, aReturnVarName, aSynth);
		}

	}

	@Override
	public IGBindings resolveVariables(IGSequentialStatement aStmt, IGSequenceOfStatements aInlinedSOS, IGBindings aBindings, IGSequenceOfStatements aResolvedSOS, IGClock aClock,
			IGObjectRemapping aOR, IGSynth aSynth) throws ZamiaException {
		IGSequenceOfStatements seq = (IGSequenceOfStatements) aStmt;

		IGBindings retBindings = new IGBindings(aBindings);

		int n = seq.getNumStatements();
		for (int i = 0; i < n; i++) {
			IGSequentialStatement stmt = seq.getStatement(i);

			IGBindings tmpBindings = aSynth.getSynthAdapter(stmt).resolveVariables(stmt, aInlinedSOS, retBindings, aResolvedSOS, aClock, aOR, aSynth);

			logger.debug("IGSASequenceOfStatements: resolveVariables(): bindings for %s:", stmt);
			tmpBindings.dumpBindings();

			retBindings.merge(tmpBindings);
		}

		return retBindings;
	}

	@Override
	public IGBindings computeBindings(IGSequentialStatement aStmt, IGSequenceOfStatements aResolvedSOS, IGBindings aLastBindings, IGClock aClock, IGSynth aSynth)
			throws ZamiaException {
		IGSequenceOfStatements seq = (IGSequenceOfStatements) aStmt;

		SourceLocation location = seq.computeSourceLocation();

		IGClock clock = aClock;
		IGBindings newBindings = new IGBindings();

		int n = seq.getNumStatements();
		for (int i = 0; i < n; i++) {
			IGSequentialStatement stmt = seq.getStatement(i);

			if (stmt instanceof IGSequentialWait) {
				if (i > 0)
					throw new ZamiaException("Wait statement has to be the first statement in process to be synthesizeable.", location);
				if (clock != null)
					throw new ZamiaException("Clock has already been specified in this scope.", location);

				IGSequentialWait sw = (IGSequentialWait) stmt;
				clock = aSynth.findClock(sw);
			} else {

				IGBindings tmpBindings = aSynth.getSynthAdapter(stmt).computeBindings(stmt, aResolvedSOS, aLastBindings, clock, aSynth);

				newBindings.merge(tmpBindings);
			}
		}

		return newBindings;
	}

}
