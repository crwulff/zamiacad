/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 2, 2009
 */
package org.zamia.plugin.editors.annotations;

/**
 * 
 * @author Guenter Bartsch
 *
 */
public class Annotation implements Comparable<Annotation> {
	
	private int fOffset;
	private String fStr;
	
	public Annotation(int aOffset, String aStr) {
		
		setOffset(aOffset);
		setStr(aStr);
		
	}

	public void setOffset(int offset) {
		fOffset = offset;
	}

	public int getOffset() {
		return fOffset;
	}

	public void setStr(String str) {
		fStr = str;
	}

	public String getStr() {
		return fStr;
	}

	public int compareTo(Annotation aO) {
		return getOffset() - aO.getOffset();
	}
	

}
