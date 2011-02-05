/* 
 * Copyright 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 8, 2011
 */
package org.zamia.instgraph.synth.model;

import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.instgraph.synth.IGBindings;
import org.zamia.instgraph.synth.IGSynth;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public abstract class IGSMStatement{

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	protected final SourceLocation fLocation;

	public IGSMStatement(String aLabel, SourceLocation aLocation, IGSynth aSynth) {
		fLocation = aLocation;
	}
	
	public abstract void dump(int aIndent) ;

	public abstract IGBindings computeBindings(IGBindings aBindingsBefore, IGSynth aSynth) throws ZamiaException;

	public SourceLocation getLocation() {
		return fLocation;
	}

}
