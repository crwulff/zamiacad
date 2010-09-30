/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 21, 2010
 */
package org.zamia.zdb;

import java.util.ArrayList;
import java.util.List;

import org.zamia.util.ehm.ExtendibleHashMap;


/**
 * An index that lives in ZDB which has Lists as values
 * 
 * key -> ( value, value, value, ...)
 * 
 * @author Guenter Bartsch
 *
 */
public class ZDBListIndex<V> {

	private final ZDB fZDB;

	private ExtendibleHashMap fEHM;

	public ZDBListIndex(String aIndexId, ZDB aZDB) {
		fZDB = aZDB;
		fEHM = fZDB.getOrCreateEHM(aIndexId);
	}

	@SuppressWarnings("unchecked")
	public void add(long aKey, V aValue) {

		long listDBID = fEHM.get(aKey);

		if (listDBID > 0) {
			ArrayList<V> list = (ArrayList<V>) fZDB.load(listDBID);

			list.add(aValue);

			fZDB.update(listDBID, list);

		} else {
			ArrayList<V> list = new ArrayList<V>();

			list.add(aValue);

			listDBID = fZDB.store(list);

			fEHM.put(aKey, listDBID);
		}
	}

	@SuppressWarnings("unchecked")
	public List<V> get(long aKey) {

		long listDBID = fEHM.get(aKey);

		if (listDBID <= 0) {
			return null;
		}

		ArrayList<V> list = (ArrayList<V>) fZDB.load(listDBID);

		return list;
	}

	public void delete(long aKey) {
		fEHM.delete(aKey);
	}

}
