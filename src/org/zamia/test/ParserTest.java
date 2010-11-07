/* 
 * Copyright 2008-2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */

package org.zamia.test;

import java.io.PrintStream;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.zamia.ZamiaLogger;

/**
 * @author Guenter Bartsch
 */
public class ParserTest extends TestCase {

	PrintStream out = System.out;

	PrintStream err = System.err;

	private Builder fBuilder;

	public void setupTest() {
		ZamiaLogger.setup(Level.DEBUG);
		fBuilder = new Builder(out);
	}

	public void testWhenConcatenation() throws Exception {
		setupTest();

		fBuilder.clean();
		int n;
		if ((n = fBuilder.compileFile("test/whenamp.vhdl")) > 0) {
			System.out.println("Total: " + n + " errors.");

			fail("Errors detected.");
		}
	}

	public void testCharLiterals() throws Exception {
		setupTest();

		fBuilder.clean();
		int n;
		if ((n = fBuilder.compileFile("test/conversions.vhd")) > 0) {
			System.out.println("Total: " + n + " errors.");

			fail("Errors detected.");
		}
	}

	public void testG2() throws Exception {

		setupTest();

		fBuilder.clean();
		int n;
		if ((n = fBuilder.compileDir("test/g2", "WORK")) > 0) {
			System.out.println("Total: " + n + " errors.");
			fail("Errors detected.");
		}
	}

	public void testCaGrfAryA() throws Exception {
		setupTest();

		fBuilder.clean();
		int n;
		if ((n = fBuilder.compileFile("test/ca_grf_ary_a.vhdl")) > 0) {
			System.out.println("Total: " + n + " errors.");

			fail("Errors detected.");
		}
	}

	public void testCWNLat() throws Exception {
		setupTest();

		fBuilder.clean();
		int n;
		if ((n = fBuilder.compileFile("test/cw_nlat.vhdl")) > 0) {
			System.out.println("Total: " + n + " errors.");

			fail("Errors detected.");
		}
	}

	public void testExamples() throws Exception {

		setupTest();

		fBuilder.clean();
		int n;
		if ((n = fBuilder.compileDir("examples", "WORK")) > 0) {
			System.out.println("Total: " + n + " errors.");
			fail("Errors detected.");
		}
	}

	@Override
	protected void tearDown() {
		fBuilder.shutdown();
	}

}
