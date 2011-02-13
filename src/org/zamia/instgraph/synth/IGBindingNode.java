/* 
 * Copyright 2010,2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 27, 2010
 */
package org.zamia.instgraph.synth;

import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.instgraph.synth.model.IGSMExprNode;
import org.zamia.rtlng.RTLSignal;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public abstract class IGBindingNode {

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	protected final SourceLocation fLocation;
	
	protected IGBindingNode (SourceLocation aLocation) {
		fLocation = aLocation;
	}
	
	public abstract void dump(int aI);

	public abstract IGBindingNode replaceOmega(IGBindingNode aNode) throws ZamiaException;

	public abstract IGSMExprNode computeCombinedEnable(IGSynth aSynth) throws ZamiaException;

	public abstract RTLSignal synthesizeASyncData(IGSMExprNode aAE, RTLSignal aClk, IGSynth aSynth) throws ZamiaException;

}
