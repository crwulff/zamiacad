/*
 * Copyright 2007-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on May 11, 2007
 */

package org.zamia.rtl;

import org.zamia.vhdl.ast.VHDLNode;

/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public abstract class RTLPortModule extends RTLModule {

	protected RTLPort internalPort, externalPort;

	public RTLPortModule (RTLGraph parent_, RTLPort p, VHDLNode src_) {
		super(parent_, p.getId(), src_);
	}
	
	public RTLPort getInternalPort() {
		return internalPort;
	}
	public RTLPort getExternalPort() {
		return externalPort;
	}
	
	@Override
	public boolean equals(RTLModule module2_) {
		
		return false;
	}

}
