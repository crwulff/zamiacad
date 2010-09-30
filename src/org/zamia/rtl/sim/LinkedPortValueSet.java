/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 10, 2009
 */
package org.zamia.rtl.sim;

import java.util.HashMap;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLPort;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class LinkedPortValueSet {

	class Entry {
		public PortValue fPV;

		public Entry fNext;
	}

	private HashMap<RTLPort, Entry> fMap;

	private Entry fFirst, fLast;

	public LinkedPortValueSet() {
		fMap = new HashMap<RTLPort, Entry>();
	}

	public PortValue getFirst() {
		return fFirst.fPV;
	}

	public boolean isEmpty() {
		return fFirst == null;
	}

	public PortValue get(RTLPort aPort) {
		
		Entry entry = fMap.get(aPort);
		if (entry == null)
			return null;
		
		return entry.fPV;
	}

	public PortValue removeFirst() {

		Entry first = fFirst;
		fFirst = fFirst.fNext;
		if (fFirst == null) {
			fLast = null;
		}
		fMap.remove(first.fPV.fPort);

		return first.fPV;
	}

	public void set(PortValue aPV) {

		Entry entry = fMap.get(aPV.fPort);

		if (entry == null) {

			entry = new Entry();
			entry.fPV = aPV;

			if (fLast == null) {
				fLast = entry;
				fFirst = entry;
			} else {
				fLast.fNext = entry;
				fLast = entry;
			}

			fMap.put(aPV.fPort, entry);

		} else {
			entry.fPV = aPV;
		}
	}

	public ZILValue get(RTLPort aPort, ZILValue aValue) throws ZamiaException {

		PortValue pv = get(aPort);

		if (pv != null) {
			return pv.fValue;
		}

		ZILValue value = aValue.cloneValue();
		
		set(new PortValue(aPort, value));

		return value;
	}

	public void clear() {
		fMap = new HashMap<RTLPort, Entry>();
	}

}
