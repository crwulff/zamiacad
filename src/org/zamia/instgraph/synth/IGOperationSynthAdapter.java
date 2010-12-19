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
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.rtlng.RTLSignal;

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
	 * @param aOperation
	 * @param aOR
	 * @param aInlinedSOS
	 *            statement sequence where inlined statements can be added to
	 *            they will be executed before the operation is evaluated
	 * @param aSynth
	 * @return clone of this operation tree with subprogram calls replaced by
	 *         variables whose values have been computed in aInlinedSOS
	 * @throws ZamiaException
	 */

	public abstract IGOperation inlineSubprograms(IGOperation aOperation, IGObjectRemapping aOR, IGSequenceOfStatements aInlinedSOS, IGSynth aSynth) throws ZamiaException;

	/**
	 * replace any nodes representing variables by cloned nodes which carry
	 * information about the current content of that variable
	 * 
	 * @param aOperation
	 * @param aBindings
	 * @param aResolvedSOS
	 * @param aClock
	 * @param aOR
	 * @param aSynth
	 * @return cloned operation tree with nodes containing var information
	 * @throws ZamiaException
	 */

	public IGOperation resolveVariables(IGOperation aOperation, IGBindings aBindings, IGSequenceOfStatements aResolvedSOS, IGClock aClock, IGObjectRemapping aOR, IGSynth aSynth)
			throws ZamiaException {
		throw new ZamiaException("Sorry, not implemented yet.");
	}

	public RTLSignal synthesizeValue(IGOperation aOperation, IGSynth aSynth) throws ZamiaException {
		throw new ZamiaException("Sorry, not implemented yet.");
	}

	public RTLSignal synthesizeEnable(IGOperation aOperation, IGSynth aSynth) throws ZamiaException {
		throw new ZamiaException("Sorry, not implemented yet.");
	}

}
