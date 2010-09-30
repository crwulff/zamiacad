/* 
 * Copyright 2007-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */


package org.zamia.test;

import java.io.File;
import java.io.PrintStream;
import java.io.Reader;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.zamia.FSCache;
import org.zamia.SourceFile;
import org.zamia.ZamiaLogger;
import org.zamia.rtl.sim.vcd.VCDParser;


/**
 * @author Guenter Bartsch
 */
public class VCDTest extends TestCase {

	private static FSCache fsCache = FSCache.getInstance();

	PrintStream out = System.out;
	PrintStream err = System.err;

	public void setupTest() {
		ZamiaLogger.setup(Level.DEBUG);
	}
	
	public void testCounter() throws Exception {
		
		VCDParser parser = new VCDParser();
		
		SourceFile sf = new SourceFile(new File ("test/vcd/adder.vcd"));
		
		Reader reader = fsCache.openFile(sf, false);
		
		parser.parse(reader, sf);

	}

	
}
