/* 
 * Copyright 2009, 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 27, 2009
 */
package org.zamia.vg;

import java.io.PrintStream;

import org.zamia.SourceLocation;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.util.HashSetArray;
import org.zamia.util.PathName;
import org.zamia.vhdl.ast.ASTObject;


/**
 * IGObject signal counterpart
 * 
 * @author Guenter Bartsch
 *
 */

public class VGSignal {

	private final VGBox fBox;

	private final String fTitle;

	private final OIDir fDir;

	private final long fDBID;

	private final HashSetArray<VGSignal> fConns, fExternalConns;

	private final SourceLocation fLocation;

	private final PathName fPath;

	public VGSignal(VGBox aBox, String aTitle, OIDir aDir, long aDBID, SourceLocation aLocation, PathName aPath) {

		fBox = aBox;
		fTitle = aTitle;
		fDir = aDir;
		fDBID = aDBID;
		fLocation = aLocation;
		fPath = aPath;

		fConns = new HashSetArray<VGSignal>();
		fExternalConns = new HashSetArray<VGSignal>();
	}

	public void connect(VGSignal aSignal) {
		fConns.add(aSignal);
	}

	public void connectExternal(VGSignal aSignal) {
		fExternalConns.add(aSignal);
	}
	public void dump(int aI, PrintStream aOut) {
		ASTObject.printlnIndented("SIGNAL " + fTitle, aI, aOut);
		int n = fConns.size();
		for (int i = 0; i < n; i++) {
			VGSignal conn = fConns.get(i);
			ASTObject.printlnIndented("CONN " + conn, aI + 2, aOut);
		}
	}

	@Override
	public String toString() {
		return fPath.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fPath == null) ? 0 : fPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VGSignal other = (VGSignal) obj;
		if (fPath == null) {
			if (other.fPath != null)
				return false;
		} else if (!fPath.equals(other.fPath))
			return false;
		return true;
	}

	public int getNumConns() {
		return fConns.size();
	}

	public int getNumExternalConns() {
		return fExternalConns.size();
	}

	public VGBox getBox() {
		return fBox;
	}

	public String getTitle() {
		return fTitle;
	}

	public OIDir getDir() {
		return fDir;
	}

	public long getDBID() {
		return fDBID;
	}

	public SourceLocation getLocation() {
		return fLocation;
	}

	public PathName getPath() {
		return fPath;
	}

	public VGSignal getConn(int aIdx) {
		return fConns.get(aIdx);
	}

	public VGSignal getExternalConn(int aIdx) {
		return fExternalConns.get(aIdx);
	}


}
