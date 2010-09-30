/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on May 23, 2009
 */
package org.zamia.instgraph;

import java.io.Serializable;

import org.zamia.SourceLocation;



/**
 * 
 * Result of computeAccessedItems
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class IGItemAccess implements Serializable {

	public enum AccessType {
		Read, Write, Call, Declaration, ReadWrite, Unknown, Instantiation
	};

	private IGItem fItem;
	
	private AccessType fAccessType;

	private int fDepth;
	
	private SourceLocation fLocation;

	public IGItemAccess(IGItem aItem, AccessType aAccessType, int aDepth, SourceLocation aLocation) {
		fItem = aItem;
		fAccessType = aAccessType;
		fDepth = aDepth;
		fLocation = aLocation;
	}
	
	public AccessType getAccessType() {
		return fAccessType;
	}
	
	public int getDepth() {
		return fDepth;
	}
	
	public IGItem getItem() {
		return fItem;
	}
	
	public SourceLocation getLocation() {
		return fLocation;
	}
}
