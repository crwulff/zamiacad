/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 27, 2010
 */
package org.zamia.instgraph.synth;

import org.zamia.ExceptionLogger;
import org.zamia.ZamiaLogger;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public abstract class IGBindingNode {

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	public abstract void dump(int aI);

}
