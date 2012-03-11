/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 15, 2009
 */
package org.zamia.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.zamia.ExceptionLogger;
import org.zamia.ZamiaLogger;


/**
 * Represents an immutable hierarchical path, e.g. a path to a signal in a
 * design
 * 
 * 
 * /module1/module2/instance1
 * 
 * @author Guenter Bartsch
 *
 *  Reverse engineering by Valentin Tihhomirov:
 *  In Guenter's example, we have module2 opened and 'instance' item is selected (cursor points to it).
 *  Adding hierarchy separator, . or /, we enter the instance1. Its entity declaration is opened. In this 
 *  case nothing is selected. This is signaled by 'null' in the leaf segment of the PathName.
 *  
 *   TODO: consider removing redundancies 
 *   	1) computable goString(): fPath holds the string representation of the path. Do we need it? IMO, it is used rarely and can be computed from segments at any time
 *   	2) sharing parent segments: Currently, PathName keeps all the segments up to the toplevel. This means that root segments are replicated many times. The path could consist only of a single reference to parent IGInstance.   
 */

@SuppressWarnings("serial")
public class PathName implements Serializable {

	protected final static ZamiaLogger logger = ZamiaLogger.getInstance();

	protected final static ExceptionLogger el = ExceptionLogger.getInstance();

	public final static char separator = '.';

	private String fSegments[];

	private String fPath;

	public PathName(List<String> aSegments) {
		initialize(aSegments);
	}

	private void initialize(List<String> aSegments) {
		StringBuilder buf = new StringBuilder(separator);
		int n = aSegments.size();
		fSegments = new String[n];
		
		for (int i = 0; i < n; i++) {
			String segment = aSegments.get(i);
			if (segment == null) {
				if (i != n - 1)
					logger.error("PathName: invalid null segment detected.");
			} else {
				segment = segment.toUpperCase();
				buf.append(segment);
			}

			if (i != n-1)
				buf.append(separator);
			
			fSegments[i] = segment;
		}

		fPath = buf.toString();
		
	}
	
	@Override
	public String toString() {
		return fPath;
	}

	public PathName(String aPath) {
		String[] segs = aPath.split("\\.");

		int n = segs.length;
		List<String> segments = new ArrayList(n);
		
		for (int i = 0; i < n; i++) {
			String segment = segs[i];
			if (segment != null && segment.length() > 0)
				segments.add(segment);
		}
		
		if (aPath.endsWith(".")) {
			segments.add(null);
		}
		
		initialize(segments);

	}

	public PathName clonePathName() {
		ArrayList<String> segments = new ArrayList<String>();

		int n = getNumSegments();

		for (int i = 0; i < n; i++) {
			segments.add(getSegment(i));
		}

		return new PathName(segments);
	}

	public PathName getParent() {
		ArrayList<String> segments = new ArrayList<String>();

		int n = getNumSegments() - 1;

		for (int i = 0; i < n; i++) {
			segments.add(getSegment(i));
		}

		return new PathName(segments);
	}

	public PathName append(String aSegment) {
		ArrayList<String> segments = new ArrayList<String>();

		int n = getNumSegments();

		for (int i = 0; i < n; i++) {
			segments.add(getSegment(i));
		}

		if (n > 0 && segments.get(n - 1) == null) {
			segments.set(n - 1, aSegment);
		} else {
			segments.add(aSegment);
		}

		return new PathName(segments);
	}

	public PathName append(PathName aPath) {
		ArrayList<String> segments = new ArrayList<String>();

		int n = getNumSegments();

		for (int i = 0; i < n; i++) {
			String segment = getSegment(i);
			if (segment != null) {
				segments.add(segment);
			}
		}

		n = aPath.getNumSegments();
		for (int i = 0; i < n; i++) {
			String segment = aPath.getSegment(i);
			segments.add(segment);
		}

		return new PathName(segments);
	}

	public String getSegment(int aIdx) {
		return fSegments[aIdx];
	}

	public int getNumSegments() {
		return fSegments.length;
	}

	public String getPath() {
		return fPath;
	}

	@Override
	public int hashCode() {
		return fPath.hashCode();
	}

	@Override
	public boolean equals(Object aObject) {
		if (!(aObject instanceof PathName))
			return false;

		PathName pn2 = (PathName) aObject;

		return fPath.equals(pn2.getPath());
	}

	public boolean endsInNull() {
		int l = fSegments.length;

		return l > 0 && fSegments[l - 1] == null;
	}

	public PathName getNullParent() {
		if (endsInNull()) {
			return getParent();
		}
		int n = getNumSegments();
		ArrayList<String> l = new ArrayList<String>(n);
		for (int i = 0; i < n; i++) {
			l.add(getSegment(i));
		}
		if (n > 0) {
			l.set(n - 1, null);
		}
		return new PathName(l);
	}

	/**
	 * Produce a new PathName that consists of the first aNumSegments of this one
	 * 
	 * @param aNumSegments
	 * @return
	 */
	
	public PathName getPrefix(int aNumSegments) {
		
		int n = getNumSegments();
		int nSegments = aNumSegments;
		if (nSegments > n) {
			nSegments = n;
		}
		
		if (nSegments == n) {
			return this;
		}
		
		ArrayList<String> l = new ArrayList<String>(nSegments);
		for (int i = 0; i < nSegments; i++) {
			l.add(getSegment(i));
		}
		
		return new PathName(l);
	}

}
