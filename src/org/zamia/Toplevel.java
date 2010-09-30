/* 
 * Copyright 2008,2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jun 17, 2008
 */
package org.zamia;

import java.io.Serializable;

import org.zamia.vhdl.ast.DUUID;


/**
 * Used in BuildPath representation
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class Toplevel implements Serializable {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private DUUID fDUUID;

	private SourceLocation fLocation;

	public Toplevel() {
	}

	public Toplevel(DUUID aDUUID, SourceLocation aLocation) {
		fDUUID = aDUUID;
		fLocation = aLocation;
	}

	public DUUID getDUUID() {
		return fDUUID;
	}

	public SourceLocation getLocation() {
		return fLocation;
	}

	@Override
	public String toString() {
		return "TOPLEVEL " + fDUUID;
	}

	@Override
	public boolean equals(Object aObj) {
		
		if (!(aObj instanceof Toplevel))
			return false;
		
		Toplevel tl2 = (Toplevel) aObj;
		
		return getDUUID().equals(tl2.getDUUID());
	}

	@Override
	public int hashCode() {
		return fDUUID.hashCode();
	}
}
