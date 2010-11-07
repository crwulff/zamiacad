/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Oct 19, 2010
 */
package org.zamia.verilog.pre;

import java.io.IOException; 

import org.zamia.SourceFile;
import org.zamia.verilog.lexer.LexerException;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public interface IPreprocessor {

	
	public int getLine();
	
	public int getCol();
	
	public int read() throws IOException, LexerException;
	
	public void unread(char aC);
	
	public void close() throws IOException;

	public SourceFile getSourceFile();
	
}
