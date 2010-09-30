/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 27, 2010
 */
package org.zamia.util.ehm;

import org.zamia.ExceptionLogger;
import org.zamia.ZamiaLogger;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class EHMNode {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private long fId;

	private EHMNode fParent;

	private EHMNode fZeroNode, fOneNode;

	public EHMNode(long aId, EHMNode aParent) {
		fId = aId;
		fParent = aParent;
		fZeroNode = null;
		fOneNode = null;
	}

	public long getId() {
		return fId;
	}

	public EHMNode getZeroNode() {
		return fZeroNode;
	}

	public void setZeroNode(EHMNode aZeroNode) {
		fZeroNode = aZeroNode;
	}

	public EHMNode getOneNode() {
		return fOneNode;
	}

	public void setOneNode(EHMNode aOneNode) {
		fOneNode = aOneNode;
	}

	public EHMNode getParent() {
		return fParent;
	}

	public void setId(long aId) {
		fId = aId;
	}

}
