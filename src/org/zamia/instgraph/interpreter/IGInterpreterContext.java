/* 
 * Copyright 2008, 2010 by the authors indicated in the @author tags. 
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
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperationLiteral;
import org.zamia.instgraph.IGStaticValue;
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

	private HashMap<Long, IGObjectDriver> fDrivers = new HashMap<Long, IGObjectDriver>();

	private HashMap<Long, IGTypeStatic> fCachedTypes = new HashMap<Long, IGTypeStatic>();

	private HashMap<IGOperationLiteral, IGStaticValue> fCachedLiterals = new HashMap<IGOperationLiteral, IGStaticValue>();

	private static int counter = 0;

	private final int fCnt = counter++;

	public IGInterpreterContext() {

	}

	public IGObjectDriver createObject(IGInterpreterObject aObject, SourceLocation aLocation) throws ZamiaException {

		long dbid = aObject.getDBID();

		String id = aObject.getObject().getId();
		
		IGObject obj = aObject.getObject();
		
		IGObjectDriver driver = new IGObjectDriver(id, obj.getDirection(), obj.getCat(), null, aObject.getStaticType(), aLocation);

		fDrivers.put(dbid, driver);

		return driver;
	}

	public IGObjectDriver getObjectDriver(long aDBID) {
		return fDrivers.get(aDBID);
	}

	public IGStaticValue getObjectValue(long aDBID) {
		IGObjectDriver driver = fDrivers.get(aDBID);

		if (driver == null)
			return null;

		return driver.getValue();
	}

	public void setObjectValue(IGInterpreterObject aObject, IGStaticValue aValue, SourceLocation aLocation) throws ZamiaException {

		long dbid = aObject.getDBID();

		IGObjectDriver driver = fDrivers.get(dbid);

		if (driver == null) {
			driver = createObject(aObject, aLocation);
		}

		driver.setValue(aValue, aLocation);
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
		for (Long dbid : fDrivers.keySet()) {
			if (dbid != null) {
				logger.debug("InterpreterContext: Object %s => %s", dbid, fDrivers.get(dbid));
			}
		}
	}

	@Override
	public String toString() {
		return "IGInterpreterContext(cnt=" + fCnt + ")";
	}

}
