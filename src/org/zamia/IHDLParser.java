/* 
 * Copyright 2005-2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by guenter on Dec 29, 2005
 */
package org.zamia;

import java.io.IOException;
import java.io.Reader;

import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.DUUID;



/**
 * Common interface of ZamiaCAD's HDL parsers
 * 
 * @author Guenter Bartsch
 */

public interface IHDLParser {

	
	/**
	 * Start parsing, both parameters are for referencing only
	 * and may be null.
	 * 
	 * @param reader_ input file to parse
	 * @param lib_ VHDL Library name the DesignUnits will be added to
	 * @param sf_ SourceFile reference for error reporting
	 * @param bottomUp_ if false, compiler will ignore entities and architectures that do not match the file name
	 * @param dum_ design unit manager which gets design units and errors handed over
	 * @return HashSet<DUUID>
	 * @throws IOException
	 */
	
	public HashSetArray<DUUID> parse(Reader aReader, String aLibId, SourceFile aSF, int aPriority, boolean aUseFSCache, boolean aBottomUp, ZamiaProject aZPrj) throws IOException;
	
}
