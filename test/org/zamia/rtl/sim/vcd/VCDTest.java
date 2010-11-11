/* 
 * Copyright 2007-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */


package org.zamia.rtl.sim.vcd;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;
import org.zamia.FSCache;
import org.zamia.SourceFile;
import org.zamia.ZamiaLogger;

import java.io.File;
import java.io.PrintStream;
import java.io.Reader;


/**
 * @author Guenter Bartsch
 */
public class VCDTest {

	private static FSCache fsCache = FSCache.getInstance();

	PrintStream out = System.out;
	PrintStream err = System.err;

	@Before
	public void setupTest() {
		ZamiaLogger.setup(Level.DEBUG);
	}

	@Test
	public void testCounter() throws Exception {

		VCDParser parser = new VCDParser();

		SourceFile sf = new SourceFile(new File("examples/vcd/adder.vcd"));

		Reader reader = fsCache.openFile(sf, false);

		parser.parse(reader, sf);

	}

}
