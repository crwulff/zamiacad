/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 10, 2009
 */
package org.zamia.instgraph;

import org.zamia.ZamiaProject;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.zdb.ZDB;


/**
 * Transients during elaboration
 * 
 * @author Guenter Bartsch
 * 
 */

public class IGElaborationEnv {

	private ZamiaProject fZPrj;

	private IGInterpreterRuntimeEnv fEnv;

	public IGElaborationEnv(ZamiaProject aZPrj) {
		fZPrj = aZPrj;
	}

	public ZamiaProject getZamiaProject() {
		return fZPrj;
	}

	public ZDB getZDB() {
		return fZPrj.getZDB();
	}

	public void setInterpreterEnv(IGInterpreterRuntimeEnv aEnv) {
		fEnv = aEnv;
	}

	public IGInterpreterRuntimeEnv getInterpreterEnv() {
		return fEnv;
	}
}
