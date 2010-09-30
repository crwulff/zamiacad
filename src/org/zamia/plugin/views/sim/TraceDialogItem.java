/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Mar 1, 2009
 */
package org.zamia.plugin.views.sim;

import org.zamia.util.PathName;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class TraceDialogItem {

	public PathName fName;
	
	public boolean fIsModule;
	
	public TraceDialogItem (PathName aPathName, boolean aIsModule) {
		fName = aPathName;
		fIsModule = aIsModule;
	}
	
}
