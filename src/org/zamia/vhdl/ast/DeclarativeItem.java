/* 
 * Copyright 2008,2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Aug 11, 2008
 */
package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.ZamiaException;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGContainerItem;
import org.zamia.instgraph.IGElaborationEnv;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public abstract class DeclarativeItem extends VHDLNode {

	protected String id;
	
	public DeclarativeItem(String aId, VHDLNode aParent, long aLocation) {
		super (aParent, aLocation);
		id = aId;
	}
	
	public String getId() {
		return id;
	}

	public abstract IGContainerItem computeIG(ArrayList<IGContainerItem> aSpecItems, IGContainer aContainer, IGElaborationEnv aCache) throws ZamiaException;
}
