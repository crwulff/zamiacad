/*
 * Copyright 2008-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */
package org.zamia.analysis.ig;

import org.zamia.ToplevelPath;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite;
import org.zamia.instgraph.IGObject;
import org.zamia.util.HashMapArray;
import org.zamia.util.PathName;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class IGSearchResultBuilder {

	class RSRWrapper {
		private ReferenceSearchResult fResult;

		private HashMapArray<String, RSRWrapper> fChildren;

		public RSRWrapper(String aTitle) {
			fResult = new ReferenceSearchResult(aTitle, null, 0, fZPrj);
			fChildren = new HashMapArray<String, RSRWrapper>();
		}

		public RSRWrapper getOrCreateChild(String aLabel) {

			RSRWrapper res = fChildren.get(aLabel);
			if (res == null) {
				res = new RSRWrapper(aLabel);
				fChildren.put(aLabel, res);
			}
			return res;
		}

		public ReferenceSearchResult getResult() {
			return fResult;
		}

		public int getNumChildren() {
			return fChildren.size();
		}

		public RSRWrapper getChild(int aI) {
			return fChildren.get(aI);
		}
	}

	private RSRWrapper fRoot;

	private ZamiaProject fZPrj;

	public IGSearchResultBuilder(ZamiaProject aZPrj) {
		fZPrj = aZPrj;
		fRoot = new RSRWrapper("Root");
	}

	private RSRWrapper findResultHierachy(PathName aPath) {

		RSRWrapper res = fRoot;

		int n = aPath.getNumSegments();
		for (int i = 0; i < n; i++) {
			String segment = aPath.getSegment(i);
			if (segment != null) {
				res = res.getOrCreateChild(segment);
			}
		}

		return res;
	}

	public void add(ToplevelPath aPath, ReferenceSite aSite, IGObject aObj) {

		RSRWrapper wrapper = findResultHierachy(aPath.getPath());

		ReferenceSearchResult res = wrapper.getResult();

		if (aObj != null) {
			res.setDirection(aObj.getDirection());
		}

		res.add(aSite);
	}

	private ReferenceSearchResult getResult(RSRWrapper aWrapper) {

		ReferenceSearchResult result = aWrapper.getResult();

		int n = aWrapper.getNumChildren();
		for (int i = 0; i < n; i++) {

			RSRWrapper child = aWrapper.getChild(i);

			ReferenceSearchResult childResult = getResult(child);

			if (childResult.getNumChildren() > 0) {
				result.add(childResult);
			}
		}

		return result;
	}

	public ReferenceSearchResult getResult() {
		return getResult(fRoot);
	}

}
