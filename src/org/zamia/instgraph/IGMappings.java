/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jul 25, 2009
 */
package org.zamia.instgraph;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGMappings implements Serializable {

	private ArrayList<IGMapping> fMappings = new ArrayList<IGMapping>();

	private int fScore = 0;
	
	private boolean fFailed = false;

	public IGMappings() {

	}

	public int getNumMappings() {
		return fMappings.size();
	}

	public IGMapping getMapping(int aIdx) {
		return fMappings.get(aIdx);
	}

	public void addMapping(IGMapping aMapping, int aScore) {
		fScore += aScore;
		fMappings.add(aMapping);
	}

	public int getScore() {
		return fScore;
	}

	public boolean isFailed() {
		return fFailed;
	}

	public void setFailed(boolean aFailed) {
		fFailed = aFailed;
	}

	@Override
	public String toString() {
		return "IGMappings{" + fScore + "}(" + fMappings + ")";
	}

}
