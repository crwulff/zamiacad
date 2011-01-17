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
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.synth.model.IGSMExprNode;
import org.zamia.instgraph.synth.model.IGSMSequenceOfStatements;
import org.zamia.instgraph.synth.model.IGSMTarget;

/**
 * 
 * @author Guenter Bartsch
 *
 */
public abstract class IGOperationSynthAdapter {

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	/**
	 * - inline any subprogram calls contained in this operation (and its children)
	 * - unroll loops
	 * - convert case-statements to if-then-else
	 * 
	 * @param aValue
	 * @param aOR
	 * @param aPreprocessedSOS
	 * @param aSynth
	 * @return
	 */

	public IGSMExprNode preprocess(IGOperation aOperation, IGObjectRemapping aOR, IGSMSequenceOfStatements aPreprocessedSOS, IGSynth aSynth) throws ZamiaException {
		throw new ZamiaException("Sorry, not implemented yet.");
	}

	public IGSMTarget preprocessTarget(IGOperation aOperation, IGObjectRemapping aOR, IGSMSequenceOfStatements aPreprocessedSOS, IGSynth aSynth) throws ZamiaException {
		throw new ZamiaException("Sorry, not implemented yet.");
	}

}
