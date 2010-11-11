/* 
 * Copyright 2007-2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */

package org.zamia;

import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Test;

import java.io.PrintStream;

import static org.junit.Assert.fail;

/**
 * @author Guenter Bartsch
 */
public class DG2ETest {

	PrintStream out = System.out;

	PrintStream err = System.err;

	private Builder fBuilder;

	public void setupTest() {
		ZamiaLogger.setup(Level.DEBUG);
		fBuilder = new Builder(out);
	}

	@Test
	public void testVHDLParser() throws Exception {

		setupTest();

		fBuilder.clean();
		int n;
		if ((n = fBuilder.compileDir("examples/dg2e", "WORK")) > 0) {
			System.out.println("Total: " + n + " errors.");
			fail("Errors detected.");
		}
	}

	@After
	public void tearDown() {
		fBuilder.shutdown();
	}

	//	public void testOneFile() throws Exception {
	//		setupTest();
	//
	//		b.clean();
	//		int n;
	//		if ( (n=b.compileFile("/home/guenter/projects/workspace/zamia/test/dg2e/ch_04/fg_04_06.vhd")) > 0) {
	//			System.out.println ("Total: "+n+" errors.");
	//			
	//			fail("Errors detected.");		
	//		}
	//	}

}
