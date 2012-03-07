/* 
 * Copyright 2008,2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Sep 7, 2008
 */
package org.zamia.analysis;

import java.io.PrintStream;

import org.zamia.SourceLocation;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.VHDLNode;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class ReferenceSearchResult {

	private String fTitle, fPrefix;

	private ReferenceSearchResult fParent;

	private HashSetArray<ReferenceSearchResult> fChildren = new HashSetArray<ReferenceSearchResult>();

	private SourceLocation fLocation;

	private int fLength;

	private ZamiaProject fZPrj;
	
	protected ToplevelPath fPath;

	private OIDir fDirection = OIDir.NONE;

	public ReferenceSearchResult(String aTitle, SourceLocation aLocation, int aLength) {
		fTitle = aTitle;
		fLocation = aLocation;
		fLength = aLength;
	}

	public void setParent(ReferenceSearchResult aParent) {
		fParent = aParent;
	}

	public void add(ReferenceSearchResult aRSS) {
		fChildren.add(aRSS);
		aRSS.setParent(this);
	}

	public String getTitle() {
		return fTitle;
	}

	public void setTitle(String aTitle) {
		fTitle = aTitle;
	}

	public SourceLocation getLocation() {
		return fLocation;
	}

	public int getLength() {
		return fLength;
	}
	
	public void setLength(int aLength) {
		fLength = aLength;
	}

	public int getNumChildren() {
		return fChildren.size();
	}

	public ReferenceSearchResult getChild(int idx_) {
		return fChildren.get(idx_);
	}

	@Override
	public String toString() {

		if (fTitle == null)
			return "";
		if (fPrefix != null)
			return fPrefix + fTitle + " (" + fLocation + ")";
		return fTitle + (fLocation != null ? " (" + fLocation + ")" : "");
	}

	public ReferenceSearchResult getParent() {
		return fParent;
	}

	public void dump(int aIndent, PrintStream aOut) {

		VHDLNode.printlnIndented("ReferenceSearchResult: " + fTitle, aIndent, aOut);

		int n = getNumChildren();
		for (int i = 0; i < n; i++) {
			ReferenceSearchResult child = getChild(i);
			child.dump(aIndent + 1, aOut);
		}
	}

	public int countRefs() {
		
		int n = getNumChildren();
		int count = 0;
		
		for (int i = 0; i<n; i++) {
			ReferenceSearchResult child = getChild(i);
			
			count += child.countRefs();
		}
		
		return count;
	}

	public ToplevelPath getPath() {
		return fPath;
	}
	
	public void setPrefix(String aPrefix) {
		fPrefix = aPrefix;
	}
	
	public String getPrefix() {
		return fPrefix;
	}

	public void setDirection(OIDir aDirection) {
		fDirection  = aDirection;
	}
	
	public OIDir getDirection () {
		return fDirection;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fDirection == null) ? 0 : fDirection.hashCode());
		result = prime * result + fLength;
		result = prime * result + ((fLocation == null) ? 0 : fLocation.hashCode());
		result = prime * result + ((fPath == null) ? 0 : fPath.hashCode());
		result = prime * result + ((fPrefix == null) ? 0 : fPrefix.hashCode());
		result = prime * result + ((fTitle == null) ? 0 : fTitle.hashCode());
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
		ReferenceSearchResult other = (ReferenceSearchResult) obj;
		if (fDirection == null) {
			if (other.fDirection != null)
				return false;
		} else if (!fDirection.equals(other.fDirection))
			return false;
		if (fLength != other.fLength)
			return false;
		if (fLocation == null) {
			if (other.fLocation != null)
				return false;
		} else if (!fLocation.equals(other.fLocation))
			return false;
		if (fPath == null) {
			if (other.fPath != null)
				return false;
		} else if (!fPath.equals(other.fPath))
			return false;
		if (fPrefix == null) {
			if (other.fPrefix != null)
				return false;
		} else if (!fPrefix.equals(other.fPrefix))
			return false;
		if (fTitle == null) {
			if (other.fTitle != null)
				return false;
		} else if (!fTitle.equals(other.fTitle))
			return false;
		return true;
	}

}
