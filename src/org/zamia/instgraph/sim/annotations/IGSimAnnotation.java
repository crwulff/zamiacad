/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Oct 13, 2009
 */
package org.zamia.instgraph.sim.annotations;

import org.zamia.SourceLocation;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.util.PathName;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSimAnnotation {

	private final SourceLocation fLocation;

	private final PathName fPath;

	private final IGStaticValue fValue;

	public IGSimAnnotation(SourceLocation aLocation, IGStaticValue aValue, PathName aPath) {
		fLocation = aLocation;
		fValue = aValue;
		fPath = aPath;
	}

	public IGStaticValue getValue() {
		return fValue;
	}

	public SourceLocation getLocation() {
		return fLocation;
	}

	@Override
	public boolean equals(Object aObj) {

		if (aObj == this) {
			return true;
		}

		if (!(aObj instanceof IGSimAnnotation)) {
			return false;
		}

		IGSimAnnotation ann2 = (IGSimAnnotation) aObj;

		return ann2.getValue().equals(getValue()) && ann2.getLocation().equals(getLocation());
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return fLocation + " : " + fValue;
	}

	public PathName getPath() {
		return fPath;
	}
}
