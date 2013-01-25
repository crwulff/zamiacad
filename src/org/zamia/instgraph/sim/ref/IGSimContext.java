/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Aug 15, 2009
 */
package org.zamia.instgraph.sim.ref;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.interpreter.IGInterpreterContext;
import org.zamia.instgraph.interpreter.IGObjectDriver;
import org.zamia.util.PathName;

/**
 * @author Guenter Bartsch
 */

@SuppressWarnings("serial")
public class IGSimContext extends IGInterpreterContext {

	private PathName fPath;

	public IGSimContext(PathName aPath) {
		fPath = aPath;
	}

	@Override
	protected IGObjectDriver createDriver(String aId, IGObject.OIDir aDir, IGObject.IGObjectCat aCat, IGTypeStatic aType, boolean aIsLightweight, SourceLocation aLocation) throws ZamiaException {

		switch (aCat) {
			case SIGNAL:
				return new IGSignalDriver(aId, aDir, aCat, aType, aIsLightweight, aLocation);
			default:
				return super.createDriver(aId, aDir, aCat, aType, aIsLightweight, aLocation);
		}
	}

	@Override
	public IGStaticValue getObjectValue(long aDBID) throws ZamiaException {
		// For signals -- get delta
		// For others -- current value
		IGObjectDriver driver = getObjectDriver(aDBID);
		if (driver == null) {
			return null;
		}

		if (driver instanceof IGSignalDriver) {
			return ((IGSignalDriver) driver).getNextValue();
		} else {
			return driver.getValue(null);
		}
	}


	public PathName getPath() {
		return fPath;
	}

	@Override
	public String toString() {
		return super.toString() + "<" + getPath() + ">";
	}
}
