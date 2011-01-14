/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
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
import org.zamia.instgraph.IGSequenceOfStatements;
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
	
	public abstract void preprocess(IGSequentialStatement aStmt, IGObjectRemapping aOR, IGSMSequenceOfStatements aPreprocessedSOS, String aReturnVarName, IGClock aClock, IGSynth aSynth) throws ZamiaException;
	
	/**
	 * 
	 * @param aStmt
	 * @param aOR
	 * @param aInlinedSOS
	 * @param aReturnVarName
	 * @param aSynth
	 * @throws ZamiaException
	 */

	public abstract void inlineSubprograms(IGSequentialStatement aStmt, IGObjectRemapping aOR, IGSequenceOfStatements aInlinedSOS, String aReturnVarName, IGSynth aSynth)
			throws ZamiaException;

	
	
	
	/**
	 * 
 	 * this only needs to be implemented in SequentialIf and SequentialAssignments,
	 * so the default is to throw an exception if it is accidently called on anything else
	 * (which should have been replaced by inlining and unrolling before)
	 * @param aStmt
	 * @param aInlinedSOS
	 * @param aBindings
	 * @param aResolvedSOS
	 * @param aClock
	 * @param aOR
	 * @param aSynth
	 * @return
	 * @throws ZamiaException
	 */

	public IGBindings resolveVariables(IGSequentialStatement aStmt, IGSequenceOfStatements aInlinedSOS, IGBindings aBindings, IGSequenceOfStatements aResolvedSOS, IGClock aClock,
			IGObjectRemapping aOR, IGSynth aSynth) throws ZamiaException {
		throw new ZamiaException("Sorry, not implemented yet.");
	}

	public IGBindings computeBindings(IGSequentialStatement aStmt, IGSequenceOfStatements aResolvedSOS, IGBindings aLastBindings, IGClock aClock, IGSynth aSynth) throws ZamiaException {
		throw new ZamiaException("Sorry, not implemented yet.");
	}

}
