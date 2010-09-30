/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.util.ehm;

import org.zamia.util.LLFSHashMap;

/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class EHMCacheEntry {

	private LLFSHashMap fPage;

	private EHMCacheEntry fPrev, fNext;

	private long fId;

	private boolean fDirty;

	public EHMCacheEntry(long aId, LLFSHashMap aPage, boolean aDirty) {
		fId = aId;
		fPage = aPage;
		fDirty = aDirty;
	}

	public long getId() {
		return fId;
	}

	public EHMCacheEntry getPrev() {
		return fPrev;
	}

	public void setPrev(EHMCacheEntry aPrev) {
		fPrev = aPrev;
	}

	public EHMCacheEntry getNext() {
		return fNext;
	}

	public void setNext(EHMCacheEntry aNext) {
		fNext = aNext;
	}

	public LLFSHashMap getPage() {
		return fPage;
	}

	public void setPage(LLFSHashMap aPage) {
		fPage = aPage;
	}

	public boolean isDirty() {
		return fDirty;
	}

	public void setDirty(boolean aDirty) {
		fDirty = aDirty;
	}

}
