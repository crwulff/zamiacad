/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jul 16, 2005
 */

package org.zamia.vhdl.ast;

import org.zamia.ZamiaException;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGStructure;



/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public abstract class ConcurrentStatement extends VHDLNode {

	protected boolean fPostponed = false; 
	protected String fLabel;

	public ConcurrentStatement(VHDLNode aParent, long aLocation) {
		super (aParent, aLocation);
	}
	
	public ConcurrentStatement(String aLabel, VHDLNode aParent, long aLocation) {
		super (aParent, aLocation);
		fLabel = aLabel;
	}
	
	public abstract void computeIG(DMUID aDUUID, IGContainer aContainer, IGStructure aStructure, IGElaborationEnv aEE) throws ZamiaException;

	public void setPostponed(boolean aPostponed) {
		fPostponed = aPostponed;
	}

	public void setLabel(String aLabel) {
		fLabel = aLabel;
	}
	
	public String getLabel() {
		return fLabel;
	}

}
