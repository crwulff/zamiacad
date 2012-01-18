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
import java.util.Iterator;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.DMUID;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class SFDMInfo implements Serializable {

	private HashSetArray<DMUID> fDMUIDs;

	private long fTimestamp = System.currentTimeMillis();

	public SFDMInfo() {
		fDMUIDs = new HashSetArray<DMUID>();
	}

	public SFDMInfo(HashSetArray<DMUID> aDMUIDs) {
		fDMUIDs = aDMUIDs;
	}

	public long getTimestamp() {
		return fTimestamp;
	}

	public int getNumDMUIDs() {
		return fDMUIDs.size();
	}

	public DMUID getDMUID(int aIdx) {
		return fDMUIDs.get(aIdx);
	}

	public void add(DMUID aDMUID) {
		fDMUIDs.add(aDMUID);
	}

	//inspired by VHDL package import
	public Iterator<DMUID> iterator() {
		return fDMUIDs.iterator();
	}
	
	public void touch() {
		fTimestamp = System.currentTimeMillis();
	}
}
