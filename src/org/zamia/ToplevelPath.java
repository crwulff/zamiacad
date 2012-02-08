/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 10, 2009
 */
package org.zamia;

import java.io.Serializable;

import org.zamia.util.PathName;
import org.zamia.vhdl.ast.DMUID;


/**
 * A toplevel + PathName => an absolute path in a design
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class ToplevelPath implements Serializable {

	public final static char separator = ':';

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private Toplevel fToplevel;

	private PathName fPath;

	public ToplevelPath(Toplevel aToplevel, PathName aPath) {
		fToplevel = aToplevel;
		fPath = aPath;
	}

	/**
	 * Parses the string
	 * 
	 * expected format: duuid ":" path
	 * 
	 * e.g. WORK.FOO(BAR):A.B.S
	 * 
	 * @param aString
	 */
	public ToplevelPath(String aString) throws ZamiaException {

		String[] parts = aString.split("" + separator);

		if (parts.length < 1) {
			throw new ZamiaException("ToplevelPath: Couldn't parse: " + aString + " #parts: " + parts.length);
		}

		DMUID duuid = DMUID.parse(parts[0]);

		fToplevel = new Toplevel(duuid, null);
		if (parts.length > 1) {
			fPath = new PathName(parts[1]);
		} else {
			fPath = new PathName("");
		}

	}

	public Toplevel getToplevel() {
		return fToplevel;
	}

	public PathName getPath() {
		return fPath;
	}

	@Override
	public String toString() {
		return fToplevel.getDUUID().toString() + separator + fPath.toString();
	}

	@Override
	public boolean equals(Object aObj) {

		if (!(aObj instanceof ToplevelPath))
			return false;

		ToplevelPath tlp2 = (ToplevelPath) aObj;

		Toplevel tl1 = getToplevel();
		Toplevel tl2 = tlp2.getToplevel();
		if (!tl1.equals(tl2))
			return false;

		PathName p1 = getPath();
		PathName p2 = tlp2.getPath();

		return p1.equals(p2);
	}

	@Override
	public int hashCode() {
		return fPath.hashCode() << 16 | fToplevel.hashCode();
	}

	public ToplevelPath getParent() {
		return new ToplevelPath(fToplevel, fPath.getParent());
	}

	public ToplevelPath append(String aSegment) {
		return new ToplevelPath(fToplevel, fPath.append(aSegment));
	}

	public ToplevelPath append(PathName aPrefix) {
		
		ToplevelPath res = this;
		int n = aPrefix.getNumSegments();
		for (int i = 0; i<n; i++) {
			res = res.append(aPrefix.getSegment(i));
		}
		
		return res;
	}

	public boolean endsInNull() {
		return fPath.endsInNull();
	}

	public ToplevelPath getNullParent() {
		return new ToplevelPath(fToplevel, fPath.getNullParent());
	}

	public int getNumSegments() {
		return fPath.getNumSegments();
	}

	public ToplevelPath descend() {
		return append((String) null);
	}
}
