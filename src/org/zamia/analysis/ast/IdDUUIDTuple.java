/*
 * Copyright 2008-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */
package org.zamia.analysis.ast;

import org.zamia.vhdl.ast.DUUID;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class IdDUUIDTuple {

	public String fId;

	public DUUID fDUUID;

	public IdDUUIDTuple(String aId, DUUID aDUUID) {
		fId = aId;
		fDUUID = aDUUID;
	}

	@Override
	public int hashCode() {
		String str = fDUUID + fId;
		return str.hashCode();
	}

	@Override
	public boolean equals(Object aObject) {

		if (!(aObject instanceof IdDUUIDTuple))
			return false;

		IdDUUIDTuple triple2 = (IdDUUIDTuple) aObject;

		DUUID duuid1 = fDUUID;
		DUUID duuid2 = triple2.fDUUID;

		return duuid1.equals(duuid2) && fId.equals(triple2.fId);
	}

	@Override
	public String toString() {
		return fDUUID + " : " + fId;
	}
}
