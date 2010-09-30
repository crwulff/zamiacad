/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.zdb;

/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class ZDBCacheEntry {

	private Object fObject;

	private ZDBCacheEntry fPrev, fNext;

	private long fId;
	
	private boolean fDirty;

	private boolean fDeleted = false;

	public ZDBCacheEntry(long aId, Object aObject, boolean aDirty) {
		fId = aId;
		fObject = aObject;
		fDirty = aDirty;
	}

	public long getId() {
		return fId;
	}

	public ZDBCacheEntry getPrev() {
		return fPrev;
	}

	public void setPrev(ZDBCacheEntry aPrev) {
		fPrev = aPrev;
	}

	public ZDBCacheEntry getNext() {
		return fNext;
	}

	public void setNext(ZDBCacheEntry aNext) {
		fNext = aNext;
	}

	public Object getObject() {
		return fObject;
	}

	public boolean isDirty() {
		return fDirty;
	}
	
	public void setDirty(boolean aDirty) {
		fDirty = aDirty;
	}

	public void setObject(Object aObject) {
		fObject = aObject;
	}

	public void setDeleted(boolean aDeleted) {
		fDeleted = aDeleted;
	}
	
	public boolean isDeleted() {
		return fDeleted;
	}
}
