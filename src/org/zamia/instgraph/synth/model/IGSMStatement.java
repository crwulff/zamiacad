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
import org.zamia.instgraph.synth.IGSynth;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public abstract class IGSMStatement{

	public IGSMStatement(String aLabel, SourceLocation aLocation, IGSynth aSynth) {
		
	}
	
	
	public abstract void dump(int aIndent) ;

}
