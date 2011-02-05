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
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSequentialIf;
import org.zamia.instgraph.IGSequentialStatement;
import org.zamia.instgraph.synth.IGObjectRemapping;
import org.zamia.instgraph.synth.IGStmtSynthAdapter;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.instgraph.synth.model.IGSMExprNode;
import org.zamia.instgraph.synth.model.IGSMIf;
import org.zamia.instgraph.synth.model.IGSMSequenceOfStatements;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSASequentialIf extends IGStmtSynthAdapter {

	@Override
	public void inline(IGSequentialStatement aStmt, IGObjectRemapping aOR, IGSMSequenceOfStatements aPreprocessedSOS, String aReturnVarName, IGSynth aSynth)
			throws ZamiaException {
		IGSequentialIf ifstmt = (IGSequentialIf) aStmt;

		IGSequenceOfStatements thenSOS = ifstmt.getThenSOS();
		IGSequenceOfStatements elseSOS = ifstmt.getElseSOS();
		IGOperation cond = ifstmt.getCond();

		IGSMExprNode icond = aSynth.getSynthAdapter(cond).preprocess(cond, aOR, aPreprocessedSOS, aSynth);

		IGSMSequenceOfStatements ts = new IGSMSequenceOfStatements(null, thenSOS.computeSourceLocation(), aSynth);
		aSynth.getSynthAdapter(thenSOS).inline(thenSOS, aOR, ts, aReturnVarName, aSynth);

		IGSMSequenceOfStatements es;

		if (elseSOS != null) {
			es = new IGSMSequenceOfStatements(null, elseSOS.computeSourceLocation(), aSynth);
			aSynth.getSynthAdapter(elseSOS).inline(elseSOS, aOR, es, aReturnVarName, aSynth);
		} else {
			es = new IGSMSequenceOfStatements(null, thenSOS.computeSourceLocation(), aSynth);
		}

		IGSMIf se = new IGSMIf(icond, ts, es, ifstmt.getId(), ifstmt.computeSourceLocation(), aSynth);

		aPreprocessedSOS.add(se);

	}

}
