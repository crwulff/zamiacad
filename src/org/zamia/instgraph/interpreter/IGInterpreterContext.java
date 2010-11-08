/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 25, 2008
 */
package org.zamia.instgraph.interpreter;

import java.io.Serializable;
import java.util.HashMap;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.instgraph.IGOperationLiteral;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGStaticValueBuilder;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGTypeStatic;


/*
*
 * 
 * @author Guenter Bartsch
 *
 */
@SuppressWarnings("serial")
public class IGInterpreterContext implements Serializable {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private HashMap<Long, IGStaticValue> fObjects = new HashMap<Long, IGStaticValue>();

	private HashMap<Long, IGTypeStatic> fCachedTypes = new HashMap<Long, IGTypeStatic>();

	private HashMap<IGOperationLiteral, IGStaticValue> fCachedLiterals = new HashMap<IGOperationLiteral, IGStaticValue>();

	public IGInterpreterContext() {

	}

	public IGStaticValue getObjectValue(long aDBID) {
		return fObjects.get(aDBID);
	}

	public void setObjectValue(IGInterpreterObject aObject, IGStaticValue aValue, SourceLocation aLocation) throws ZamiaException {

		IGStaticValue value = aValue;

		// so we have to do an index shift?
		IGTypeStatic t = value.getStaticType();
		if (t.isArray()) {

			if (t.isUnconstrained()) {
				logger.warn("IGInterpreterRuntime: Warning: setting %s to an unconstrained array value: %s", aObject, aValue);
			} else {

				IGTypeStatic oT = aObject.getStaticType();
				if (!oT.isUnconstrained() && oT.getCat() != IGType.TypeCat.FILE  && oT.getCat() != IGType.TypeCat.ACCESS) {

					IGTypeStatic oIdxT = oT.getStaticIndexType(null);
					IGTypeStatic idxT = t.getStaticIndexType(null);

					int oC = (int) oIdxT.computeCardinality(null);
					int c = (int) idxT.computeCardinality(null);
					if (oC != c) {
						throw new ZamiaException("Interpreter: error: tried to assign object " + aObject + " from incompatible array value " + aValue + ": " + oT + " vs " + t,
								aLocation);
					}

					int l = (int) idxT.getStaticLeft(aLocation).getLong();
					int r = (int) idxT.getStaticRight(aLocation).getLong();
					boolean a = idxT.getStaticAscending().isTrue();
					int off = a ? l : r;

					int oL = (int) oIdxT.getStaticLeft(aLocation).getLong();
					int oR = (int) oIdxT.getStaticRight(aLocation).getLong();
					boolean oA = oIdxT.getStaticAscending().isTrue();
					int oOff = oA ? oL : oR;

					if (l != oL || r != oR || a != oA) {
						// yes, time to shift the index

						IGStaticValueBuilder builder = new IGStaticValueBuilder(oT, null, aLocation);

						for (int i = 0; i < c; i++) {

							IGStaticValue v = value.getValue(i + off, aLocation);

							builder.set(i + oOff, v, aLocation);
						}

						value = builder.buildConstant();
					}
				}
			}
		}

		fObjects.put(aObject.getDBID(), value);
	}

	public void putCachedLiteralActualConstant(IGOperationLiteral aLiteral, IGStaticValue aConstant) {
		fCachedLiterals.put(aLiteral, aConstant);
	}

	public IGStaticValue getCachedLiteralActualConstant(IGOperationLiteral aLiteral) {
		return fCachedLiterals.get(aLiteral);
	}

	public void putCachedType(long aDBID, IGTypeStatic aType) {
		fCachedTypes.put(aDBID, aType);
	}

	public IGTypeStatic getCachedType(long aDBID) {
		return fCachedTypes.get(aDBID);
	}

	public void dump() {
		for (Long dbid : fObjects.keySet()) {
			if (dbid != null) {
				logger.debug("InterpreterContext: Object %s => %s", dbid, fObjects.get(dbid));
			}
		}
	}
}
