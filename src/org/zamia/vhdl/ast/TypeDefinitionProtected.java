/*
 * Copyright 2004-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.vhdl.ast;

import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGType;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class TypeDefinitionProtected extends TypeDefinition {

	public TypeDefinitionProtected(VHDLNode parent_, long location_) {
		super(parent_, location_);
	}

	@Override
	public int getNumChildren() {
		return 0;
	}

	@Override
	public VHDLNode getChild(int idx_) {
		return null;
	}

	@Override
	public IGType computeIG(IGContainer aContainer, IGElaborationEnv aCache) {
		reportError("Sorry, not implemented yet.");
		return IGType.createErrorType(aCache.getZDB());
	}
}