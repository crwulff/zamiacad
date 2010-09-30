/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 25, 2008
 */
package org.zamia.instgraph.interpreter;

import org.zamia.ZamiaException;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGTypeStatic;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGStackFrame  {

	private IGStaticValue fValue;

	private IGTypeStatic fType;
	
	private IGObjectWriter fWriter;
	
	public IGStaticValue getLiteral() {
		return fValue;
	}

	public IGStackFrame(IGStaticValue aValue) {
		fValue = aValue;
	}

	public IGStackFrame(IGTypeStatic aType) {
		fType = aType;
	}

	public IGStackFrame(IGObjectWriter aWriter) {
		fWriter = aWriter;
	}

	public IGStaticValue getValue() throws ZamiaException {

		if (fValue != null) {
			return fValue;
		}
		throw new ZamiaException("StackFrame.getValue(): Internal error - empty.");
	}

	public boolean getBool() throws ZamiaException {
		
		IGStaticValue v = getValue();

		return v.isLogicOne();
	}

	public int getInt() throws ZamiaException {
		IGStaticValue v = getValue();

		return v.getInt();
	}
	
	public IGTypeStatic getType() {
		return fType;
	}
	
	@Override
	public String toString () {
		if (fType != null)
			return "SF Type "+fType;
		if (fValue != null)
			return "SF Value "+fValue;
		if (fWriter != null)
			return "SF Writer "+fWriter;
		return "SF VOID ?";
	}

	public IGObjectWriter getObjectWriter() {
		return fWriter;
	}

}
