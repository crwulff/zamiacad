/*
 * Copyright 2007 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.vhdl.ast;

/**
 * @author guenter bartsch
 */
public enum NameMaskValue {
	UNDEFINED,
	SET;


	public String toString(){
	 if (this.compareTo(NameMaskValue.UNDEFINED )==0){
		 return "J";
	 }else
		 return "H";
	
	}
	
	public char toChar(){
		return this.toString().charAt(0);	
	}
	
}
