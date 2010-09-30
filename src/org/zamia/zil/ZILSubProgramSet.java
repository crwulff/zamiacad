/* 
 * Copyright 2007-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Sep 20, 2007
 */
package org.zamia.zil;

import java.util.ArrayList;

/**
 * Exactly what the name implies: a set (list) of sub programs
 * 
 * used to represent several, overloaded subprograms with the same id
 * 
 * @author Guenter Bartsch
 *
 */

public class ZILSubProgramSet extends ZILObject implements Cloneable {

	public ArrayList<ZILSubProgram> fSubs; 
	
	public ZILSubProgramSet (String aId, ZILType aType, ZILIContainer aContainer) {
		super (aId, aType, aContainer, null);
		fSubs = new ArrayList<ZILSubProgram>(1);
	}
	
	@Override
	public Object clone() {
		ZILSubProgramSet s2 = new ZILSubProgramSet(getId(), getType(), getContainer());
		int n = fSubs.size();
		for (int i = 0; i<n; i++) {
			s2.fSubs.add(fSubs.get(i));
		}
		return s2;
	}
	
	public ZILSubProgramSet merge (ZILSubProgramSet aSubProgramSet) {
		
		ZILSubProgramSet res = new ZILSubProgramSet(getId(), getType(), getContainer());
		
		int n = aSubProgramSet.getNumSubPrograms();
		for (int i = 0; i<n; i++) {
			res.add(aSubProgramSet.getSubProgram(i));
		}
		n = getNumSubPrograms();
		for (int i = 0; i<n; i++) {
			res.add(getSubProgram(i));
		}
		
		return res;
	}
	public void dump(int aIndent) {
		logger.debug("%s", toString());
	}
	
	@Override
	public String toString() {
		return "SubProgramSet (id="+getId()+", type="+getType()+")"; 
	}

	public void add(ZILSubProgram aObj) {
		fSubs.add(aObj);
	}

	public int getNumSubPrograms() {
		return fSubs.size();
	}

	public ZILSubProgram getSubProgram(int aIdx) {
		return fSubs.get(aIdx);
	}

}
