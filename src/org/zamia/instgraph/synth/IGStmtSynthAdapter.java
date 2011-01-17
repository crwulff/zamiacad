/* 
 * Copyright 2010,2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 23, 2010
 */
package org.zamia.instgraph.synth;

import org.zamia.ExceptionLogger;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.instgraph.IGSequentialStatement;
import org.zamia.instgraph.synth.model.IGSMSequenceOfStatements;

/**
 * 
 * @author Guenter Bartsch
 *
 */
public abstract class IGStmtSynthAdapter {

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	/**
	 * Inline subprograms, unroll loops
	 * 
	 * @param aStmt
	 * @param aOR
	 * @param aPreprocessedSOS
	 * @param aReturnVarName
	 * @param aClock TODO
	 * @param aSynth
	 */

	public abstract void preprocess(IGSequentialStatement aStmt, IGObjectRemapping aOR, IGSMSequenceOfStatements aPreprocessedSOS, String aReturnVarName, IGClock aClock,
			IGSynth aSynth) throws ZamiaException;

}
