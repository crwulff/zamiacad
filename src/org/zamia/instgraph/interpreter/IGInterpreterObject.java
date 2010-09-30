/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Aug 20, 2009
 */
package org.zamia.instgraph.interpreter;

import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGTypeStatic;

/**
 * Wrapper for IGObject + static type
 * 
 * @author Guenter Bartsch
 * 
 */

public class IGInterpreterObject {

	private final IGObject fObject;

	private final IGTypeStatic fStaticType;

	public IGInterpreterObject(IGObject aObject, IGTypeStatic aStaticType) {

		fObject = aObject;
		fStaticType = aStaticType;
	}

	public IGObject getObject() {
		return fObject;
	}

	public IGTypeStatic getStaticType() {
		return fStaticType;
	}

	public long getDBID() {
		return fObject.getDBID();
	}
}
