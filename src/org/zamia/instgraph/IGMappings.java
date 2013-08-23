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
import java.util.Collection;

/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGMappings extends ArrayList<IGMapping> implements Serializable {

	private int fScore = 0;
	
	private boolean fFailed = false;

	public IGMappings() {

	}

//	public int getNumMappings() {
//		return fMappings.size();
//	}
//
	
	private boolean addBan() {
		throw new InternalError("Substitution add is banned");
	}
	public boolean add(IGMapping e) {return addBan();}
	public void add(int index, IGMapping element) {addBan();}@Override
	public boolean addAll(Collection<? extends IGMapping> c) {return addBan();}
	public boolean addAll(int index, Collection<? extends IGMapping> c) {return addBan();}
	
	public void addMapping(IGMapping aMapping, int aScore) {
		fScore += aScore;
		super.add(aMapping);
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
		return "IGMappings{" + fScore + "}(" + super.toString() + ")";
	}

}
