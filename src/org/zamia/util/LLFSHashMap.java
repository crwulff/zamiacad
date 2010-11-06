/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 31, 2010
 */
package org.zamia.util;

/**
 * This is a very special-purpose, fixed size, long->long hash map.
 * 
 * Not sure if there is any use for this data structure outside
 * EHM/ZDB.
 * 
 * 
 * @author Guenter Bartsch
 *
 */

public class LLFSHashMap {

	private long fValues[];

	private long fKeys[];

	private boolean fFree[];

	private int fSize, fAllocedSize, fNumEntries;

	public LLFSHashMap(int aSize) {

		fSize = aSize;
		fAllocedSize = fSize * 2;
		fNumEntries = 0;

		fValues = new long[fAllocedSize];
		fKeys = new long[fAllocedSize];
		fFree = new boolean[fAllocedSize];
		for (int i = 0; i < fAllocedSize; i++) {
			fFree[i] = true;
		}
	}

	//    static int indexFor(int h, int length) {
	//        return h & (length-1);
	//    }

	private int hash(long aKey) {

		long h = aKey;

		// defense

		h ^= (h >>> 20) ^ (h >>> 12);
		h = h ^ (h >>> 7) ^ (h >>> 4);

		return (int) (h % fAllocedSize);
	}

	public void put(long aKey, long aValue) {
		int x = hash(aKey);

		while (!fFree[x] && fKeys[x]!=aKey) {
			x = (x + 1) % fAllocedSize;
		}

		fValues[x] = aValue;
		fKeys[x] = aKey;
		fFree[x] = false;
		fNumEntries++;
	}

	public long get(long aKey) {

		int x = hash(aKey);

		while (!fFree[x] && fKeys[x] != aKey) {
			x = (x + 1) % fAllocedSize;
		}

		if (fFree[x]) {
			return -1;
		}

		return fValues[x];
	}

	public boolean hasKey(long aKey) {

		int x = hash(aKey);

		while (!fFree[x] && fKeys[x] != aKey) {
			x = (x + 1) % fAllocedSize;
		}

		return !fFree[x];
	}

	public int size() {
		return fNumEntries;
	}

	public int getAllocedSize() {
		return fAllocedSize;
	}

	public long getKey(int aIdx) {
		return fKeys[aIdx];
	}

	public long getValue(int aIdx) {
		return fValues[aIdx];
	}

	public boolean getFree(int aIdx) {
		return fFree[aIdx];
	}
}
