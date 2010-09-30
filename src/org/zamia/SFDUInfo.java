/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 17, 2010
 */
package org.zamia;

import java.io.Serializable;

import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.DUUID;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class SFDUInfo implements Serializable {

	private HashSetArray<DUUID> fDUUIDs;

	private long fTimestamp = System.currentTimeMillis();

	public SFDUInfo() {
		fDUUIDs = new HashSetArray<DUUID>();
	}

	public SFDUInfo(HashSetArray<DUUID> aDUUIDs) {
		fDUUIDs = aDUUIDs;
	}

	public long getTimestamp() {
		return fTimestamp;
	}

	public int getNumDUUIDs() {
		return fDUUIDs.size();
	}

	public DUUID getDUUID(int aIdx) {
		return fDUUIDs.get(aIdx);
	}

	public void add(DUUID aDUUID) {
		fDUUIDs.add(aDUUID);
	}

	public void touch() {
		fTimestamp = System.currentTimeMillis();
	}
}
