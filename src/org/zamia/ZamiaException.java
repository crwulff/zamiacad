/* 
 * Copyright 2007-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * 
 */

package org.zamia;

import java.io.File;

import org.zamia.vhdl.ast.VHDLNode;

/**
 * A unified Exception class for all source/intermediate-object related
 * problems.
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class ZamiaException extends Exception {

	public enum ExCat {
		FRONTEND, INTERMEDIATE, RTL, EXTERNAL
	};

	private SourceLocation fLocation;

	private ExCat fCat = ExCat.FRONTEND;

	private boolean fError;

	public ZamiaException(ExCat aCat, boolean aError, String aMessage, SourceLocation aLocation) {
		super(aMessage);
		fLocation = aLocation;
		if (fLocation != null && fLocation.fSF == null) {
			ZamiaLogger.getInstance().error(aMessage + " has no location! Correcting to a dummy.");
			fLocation.fSF = SourceLocation.dummyFile();
		}
		fCat = aCat;
		fError = aError;
	}

	public ZamiaException(String aMessage, SourceLocation aLocation) {
		this(ExCat.FRONTEND, true, aMessage, aLocation);
	}

	public ZamiaException(ExCat aCat, boolean aError, String aMessage, VHDLNode aSrc) {
		this(aCat, aError, aMessage, aSrc != null ? aSrc.getLocation() : null);
	}

	public ZamiaException(String aMessage, VHDLNode aSrc) {
		this(ExCat.FRONTEND, true, aMessage, aSrc);
	}

	public ZamiaException(String aMessage) {
		this(ExCat.FRONTEND, true, aMessage, (SourceLocation) null);
	}

	public SourceLocation getLocation() {
		return fLocation;
	}

	@Override
	public String toString() {
		if (fLocation != null)
			return fLocation.toStringAbsolutePath() + ": " + super.getMessage();
		return super.getMessage();
	}

	public ExCat getCat() {
		return fCat;
	}

	public boolean isError() {
		return fError;
	}

	@Override
	public int hashCode() {
		return getMessage().hashCode();
	}

	@Override
	public boolean equals(Object aObject) {

		if (this == aObject) {
			return true;
		}

		if (!(aObject instanceof ZamiaException))
			return false;

		ZamiaException ex2 = (ZamiaException) aObject;

		SourceLocation l2 = ex2.getLocation();
		if (fLocation != null && l2 == null)
			return false;
		if (fLocation == null && l2 != null)
			return false;
		if (fLocation != null && l2 != null && !fLocation.equals(l2))
			return false;

		ExCat cat2 = ex2.getCat();
		if (getCat() != cat2)
			return false;
		if (isError() != ex2.isError())
			return false;

		return getMessage().equals(ex2.getMessage());
	}

	public void setCat(ExCat aCat) {
		fCat = aCat;
	}

}
