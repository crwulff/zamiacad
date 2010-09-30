/*
 * Copyright 2004 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia;
/**
 * @author guenter bartsch
 */


@SuppressWarnings("serial")
public class ParserException extends Exception {
	private String message;
	
	public ParserException (String message_, int line_, int col_) {
		super (message_);
		message = message_;
	}
	
	public ParserException (String message_, SourceLocation location_) {
		super (message_);
		message = message_;
	}

	public String toString() {
		return message;
	}

}
