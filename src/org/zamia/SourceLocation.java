/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

// mainly used for error reporting
package org.zamia;

import java.io.File;
import java.io.Serializable;

/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class SourceLocation implements Serializable, Comparable<SourceLocation> {

	public SourceFile fSF;

	public int fLine, fCol;

	public SourceLocation(SourceFile aSF, int aLine, int aCol) {
		fSF = aSF;
		fLine = aLine;
		fCol = aCol;
	}

	public SourceLocation(SourceFile aSF, long aLocation) {
		fSF = aSF;
		fLine = (int) aLocation & 0xffffffff;
		fCol = (int) (aLocation >> 32);
	}

	static public SourceFile dummyFile() {return new SourceFile(new File("unknown"));}
	
	public SourceLocation() {
		fSF = dummyFile();
		fLine = 0;
		fCol = 0;
	}

	public String toString() {
		if (fSF != null)
			return fSF + ":" + fLine + "," + fCol;
		return "unknown source";
	}

	public String toStringAbsolutePath() {
		if (fSF != null)
			return fSF.getAbsolutePath() + ":" + fLine + "," + fCol;
		return "unknown source";
	}

	public File getDir() {
		return fSF.getFile().getParentFile();
	}

	@Override
	public int hashCode() {
		String str = toStringAbsolutePath();
		return str.hashCode();
	}

	@Override
	public boolean equals(Object aObj) {
		if (this == aObj) {
			return true;
		}
		if (aObj instanceof SourceLocation) {
			SourceLocation anotherSourceLocation = (SourceLocation) aObj;

			return fSF.equals(anotherSourceLocation.fSF) && fCol == anotherSourceLocation.fCol && fLine == anotherSourceLocation.fLine;
			
		}
		return false;
	}

	@Override
	public int compareTo(SourceLocation o) {
		int res;
		if (fSF != null) {
			res = fSF.compareTo(o.fSF);
			if (res != 0) {
				return res;
			}
		}
		res = Integer.compare(fLine, o.fLine);
		if (res != 0) {
			return res;
		}
		return Integer.compare(fCol, o.fCol);
	}
}
