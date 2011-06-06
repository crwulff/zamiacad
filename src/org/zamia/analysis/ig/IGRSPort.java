/* 
 * Copyright 2010,2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 7, 2010
 */
package org.zamia.analysis.ig;

import org.zamia.instgraph.IGObject.OIDir;

/**
 * 
 * @author Guenter Bartsch
 *
 */
public class IGRSPort {

	private IGRSSignal fSignal;

	private IGRSNode fNode;

	private OIDir fDir;

	private String fId;

	public IGRSPort(IGRSNode aNode, String aId, OIDir aDir) {
		fNode = aNode;
		fDir = aDir;
		fSignal = null;
		fId = aId;
	}

	public void setSignal(IGRSSignal aSignal) {

		if (fSignal == aSignal)
			return;

		if (fSignal != null)
			fSignal.removePortConn(this);

		if (aSignal != null) {
			//System.out.println ("adding myself to signal "+s_+"'s conn
			// hashmap");
			aSignal.addPortConn(this);
		}
		fSignal = aSignal;
	}

	public String getId() {
		return fId;
	}

	public IGRSNode getNode() {
		return fNode;
	}

	public IGRSSignal getSignal() {
		return fSignal;
	}

	public OIDir getDirection() {
		return fDir;
	}

	public String toString() {
		return "IGRSPort(id=" + fId + ", node=" + fNode + ")";
	}

}
