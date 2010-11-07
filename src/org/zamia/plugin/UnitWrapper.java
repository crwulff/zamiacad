/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 19, 2009
 */
package org.zamia.plugin;

import org.eclipse.core.resources.IProject;
import org.zamia.ZamiaProject;
import org.zamia.vhdl.ast.DMUID;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class UnitWrapper {

	private DMUID fDUUID;
	private ZamiaProject fZPrj;
	private IProject fPrj;

	public UnitWrapper(DMUID aDUUID, ZamiaProject aZPrj, IProject aPrj) {
		fDUUID = aDUUID;
		fZPrj = aZPrj;
		fPrj = aPrj;
	}
	
	@Override 
	public String toString() {
		return fDUUID.toString();
	}

	public DMUID getDUUID() {
		return fDUUID;
	}

	public ZamiaProject getZPrj() {
		return fZPrj;
	}

	public IProject getPrj() {
		return fPrj;
	}


}
