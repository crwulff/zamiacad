/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on May 17, 2009
 */
package org.zamia.instgraph;

import org.zamia.util.PathName;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public interface IGStructureVisitor {

	public void visit (IGStructure aStructure, PathName aPath);
	
}
