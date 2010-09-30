/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by guenter on Feb 26, 2008
 */
package org.zamia;

import java.io.Serializable;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class BuildPathEntry implements Serializable {

	public String fPrefix;

	public String fLibId;

	public boolean fExtern;

	public int fPriority;

	public boolean fReadonly;

	public SourceLocation fLocation;

	public boolean fBottomUp;

	public boolean fIsDirectory;

	public boolean fRecursive;

	public BuildPathEntry(String aPrefix, String aLibId, boolean aExtern, int aPriority, boolean aReadOnly, boolean aBottomUp, boolean aIsDirectory, boolean aRecursive, SourceLocation aLocation) {
		fPrefix = aPrefix;
		fExtern = aExtern;
		fLibId = aLibId;
		fPriority = aPriority;
		fReadonly = aReadOnly;
		fLocation = aLocation;
		fBottomUp = aBottomUp;
		fIsDirectory = aIsDirectory;
		fRecursive = aRecursive;
	}

	@Override
	public boolean equals(Object aObject) {
		if (aObject == this) {
			return true;
		}

		if (!(aObject instanceof BuildPathEntry)) {
			return false;
		}

		BuildPathEntry bpe = (BuildPathEntry) aObject;

		if (!fPrefix.equals(bpe.fPrefix)) {
			return false;
		}
		if (!fLibId.equals(bpe.fLibId)) {
			return false;
		}
		// priority should be sufficient
		//		if (!fLocation.equals(bpe.fLocation)) {
		//			return false;
		//		}

		return fExtern == bpe.fExtern && fPriority == bpe.fPriority && fReadonly == bpe.fReadonly && fBottomUp == bpe.fBottomUp;
	}

	@Override
	public int hashCode() {
		String str = toString();
		return str.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		if (fExtern) {
			buf.append("extern ");
		} else {
			buf.append("local ");
		}

		if (fReadonly) {
			buf.append("readonly ");
		}

		buf.append(fLibId);
		buf.append(" \"" + fPrefix + "\"");
		return buf.toString();
	}
}
