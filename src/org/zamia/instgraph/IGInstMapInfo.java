/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 21, 2010
 */
package org.zamia.instgraph;

import java.io.Serializable;
import java.util.HashMap;

import org.zamia.util.HashSetArray;


/**
 * Quick info about an instantiation
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class IGInstMapInfo implements Serializable {

	private final long fDBID;

	private final String fLabel;

	private final HashMap<Long, HashSetArray<IGMapInfo>> fMappingInfo;

	public IGInstMapInfo(long aChildDBID, String aLabel) {

		fDBID = aChildDBID;
		fLabel = aLabel;

		fMappingInfo = new HashMap<Long, HashSetArray<IGMapInfo>>();
	}

	public String getLabel() {
		return fLabel;
	}

	public long getDBID() {
		return fDBID;
	}

	public void addMapInfo(long aFormalDBID, IGMapInfo aMapInfo) {

		HashSetArray<IGMapInfo> hsa = fMappingInfo.get(aFormalDBID);

		if (hsa == null) {
			hsa = new HashSetArray<IGMapInfo>();
			fMappingInfo.put(aFormalDBID, hsa);
		}

		hsa.add(aMapInfo);
	}
}
