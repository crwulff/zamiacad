/* 
 * Copyright 2009, 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 27, 2009
 */
package org.zamia.analysis.ig;

import java.io.PrintStream;

import org.zamia.SourceLocation;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.util.HashSetArray;
import org.zamia.util.PathName;
import org.zamia.vhdl.ast.VHDLNode;


/**
 * IGObject signal counterpart
 * 
 * @author Guenter Bartsch
 *
 */

public class IGRSSignal {

	private final IGRSBox fBox;

	private final String fTitle;

	private final OIDir fDir;

	private final long fDBID;

	private final HashSetArray<IGRSSignal> fConns, fExternalConns;

	private final SourceLocation fLocation;

	private final PathName fPath;

	public IGRSSignal(IGRSBox aBox, String aTitle, OIDir aDir, long aDBID, SourceLocation aLocation, PathName aPath) {

		fBox = aBox;
		fTitle = aTitle;
		fDir = aDir;
		fDBID = aDBID;
		fLocation = aLocation;
		fPath = aPath;

		fConns = new HashSetArray<IGRSSignal>();
		fExternalConns = new HashSetArray<IGRSSignal>();
	}

	public void connect(IGRSSignal aSignal) {
		fConns.add(aSignal);
	}

	public void connectExternal(IGRSSignal aSignal) {
		fExternalConns.add(aSignal);
	}
	public void dump(int aI, PrintStream aOut) {
		VHDLNode.printlnIndented("SIGNAL " + fTitle, aI, aOut);
		int n = fConns.size();
		for (int i = 0; i < n; i++) {
			IGRSSignal conn = fConns.get(i);
			VHDLNode.printlnIndented("CONN " + conn, aI + 2, aOut);
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
		IGRSSignal other = (IGRSSignal) obj;
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

	public IGRSBox getBox() {
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

	public IGRSSignal getConn(int aIdx) {
		return fConns.get(aIdx);
	}

	public IGRSSignal getExternalConn(int aIdx) {
		return fExternalConns.get(aIdx);
	}


}
