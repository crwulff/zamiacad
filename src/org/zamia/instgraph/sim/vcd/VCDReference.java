/* 
 * Copyright 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 20, 2011
 */
package org.zamia.instgraph.sim.vcd;

import org.zamia.util.PathName;

/**
 * signal path name + index information (if any)
 * @author Guenter Bartsch
 *
 */

public class VCDReference {

	private PathName fPathName;

	private boolean fIdx1Valid = false;

	private int fIdx1;

	private boolean fIdx2Valid = false;

	private int fIdx2;

	public VCDReference(PathName aPathName) {
		setPathName(aPathName);
	}

	public void setIdx1(int aIdx1) {
		fIdx1 = aIdx1;
		fIdx1Valid = true;
	}

	public void setIdx2(int aIdx2) {
		fIdx2 = aIdx2;
		fIdx2Valid = true;
	}

	public void setPathName(PathName pathName) {
		fPathName = pathName;
	}

	public PathName getPathName() {
		return fPathName;
	}

	public boolean isIdx1Valid() {
		return fIdx1Valid;
	}

	public boolean isIdx2Valid() {
		return fIdx2Valid;
	}

	public int getIdx1() {
		return fIdx1;
	}

	public int getIdx2() {
		return fIdx2;
	}
}
