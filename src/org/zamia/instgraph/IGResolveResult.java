/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jul 22, 2009
 */
package org.zamia.instgraph;

import java.util.ArrayList;

/**
 * result of IGContainer:resolve(id)
 * 
 * immutable
 * 
 * @author Guenter Bartsch
 * 
 */

public class IGResolveResult {

	private ArrayList<IGItem> fResults;

	private IGResolveResult fParent;

	public IGResolveResult(IGResolveResult aParent, ArrayList<IGItem> aResults) {
		fParent = aParent;
		fResults = aResults;
	}

	public int getNumResults() {
		return fResults.size();
	}

	public IGItem getResult(int aIdx) {
		return fResults.get(aIdx);
	}

	public IGResolveResult getParent() {
		return fParent;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("IGResolveResult(nResults=" + fResults.size());

		if (fParent != null) {
			buf.append(" more results in parent(s)");
		}

		buf.append(")");
		return buf.toString();
	}
}
