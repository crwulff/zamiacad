/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 21, 2010
 */
package org.zamia.zdb;

import java.util.HashMap;
import java.util.Iterator;

import org.zamia.util.ehm.ExtendibleHashMap;


/**
 * An index that lives in ZDB which has Maps as values
 * 
 * key -> ( mapkey -> mapvalue)
 * 
 * generics:
 * 
 * MK: mapkey
 * MV: mapvalue
 * 
 * 
 * @author Guenter Bartsch
 *
 */
public class ZDBMapIndex<MK, MV> {

	private final ZDB fZDB;

	private ExtendibleHashMap fEHM;

	public ZDBMapIndex(String aIndexId, ZDB aZDB) {
		fZDB = aZDB;
		fEHM = fZDB.getOrCreateEHM(aIndexId);
	}

	@SuppressWarnings("unchecked")
	public void put(long aKey, MK aMapKey, MV aMapValue) {

		long mapDBID = fEHM.get(aKey);

		if (mapDBID > 0) {
			HashMap<MK, MV> map = (HashMap<MK, MV>) fZDB.load(mapDBID);

			map.put(aMapKey, aMapValue);

			fZDB.update(mapDBID, map);

		} else {
			HashMap<MK, MV> map = new HashMap<MK, MV>();

			map.put(aMapKey, aMapValue);

			mapDBID = fZDB.store(map);

			fEHM.put(aKey, mapDBID);
		}
	}

	@SuppressWarnings("unchecked")
	public MV get(long aKey, MK aMapKey) {

		long mapDBID = fEHM.get(aKey);

		if (mapDBID <= 0) {
			return null;
		}

		HashMap<MK, MV> map = (HashMap<MK, MV>) fZDB.load(mapDBID);

		return map.get(aMapKey);
	}

	public void delete(long aKey) {
		fEHM.delete(aKey);
	}

	@SuppressWarnings("unchecked")
	public void delete(long aKey, MK aMapKey) {
		long mapDBID = fEHM.get(aKey);

		if (mapDBID <= 0) {
			return;
		}

		HashMap<MK, MV> map = (HashMap<MK, MV>) fZDB.load(mapDBID);

		map.remove(aMapKey);
	}

	@SuppressWarnings("unchecked")
	public Iterator<MK> getKeyIterator(long aKey) {
		long mapDBID = fEHM.get(aKey);

		if (mapDBID <= 0) {
			return null;
		}

		HashMap<MK, MV> map = (HashMap<MK, MV>) fZDB.load(mapDBID);
		return map.keySet().iterator();
	}

	@SuppressWarnings("unchecked")
	public Iterator<MV> getValueIterator(long aKey) {
		long mapDBID = fEHM.get(aKey);

		if (mapDBID <= 0) {
			return null;
		}

		HashMap<MK, MV> map = (HashMap<MK, MV>) fZDB.load(mapDBID);
		return map.values().iterator();
	}
}
