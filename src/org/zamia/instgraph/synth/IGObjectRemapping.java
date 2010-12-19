/*
 * Copyright 2007-2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Mar 3, 2007
 */

package org.zamia.instgraph.synth;

import java.util.HashMap;

import org.zamia.instgraph.IGObject;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGObjectRemapping {

	private HashMap<Long, Long> fVarMap;

	private IGObjectRemapping fParent;

	private final IGSynth fSynth;

	private final ZDB fZDB;

	public IGObjectRemapping(IGSynth aSynth) {
		fSynth = aSynth;
		fZDB = fSynth.getZDB();
		fParent = null;
		fVarMap = new HashMap<Long, Long>();
	}

	public IGObjectRemapping(IGObjectRemapping aParent) {
		fSynth = aParent.fSynth;
		fZDB = fSynth.getZDB();
		fParent = aParent;
		fVarMap = new HashMap<Long, Long>();
	}

	public IGObject get(IGObject aOldObj) {
		Long res = fVarMap.get(aOldObj.store());
		if (res != null) {
			IGObject v = (IGObject) fZDB.load(res);
			return v;
		}
		if (fParent != null)
			return fParent.get(aOldObj);
		return aOldObj;
	}

	public IGObject remap(IGObject aOldObj) {

		IGObject newObj = new IGObject(aOldObj.getDirection(), aOldObj.getInitialValue(), aOldObj.getCat(), aOldObj.getType(), aOldObj.getId()
				+ "(remapped)", aOldObj.computeSourceLocation(), fZDB);

		fVarMap.put(aOldObj.store(), newObj.store());

		return newObj;

	}

}
