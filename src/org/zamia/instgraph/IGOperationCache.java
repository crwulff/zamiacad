/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Aug 10, 2009
 */
package org.zamia.instgraph;

import java.util.ArrayList;
import java.util.HashMap;

import org.zamia.vhdl.ast.Name;
import org.zamia.vhdl.ast.Operation;


/**
 * Transients used during expression elaboration
 * 
 * @author Guenter Bartsch
 * 
 */

public class IGOperationCache {

	private HashMap<Name, IGResolveResult> fNameCache;

	private HashMap<Name, HashMap<IGType, IGResolveResult>> fNameCacheHinted;

	private HashMap<Operation, ArrayList<IGOperation>> fOperationCache;

	private HashMap<Operation, HashMap<IGType, ArrayList<IGOperation>>> fOperationCacheHinted;

	private HashMap<IGRange, IGOperation> fRangeCache;

	public IGOperationCache() {
		fNameCache = new HashMap<Name, IGResolveResult>();
		fNameCacheHinted = new HashMap<Name, HashMap<IGType, IGResolveResult>>();
		fOperationCache = new HashMap<Operation, ArrayList<IGOperation>>();
		fOperationCacheHinted = new HashMap<Operation, HashMap<IGType, ArrayList<IGOperation>>>();
		fRangeCache = new HashMap<IGRange, IGOperation>();
	}

	public IGResolveResult getIGResolveResult(Name aName, IGType aTypeHint) {
		if (aTypeHint == null) {
			return fNameCache.get(aName);
		}
		HashMap<IGType, IGResolveResult> hm = fNameCacheHinted.get(aName);
		if (hm == null) {
			return null;
		}
		return hm.get(aTypeHint);
	}

	public void setIGResolveResult(Name aName, IGType aTypeHint, IGResolveResult aItems) {
		if (aTypeHint == null) {
			fNameCache.put(aName, aItems);
		} else {
			HashMap<IGType, IGResolveResult> hm = fNameCacheHinted.get(aName);
			if (hm == null) {
				hm = new HashMap<IGType, IGResolveResult>();
				fNameCacheHinted.put(aName, hm);
			}
			hm.put(aTypeHint, aItems);
		}
	}

	public ArrayList<IGOperation> getIGOperation(Operation aOperation, IGType aTypeHint) {
		if (aTypeHint == null) {
			return fOperationCache.get(aOperation);
		}
		HashMap<IGType, ArrayList<IGOperation>> hm = fOperationCacheHinted.get(aOperation);
		if (hm == null) {
			return null;
		}
		return hm.get(aTypeHint);
	}

	public void setIGOperation(Operation aOperation, IGType aTypeHint, ArrayList<IGOperation> aIGOperations) {
		if (aTypeHint == null) {
			fOperationCache.put(aOperation, aIGOperations);
		} else {

			HashMap<IGType, ArrayList<IGOperation>> hm = fOperationCacheHinted.get(aOperation);
			if (hm == null) {
				hm = new HashMap<IGType, ArrayList<IGOperation>>();
				fOperationCacheHinted.put(aOperation, hm);
			}
			hm.put(aTypeHint, aIGOperations);
		}
	}

	public IGOperation getCardinality(IGRange aRange) {
		return fRangeCache.get(aRange);
	}

	public void setCardinality(IGRange aRange, IGOperation aCard) {
		fRangeCache.put(aRange, aCard);
	}

}
