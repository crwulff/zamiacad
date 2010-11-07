/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Oct 17, 2010
 */
package org.zamia.verilog.pre;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class VPMacro {

	private final String fId, fBody;

	public VPMacro(String aId, String aBody) {
		fId = aId;
		fBody = aBody;
	}

	public String getId() {
		return fId;
	}

	public String getBody() {
		return fBody;
	}

}
